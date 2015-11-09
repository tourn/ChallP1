package ch.trq.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
//import com.sun.tools.internal.jxc.ap.Const;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Round;
import ch.trq.carrera.javapilot.akka.trackanalyzer.State;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.math.PhysicModel;
import ch.trq.carrera.javapilot.math.PhysicModelCalculator;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.javapilot.akka.experimental.ThresholdConfiguration;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackAnalyzer;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.carrera.timeseries.FloatingHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import visualization.DataChart;

/**
 *  A very simple actor that determines the power value by a configurable Threshold on any of the 10 observables
 */
public class TrackLearner extends UntypedActor {

    private ActorRef pilot;
    private FloatingHistory gyroZ;
    private TrackAnalyzer trackAnalyzer;
    private PhysicModel physicModel;
    private PhysicModelCalculator physicModelCalculator;
    private State state = State.STRAIGHT;
    private static double TURN_THRESHOLD = 800;
    private final Logger LOGGER = LoggerFactory.getLogger(TrackLearner.class);

    private int power;
    private int increasePowerRate = 1;
    private int amountOverMovePower =10;
    private int powerIncreaseForPhysicCalculation = 20;

    private boolean hasStartedMoving = false;
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
                power += powerIncreaseForPhysicCalculation;
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
            if(!hasStartedMoving){
                carHasNotStarted();
            }else{
                switch (state) {
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
            state = State.RIGHT;
            trackAnalyzer.addTrackSection(state,timeStamp);
            //LOGGER.info("RIGHT TURN");
        }else if(-gyroZ.currentMean() > TURN_THRESHOLD){
            state = State.LEFT;
            trackAnalyzer.addTrackSection(state,timeStamp);
            //LOGGER.info("LEFT TURN");
        }
    }

    private void turnAction(long timeStamp) {
        if (Math.abs(gyroZ.currentMean()) < TURN_THRESHOLD) {
            state = State.STRAIGHT;
            trackAnalyzer.addTrackSection(state,timeStamp);
            //LOGGER.info("GOING STRAIGHT");
        }
    }

    private void carHasNotStarted() {
        hasStartedMoving = gyroZ.currentMean() > 10;
        if(hasStartedMoving){
            physicModel.setStartPower(power);
            power += amountOverMovePower;
        }else{
            power += increasePowerRate;
        }
        //LOGGER.info("MY POWER: " + power);
    }
}
