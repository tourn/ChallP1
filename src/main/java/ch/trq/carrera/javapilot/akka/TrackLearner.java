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
 *  An actor driving with constant power, learning the structure of the track
 */
public class TrackLearner extends UntypedActor {

    private static final int MOVE_START_GYROZ_THRESHOLD = 10;
    private static final int MOVE_START_POWER_INCREASE = 10;
    private static final double TURN_THRESHOLD = 800;
    private static final int MOVE_TRY_POWER_INCREASE = 1;

    private ActorRef pilot;
    private FloatingHistory gyroZ;
    private TrackAnalyzer trackAnalyzer;
    private PhysicModel physicModel;
    private PhysicModelCalculator physicModelCalculator;
    private State turnState = State.STRAIGHT;
    private final Logger LOGGER = LoggerFactory.getLogger(TrackLearner.class);

    private int power;

    private boolean isMoving = false;
    private boolean trackRecognitionFinished = false;

    public TrackLearner(ActorRef pilot, int startPower, int startRoundNr, int amountOfRounds, int faultyGoingStraightTime, int faultyTurnTime, int floatingHistorySize) {
        this.pilot = pilot;
        this.power = startPower;
        gyroZ = new FloatingHistory(floatingHistorySize);

        physicModel = new PhysicModel();

        trackAnalyzer = new TrackAnalyzer();

        trackAnalyzer.setOnTrackRecognized(new TrackAnalyzer.TrackRecognitionCallback() {
            @Override
            public void onTrackRecognized(Track track) {
                trackRecognitionFinished = true;
                track.setPower(power);
                physicModelCalculator = new PhysicModelCalculator(track,physicModel);
                physicModelCalculator.calculateTrackPhysics();
                LOGGER.info("Track Received");
                //pilot.tell(track, ActorRef.noSender());
            }
        });
    }

    public static Props props ( ActorRef pilot, int power, int startRoundNr, int amountOfRounds, int faultyGoingStraightTime, int faultyTurnTime, int floatingHistorySize ) {
        return Props.create( TrackLearner.class, ()->new TrackLearner( pilot, power, startRoundNr, amountOfRounds, faultyGoingStraightTime, faultyTurnTime, floatingHistorySize));
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
        }else{

        }
        pilot.tell(new PowerAction(power), getSelf());
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
            physicModel.setStartPower(power);
            power += MOVE_START_POWER_INCREASE;
        }else{
            power += MOVE_TRY_POWER_INCREASE;
        }
        //LOGGER.info("MY POWER: " + power);
    }
}
