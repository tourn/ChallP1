package ch.trq.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
//import com.sun.tools.internal.jxc.ap.Const;
import ch.trq.carrera.javapilot.akka.trackanalyzer.State;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.math.PhysicModel;
import ch.trq.carrera.javapilot.math.PhysicModelCalculator;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackAnalyzer;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.carrera.timeseries.FloatingHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  An actor driving with constant currentPower, learning the structure of the track
 */
public class TrackLearner extends UntypedActor {
    private final Logger LOGGER = LoggerFactory.getLogger(TrackLearner.class);

    private static final int MOVE_START_GYROZ_THRESHOLD = 10;
    private static final int MOVE_START_POWER_INCREASE = 10;
    private static final double TURN_THRESHOLD = 800;
    private static final int MOVE_TRY_POWER_INCREASE = 1;
    private static final int START_POWER = 50;
    private static final int GYROZ_HISTORY_SIZE = 4;


    private static final int MIN_STRAIGHT_DURATION = 180;
    private static final int MIN_TURN_DURATION = 250;
    private static final int MIN_TRACK_SECTIONS = 4;

    private ActorRef pilot;
    private TrackAnalyzer trackAnalyzer = new TrackAnalyzer(MIN_STRAIGHT_DURATION, MIN_TURN_DURATION, MIN_TRACK_SECTIONS);
    private PhysicModel physicModel = new PhysicModel();
    private PhysicModelCalculator physicModelCalculator;

    private State turnState = State.STRAIGHT;
    private FloatingHistory gyroZ;
    private int currentPower;
    private boolean isMoving = false;
    private boolean trackRecognitionFinished = false;

    public TrackLearner(ActorRef pilot) {
        this.pilot = pilot;
        this.currentPower = START_POWER;
        gyroZ = new FloatingHistory(GYROZ_HISTORY_SIZE);
    }

    public static Props props ( ActorRef pilot) {
        return Props.create( TrackLearner.class, ()->new TrackLearner(pilot));
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if ( message instanceof SensorEvent) {
            handleSensorEvent((SensorEvent) message);
        } else if ( message instanceof VelocityMessage) {
            handleVelocityMessage((VelocityMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void handleVelocityMessage(VelocityMessage message) {
        if(!trackRecognitionFinished){
            trackAnalyzer.addTrackVelocity(message.getVelocity(),message.getTimeStamp());
        }else{
            //TODO
        }
    }

    private void handleSensorEvent(SensorEvent event) {
        gyroZ.shift(event.getG()[2]);
        if(!trackRecognitionFinished){
            if(!isMoving){
                tryStartMoving();
            }else{
                checkDirectionChange(event);
            }
            if(trackAnalyzer.detectCycle()){
                onTrackRecognized(trackAnalyzer.buildTrack());
            }
        }else{
            //TODO: could we already calculate friction on a straight with 3 velocity sensors? If no, use Frank's strategy:
            //stop in a straight with 2 sensors and start again.
        }
        pilot.tell(new PowerAction(currentPower), getSelf());
    }

    private void checkDirectionChange(SensorEvent event){
        switch (turnState) {
            case STRAIGHT:
                straightAction(event.getTimeStamp());
                break;
            case RIGHT:
            case LEFT:
                turnAction(event.getTimeStamp());
                break;
        }

    }

    private void straightAction(long timeStamp) {
        if (gyroZ.currentMean() > TURN_THRESHOLD) {
            turnState = State.RIGHT;
            trackAnalyzer.addTrackSection(turnState,timeStamp);
            //LOGGER.info("RIGHT TURN");
        }else if(-gyroZ.currentMean() > TURN_THRESHOLD){
            turnState = State.LEFT;
            trackAnalyzer.addTrackSection(turnState,timeStamp);
            //LOGGER.info("LEFT TURN");
        }
    }

    private void turnAction(long timeStamp) {
        if (Math.abs(gyroZ.currentMean()) < TURN_THRESHOLD) {
            turnState = State.STRAIGHT;
            trackAnalyzer.addTrackSection(turnState,timeStamp);
            //LOGGER.info("GOING STRAIGHT");
        }
    }

    private void tryStartMoving() {
        isMoving = gyroZ.currentMean() > MOVE_START_GYROZ_THRESHOLD;

        if(isMoving){
            physicModel.setStartPower(currentPower);
            currentPower += MOVE_START_POWER_INCREASE;
        }else{
            currentPower += MOVE_TRY_POWER_INCREASE;
        }
        //LOGGER.info("MY POWER: " + currentPower);
    }

    public void onTrackRecognized(Track track) {
        trackRecognitionFinished = true;
        track.setPower(currentPower);
        physicModelCalculator = new PhysicModelCalculator(track,physicModel);
        physicModelCalculator.calculateTrackPhysics();
        LOGGER.info("Track built");
        //pilot.tell(track, ActorRef.noSender());
    }
}
