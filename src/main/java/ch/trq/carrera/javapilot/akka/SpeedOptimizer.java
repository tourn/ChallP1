package ch.trq.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.trq.carrera.javapilot.akka.history.StrategyParameters;
import ch.trq.carrera.javapilot.akka.history.TrackHistory;
import ch.trq.carrera.javapilot.akka.positiontracker.CarUpdate;
import ch.trq.carrera.javapilot.akka.positiontracker.PositionTracker;
import ch.trq.carrera.javapilot.akka.positiontracker.SectionUpdate;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackAndPhysicModelStorage;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

public class SpeedOptimizer extends UntypedActor {

    private final int WAIT_TIME_FOR_RECOVERY = 3000;
    private final int ZERO_POWER = 0;
    private final long WAIT_TIME_FOR_PENALTY = 3000;
    private static final int MIN_DECREMENT = 2;

    private boolean hasPenalty = false;
    private long reciveLastPenaltyMessageTime = 0; // Computer-System-Time... CARE

    private ActorRef pilot;
    private final Track track;
    private final int minPower = 120;
    private final int maxTurnPower = 150;
    private int maxPower = 200;
    private PositionTracker positionTracker;
    private String actorDescription;
    private final TrackHistory history;
    private StrategyParameters currentStrategyParams;
    private boolean recoveringFromPenalty = false;

    public SpeedOptimizer(ActorRef pilot, TrackAndPhysicModelStorage storage) {
        this.pilot = pilot;
        this.track = storage.getTrack();

        positionTracker = new PositionTracker(storage.getTrack(), storage.getPhysicModel());
        positionTracker.setOnSectionChanged(this::onSectionChanged);

        history = new TrackHistory(track);
        TrackSection currentSection = positionTracker.getCarPosition().getSection();
        currentStrategyParams = createStrategyParams(history.getValidHistory(currentSection.getId()));

        tellChangePower(maxTurnPower);

        actorDescription = getActorDescription();
    }

    private void onSectionChanged(TrackSection section) {
        tellSectionUpdate(section);
        updateHistory(section);
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
                tellChangePower(maxTurnPower);
            } else {

                if (!positionTracker.isTurn()) {
                    if (positionTracker.getPercentageDistance() > currentStrategyParams.getBrakePercentage()) {
                        tellChangePower(minPower);
                    } else {
                        tellChangePower(currentStrategyParams.getPower());
                    }
                } else {
                    tellChangePower(maxTurnPower);
                }
            }
        }
        Track.Position carPosition = positionTracker.getCarPosition();
        pilot.tell(new CarUpdate(carPosition.getSection().getId(), carPosition.getDurationOffset(), carPosition.getPercentage()), getSelf());
    }

    private void handlePenaltyMessage(PenaltyMessage message) {
        tellChangePower(ZERO_POWER);
        reciveLastPenaltyMessageTime = System.currentTimeMillis();
        hasPenalty = true;
        recoveringFromPenalty = true;
        currentStrategyParams.setPenaltyOccurred(true);
    }

    public String getActorDescription() {
        return "SpeedOptimizer";
    }

    private void tellChangePower(int power) {
        pilot.tell(new PowerAction(power), getSelf());
        positionTracker.setPower(power);
    }

    private void updateHistory(TrackSection section) {
        currentStrategyParams.setDuration(section.getDuration()); //FIXME: duration is currently not set in the section
        history.addEntry(currentStrategyParams);
        currentStrategyParams = createStrategyParams(history.getValidHistory(positionTracker.getCarPosition().getSection().getId()));
    }

    private StrategyParameters createStrategyParams(StrategyParameters previous) {
        StrategyParameters params = new StrategyParameters();

        params.setPowerIncrement(previous.getPowerIncrement());
        params.setBrakePercentage(previous.getBrakePercentage());
        params.setSection(previous.getSection());

        int power = previous.getPower();
        if (previous.isPenaltyOccurred()) {
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

    public void tellSectionUpdate(TrackSection section) {
        SectionUpdate sectionUpdate = new SectionUpdate(section);
        sectionUpdate.setPenaltyOccured(currentStrategyParams.isPenaltyOccurred());
        if (positionTracker.isTurn()) {
            sectionUpdate.setPowerInSection(positionTracker.getPower());
        } else {
            sectionUpdate.setPowerInSection(currentStrategyParams.getPower());
        }
        pilot.tell(sectionUpdate, getSelf());
    }
}
