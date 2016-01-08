package ch.trq.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.trq.carrera.javapilot.history.StrategyParameters;
import ch.trq.carrera.javapilot.history.TrackHistory;
import ch.trq.carrera.javapilot.akka.messages.CarUpdate;
import ch.trq.carrera.javapilot.positiontracker.PositionTracker;
import ch.trq.carrera.javapilot.positiontracker.SectionUpdate;
import ch.trq.carrera.javapilot.trackanalyzer.Track;
import ch.trq.carrera.javapilot.trackanalyzer.TrackAndPhysicModelStorage;
import ch.trq.carrera.javapilot.trackanalyzer.TrackSection;
import ch.trq.carrera.javapilot.math.PhysicModel;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeedOptimizer extends UntypedActor {

    private final int WAIT_TIME_FOR_RECOVERY = 3000;
    private final int ZERO_POWER = 0;
    private final long WAIT_TIME_FOR_PENALTY = 3000;
    private static final int MIN_DECREMENT = 2;

    private int numberOfPenalties = 0;
    private boolean hasPenalty = false;
    private long reciveLastPenaltyMessageTime = 0; // Computer-System-Time... CARE

    private ActorRef pilot;
    private final Track track;
    private final int slowDownPower = 120;
    private int maxPower = 200;
    private PositionTracker positionTracker;
    private final TrackHistory history;
    private StrategyParameters currentStrategyParams;
    private boolean recoveringFromPenalty = false;
    private double maxVelocityForTurn = 0;
    private PhysicModel physicModel;
    private final Logger LOGGER = LoggerFactory.getLogger(TrackLearner.class);

    public SpeedOptimizer(ActorRef pilot, TrackAndPhysicModelStorage storage) {
        this.pilot = pilot;
        this.track = storage.getTrack();

        positionTracker = new PositionTracker(storage.getTrack(), storage.getPhysicModel());
        positionTracker.setOnSectionChanged(this::onSectionChanged);
        this.physicModel = storage.getPhysicModel();

        history = new TrackHistory(track);
        TrackSection currentSection = positionTracker.getCarPosition().getSection();
        currentStrategyParams = createStrategyParams(history.getValidHistory(currentSection.getId()));

    }

    private void onSectionChanged(TrackSection completedSection) {
        tellSectionUpdate(completedSection);
        updateHistory(completedSection);
        final int currentSectionId = positionTracker.getCarPosition().getSection().getId();
        final StrategyParameters paramsFromLastRound = history.getValidHistory(currentSectionId);
        if(positionTracker.isTurn()){
            currentStrategyParams = createTurnStrategyParams(paramsFromLastRound);
        }else {
            currentStrategyParams = createStrategyParams(paramsFromLastRound);
        }
    }

    public static Props props(ActorRef pilot, TrackAndPhysicModelStorage storage) {
        return Props.create(SpeedOptimizer.class, () -> new SpeedOptimizer(pilot, storage));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SensorEvent) {
            handleSensorEvent((SensorEvent) message);
        } else if (message instanceof VelocityMessage) {
            handleVelocityMessage((VelocityMessage) message);
        } else if (message instanceof RoundTimeMessage) {
            handleRoundTimeMessage((RoundTimeMessage) message);
        } else if (message instanceof PenaltyMessage) {
            handlePenaltyMessage((PenaltyMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void handleRoundTimeMessage(RoundTimeMessage message) {
        //ignore
    }

    private void handleVelocityMessage(VelocityMessage message) {
        if (System.currentTimeMillis() - reciveLastPenaltyMessageTime > (WAIT_TIME_FOR_RECOVERY + WAIT_TIME_FOR_PENALTY)) {
            recoveringFromPenalty = false;
        }
        if(numberOfPenalties==0) {
            maxVelocityForTurn = positionTracker.getMaxTurnVelocity();
        }
        positionTracker.velocityUpdate(message);
    }

    private void handleSensorEvent(SensorEvent event) {
        positionTracker.sensorUpdate(event);
        if (hasPenalty) {
            tellChangePower(ZERO_POWER);
            if (System.currentTimeMillis() - reciveLastPenaltyMessageTime > WAIT_TIME_FOR_PENALTY) {
                hasPenalty = false;
            }
        } else {
            if (recoveringFromPenalty) {
                tellChangePower(track.getLearningPower());
            } else {

                if (!positionTracker.isTurn()) {
                    if (positionTracker.getPercentageDistance() > currentStrategyParams.getBrakePercentage()) {
                        tellChangePower(slowDownPower);
                    } else {
                        tellChangePower(currentStrategyParams.getPower());
                    }
                } else {
                    tellChangePower(currentStrategyParams.getPower());
                }
            }
        }
        Track.Position carPosition = positionTracker.getCarPosition();
        pilot.tell(new CarUpdate(carPosition.getSection().getId(), carPosition.getDurationOffset(), carPosition.getPercentage()), getSelf());
    }

    private void handlePenaltyMessage(PenaltyMessage message) {
        tellChangePower(ZERO_POWER);
        numberOfPenalties++;
        reciveLastPenaltyMessageTime = System.currentTimeMillis();
        hasPenalty = true;
        recoveringFromPenalty = true;
        currentStrategyParams.setPenaltyOccurred(true);
    }

    private void tellChangePower(int power) {
        pilot.tell(new PowerAction(power), getSelf());
        positionTracker.setPower(power);
    }

    private void updateHistory(TrackSection section) {
        currentStrategyParams.setDuration(section.getDuration()); //FIXME: duration is currently not set in the section
        history.addEntry(currentStrategyParams);
    }

    private StrategyParameters createStrategyParams(StrategyParameters paramsFromPreviousRound) {
        StrategyParameters params = new StrategyParameters();

        params.setPowerIncrement(paramsFromPreviousRound.getPowerIncrement());
        params.setBrakePercentage(paramsFromPreviousRound.getBrakePercentage());
        params.setSection(paramsFromPreviousRound.getSection());

        int power = paramsFromPreviousRound.getPower();
        if (paramsFromPreviousRound.isPenaltyOccurred()) {
            power -= Math.max(MIN_DECREMENT, params.getPowerIncrement());
            params.setPowerIncrement((int) (params.getPowerIncrement() * 0.5));
        } else {
            if (power + params.getPowerIncrement() < maxPower) {
                power += params.getPowerIncrement();
            }
        }
        params.setPower(power);

        if (recoveringFromPenalty) {
            params.setValid(false);
        }
        return params;
    }

    private StrategyParameters createTurnStrategyParams(StrategyParameters paramsFromPreviousRound){
        StrategyParameters params = new StrategyParameters();
        params.setSection(paramsFromPreviousRound.getSection());
        int power = paramsFromPreviousRound.getPower();
        if(maxVelocityForTurn != 0){
            power = physicModel.getPowerForVelocity(maxVelocityForTurn,params.getSection());
            LOGGER.info("mVFT: "+maxVelocityForTurn);
            LOGGER.info("p: "+ power);
        }
        params.setPower(power);
        return params;
    }

    public void tellSectionUpdate(TrackSection section) {
        SectionUpdate sectionUpdate = new SectionUpdate(section);
        sectionUpdate.setPenaltyOccured(currentStrategyParams.isPenaltyOccurred());
        sectionUpdate.setPowerInSection(currentStrategyParams.getPower());
        pilot.tell(sectionUpdate, getSelf());
    }
}
