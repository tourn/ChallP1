package ch.trq.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.trq.carrera.javapilot.akka.log.LogMessage;
import ch.trq.carrera.javapilot.akka.positiontracker.CarUpdate;
import ch.trq.carrera.javapilot.akka.positiontracker.NewRoundUpdate;
import ch.trq.carrera.javapilot.akka.positiontracker.PositionTracker;
import ch.trq.carrera.javapilot.akka.positiontracker.SectionUpdate;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Direction;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackAndPhysicModelStorage;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Currently not optimizing anything, merely a placeholder.
 */
public class SpeedOptimizer extends UntypedActor {

    private final int ZERO_POWER = 0;
    private final long WAIT_TIME_FOR_PENALTY = 3000;

    private boolean hasPenalty = false;
    private long reciveLastPenaltyMessageTime = 0; // Computer-System-Time... CARE

    private final Logger LOGGER = LoggerFactory.getLogger(SpeedOptimizer.class);
    private ActorRef pilot;
    private final Track track;
    private final int minPower = 130;
    private final int maxTurnPower = 150;
    private final int maxPower = 200;
    private PositionTracker positionTracker;
    private String actorDescription;

    public SpeedOptimizer(ActorRef pilot, TrackAndPhysicModelStorage storage) {
        this.pilot = pilot;
        this.track = storage.getTrack();
        positionTracker = new PositionTracker(storage.getTrack(), storage.getPhysicModel());

        changePower(maxTurnPower);

        actorDescription = getActorDescription();
    }

    public static Props props(ActorRef pilot, TrackAndPhysicModelStorage storage) {
        return Props.create(SpeedOptimizer.class, () -> new SpeedOptimizer(pilot, storage));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        //TODO send updates for completed tracksection, position update
        if (message instanceof SensorEvent) {
            handleSensorEvent((SensorEvent) message);
        } else if (message instanceof VelocityMessage) {
            handleVelocityMessage((VelocityMessage) message);
        } else if (message instanceof RoundTimeMessage) {
            handleRoundTimeMessage((RoundTimeMessage) message);
        } else if (message instanceof PenaltyMessage){
            handlePenaltyMessage((PenaltyMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void handleRoundTimeMessage(RoundTimeMessage message) {
        //ignore
    }

    private void handleVelocityMessage(VelocityMessage message) {
        positionTracker.velocityUpdate(message);
    }

    private void handleSensorEvent(SensorEvent event) {
        LogMessage log = new LogMessage(event, System.currentTimeMillis());
        positionTracker.update(event);
        if(hasPenalty){
            changePower(ZERO_POWER);
            if(System.currentTimeMillis() - reciveLastPenaltyMessageTime > WAIT_TIME_FOR_PENALTY){
                hasPenalty = false;
            }
        }else {
            if (!positionTracker.isTurn()) {
                if (positionTracker.getPercentageDistance() > 0.5) {
                    changePower(minPower);
                } else {
                    changePower(maxPower);
                }
            } else {
                changePower(maxTurnPower);
            }
        }
        popluateLog(log);
        Track.Position carPosition = positionTracker.getCarPosition();
        pilot.tell(new CarUpdate(carPosition.getSection().getId(), carPosition.getDurationOffset(), carPosition.getPercentage()), getSelf());
        pilot.tell(log, getSelf());
    }

    private void handlePenaltyMessage(PenaltyMessage message) {
        // TODO: Set Power to Zero, Wait 3sek, start again
        changePower(ZERO_POWER);
        reciveLastPenaltyMessageTime = System.currentTimeMillis();
        hasPenalty = true;

    }

    private void popluateLog(LogMessage log){
        log.setPower(positionTracker.getPower());
        log.setActorDescription(actorDescription);
        log.setPositionRelative(positionTracker.getCarPosition().getDistanceOffset());
        log.settAfterCalculation(System.currentTimeMillis());
    }

    public String getActorDescription(){
        return "SpeedOptimizer";
    }

    private void changePower(int power) {
        pilot.tell(new PowerAction(power), getSelf());
        positionTracker.setPower(power);
    }
}
