package ch.trq.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.javapilot.akka.experimental.ThresholdConfiguration;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

/**
 *  Currently not optimizing anything, merely a placeholder.
 */
public class SpeedOptimizer extends UntypedActor {

    private final Logger LOGGER = LoggerFactory.getLogger(SpeedOptimizer.class);
    private ActorRef pilot;
    private final Track track;
    private int power = 200;

    public SpeedOptimizer(ActorRef pilot, Track track) {
        this.pilot = pilot;
        this.track = track;
    }

    public static Props props ( ActorRef pilot, Track track ) {
        return Props.create( SpeedOptimizer.class, ()->new SpeedOptimizer( pilot, track ));
    }

    @Override
    public void onReceive(Object message) throws Exception {
        //TODO send updates for completed tracksection, position update
        if ( message instanceof SensorEvent ) {
            handleSensorEvent((SensorEvent) message);
        } else if ( message instanceof VelocityMessage) {
                handleVelocityMessage((VelocityMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void handleVelocityMessage(VelocityMessage message) {
        // ignore for now
    }

    private void handleSensorEvent(SensorEvent event) {
        pilot.tell ( new PowerAction(power), getSelf());
    }
}
