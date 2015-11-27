package ch.trq.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.trq.carrera.javapilot.akka.log.LogMessage;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Direction;
import ch.trq.carrera.javapilot.akka.trackanalyzer.PhysicLearnHelper;
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
    private PhysicLearnHelper physicLearnHelper;
    private final String actorDescription;

    private Direction turnDirection = Direction.STRAIGHT;
    private FloatingHistory gyroZ;
    private int currentPower;
    private boolean isMoving = false;
    private boolean trackRecognitionFinished = false;

    private int runPower;

    public TrackLearner(ActorRef pilot) {
        this.pilot = pilot;
        this.currentPower = START_POWER;
        gyroZ = new FloatingHistory(GYROZ_HISTORY_SIZE);
        actorDescription = getActorDescription();
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
            physicLearnHelper.handleVelocityMessage(message.getTimeStamp());
            switch(physicLearnHelper.state){
                case MOVE_TO_DESTINATION:
                    currentPower = runPower;
                    break;
                case BRAKE:
                    currentPower = 0;
                    break;
                case MEASURE:
                    currentPower = runPower;
                    break;
                case FINISHED:
                    double dv1 = message.getVelocity();
                    double v1 = dv1/2;
                    double t1 = physicLearnHelper.getMeasureTime();
                    double v2 = physicLearnHelper.getV2();
                    double dv2 = physicLearnHelper.getDV2();
                    double t2 = physicLearnHelper.getT2();
                    double p = (double)currentPower;
                    physicModelCalculator.calcConstE(v1, dv1, t1, v2, dv2, t2, p);
                    physicModelCalculator.calcFrictions();
                    physicModelCalculator.calculateDistances();
                    Track track = physicModelCalculator.getTrack();
                    //TODO: Carposition setzen? PhysikModel mitsenden bzw. nur der E wert
                    pilot.tell(track, ActorRef.noSender());
                    break;
            }
        }
    }

    private void handleSensorEvent(SensorEvent event) {
        LogMessage log = new LogMessage(event, System.currentTimeMillis());

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
            physicLearnHelper.handleTrackSectionMessage(event.getTimeStamp());
            switch(physicLearnHelper.state){
                case MOVE_TO_DESTINATION:
                    currentPower = runPower;
                    break;
                case BRAKE:
                    currentPower = 0;
                    break;
                case MEASURE:
                    currentPower = runPower;
                    break;
            }
        }
        pilot.tell(new PowerAction(currentPower), getSelf());

        populateLog(log);
        pilot.tell(log, ActorRef.noSender());
    }

    private void checkDirectionChange(SensorEvent event){
        switch (turnDirection) {
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
            turnDirection = Direction.RIGHT;
            trackAnalyzer.addTrackSection(turnDirection,timeStamp);
            //LOGGER.info("RIGHT TURN");
        }else if(-gyroZ.currentMean() > TURN_THRESHOLD){
            turnDirection = Direction.LEFT;
            trackAnalyzer.addTrackSection(turnDirection,timeStamp);
            //LOGGER.info("LEFT TURN");
        }
    }

    private void turnAction(long timeStamp) {
        if (Math.abs(gyroZ.currentMean()) < TURN_THRESHOLD) {
            turnDirection = Direction.STRAIGHT;
            trackAnalyzer.addTrackSection(turnDirection,timeStamp);
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
        runPower = currentPower;
        physicModelCalculator = new PhysicModelCalculator(track,physicModel);
        if(physicModelCalculator.hasStraightWithThreeCheckpoints()){
            physicModelCalculator.calculateTrackPhysics();
            physicModelCalculator.calculateDistances();
            LOGGER.info("Track built with distances");
            pilot.tell(track, ActorRef.noSender());
        }else{
            LOGGER.info("Track built without distances");
            LOGGER.info("Need to get another sensor in Tracksection");
            physicLearnHelper = new PhysicLearnHelper(track);
        }

        //pilot.tell(track, ActorRef.noSender());
    }

    private void populateLog(LogMessage log){
        log.setPower(currentPower);
        log.setActorDescription(actorDescription);
        log.settAfterCalculation(System.currentTimeMillis());

    }

    private String getActorDescription(){
        return String.format("TrackLearner power: %d, minStraightDuration: %d, minTurnDuration: %d",
                currentPower,
                MIN_STRAIGHT_DURATION,
                MIN_TURN_DURATION
        );
    }
}
