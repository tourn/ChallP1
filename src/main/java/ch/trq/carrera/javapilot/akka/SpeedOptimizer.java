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
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Currently not optimizing anything, merely a placeholder.
 */
public class SpeedOptimizer extends UntypedActor {

    private final Logger LOGGER = LoggerFactory.getLogger(SpeedOptimizer.class);
    private ActorRef pilot;
    private final Track track;
    private int power = 200;
    private int minPower = 100;
    private int maxTurnPower = 150;
    private int maxPower = 200;
    private PositionTracker positionTracker;
    private String actorDescription;

    public SpeedOptimizer(ActorRef pilot, Track track) {
        this.pilot = pilot;
        this.track = track;
        positionTracker = new PositionTracker(track);

        positionTracker.setOnUpdate(new PositionTracker.UpdateCallback() {
            @Override
            public void onUpdate(int sectionIndex, long offset, double percentage) {
                pilot.tell(new CarUpdate(sectionIndex, offset, percentage), getSelf());
            }
        });

        positionTracker.setOnNewRound(new PositionTracker.NewRoundCallback() {
            @Override
            public void onUpdate(long roundtime) {
                pilot.tell(new NewRoundUpdate(roundtime), getSelf());
            }
        });

        positionTracker.setOnSectionChange(new PositionTracker.SectionChangeCallback() {
            @Override
            public void onUpdate(int sectionIndex, TrackSection section) {
                pilot.tell(new SectionUpdate(section, sectionIndex), getSelf());
                if(section.getDirection().equals(Direction.TURN)){
                    changePower(maxPower);
                } else {
                    changePower(maxTurnPower);
                }
            }
        });
        changePower(maxTurnPower);

        actorDescription = getActorDescription();
    }

    public static Props props(ActorRef pilot, Track track) {
        return Props.create(SpeedOptimizer.class, () -> new SpeedOptimizer(pilot, track));
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
        } else {
            unhandled(message);
        }
    }

    private void handleRoundTimeMessage(RoundTimeMessage message) {
        // ignore for now
        //positionTracker.roundTimeUpdate(message);
    }

    private void handleVelocityMessage(VelocityMessage message) {
        // ignore for now
        positionTracker.velocityUpdate(message);
    }

    private void handleSensorEvent(SensorEvent event) {
        LogMessage log = new LogMessage(event, System.currentTimeMillis());
        positionTracker.update(event);
        if (!positionTracker.isTurn()) {
            if (positionTracker.getPercentageDistance() > 0.5) {
                changePower(minPower);
            } else {
                changePower(maxPower);
            }
        } else {
            changePower(maxTurnPower);
        }
        //pilot.tell ( new PowerAction(power), getSelf());
        popluateLog(log);
        pilot.tell(log, getSelf());
    }

    private void popluateLog(LogMessage log){
        log.setPower(positionTracker.getPower());
        log.setActorDescription(actorDescription);
        log.setPositionRelative(positionTracker.getPos().getDistanceOffset());
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
