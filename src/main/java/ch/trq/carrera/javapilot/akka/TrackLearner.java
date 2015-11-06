package ch.trq.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
//import com.sun.tools.internal.jxc.ap.Const;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Round;
import ch.trq.carrera.javapilot.akka.trackanalyzer.State;
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

    private ThresholdConfiguration configuration;
    private int power;
    private ActorRef pilot;

    private final Logger LOGGER = LoggerFactory.getLogger(TrackLearner.class);

    private State state = State.STRAIGHT;
    private FloatingHistory gyroZ;/* = new FloatingHistory(8);*/
    private static double TURN_THRESHOLD = 1000;
    private static long previousTimestamp = 0;
    private static int roundCounter = 0;
    private static TrackAnalyzer trackAnalyzer = new TrackAnalyzer();
    private boolean finishLearning = false;

    // Parameter
    int startRoundNr;
    int faultyGoingStraightTime;
    int faultyTurnTime;
    int amountOfRounds;

    public TrackLearner(ActorRef pilot, int power, int startRoundNr, int amountOfRounds, int faultyGoingStraightTime, int faultyTurnTime, int floatingHistorySize) {
        this.pilot = pilot;
        this.power = power;
        this.startRoundNr = startRoundNr;
        this.amountOfRounds = amountOfRounds;
        this.faultyGoingStraightTime = faultyGoingStraightTime;
        this.faultyTurnTime = faultyTurnTime;
        gyroZ = new FloatingHistory(floatingHistorySize);
    }

    public static Props props ( ActorRef pilot, int power, int startRoundNr, int amountOfRounds, int faultyGoingStraightTime, int faultyTurnTime, int floatingHistorySize ) {
        return Props.create( TrackLearner.class, ()->new TrackLearner( pilot, power, startRoundNr, amountOfRounds, faultyGoingStraightTime, faultyTurnTime, floatingHistorySize));
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if ( message instanceof SensorEvent ) {
            handleSensorEvent((SensorEvent) message);
        } else if ( message instanceof VelocityMessage) {
            handleVelocityMessage((VelocityMessage) message);
        } else if ( message instanceof RoundTimeMessage) {
            handleRoundTimeMessage((RoundTimeMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void handleVelocityMessage(VelocityMessage message) {
        trackAnalyzer.addTrackVelocitiesToRound(message.getVelocity(),message.getTimeStamp());
    }

    private void handleRoundTimeMessage(RoundTimeMessage message) {
        trackAnalyzer.newRound(power);
        //trackAnalyzer.printLastRound();
        roundCounter += 1;
        if(roundCounter == startRoundNr+amountOfRounds) {
            finishLearning = true;
        } else {
            //just print
            //trackAnalyzer.calculateTrack();
        }
        //LOGGER.info("Round Nr. "+roundCounter);
    }

    long tempTimeStamp = 0;
    private void handleSensorEvent(SensorEvent event) {
        pilot.tell(new PowerAction(power), getSelf());
        gyroZ.shift(event.getG()[2]);
        if (event.getTimeStamp()-tempTimeStamp <1000)
            trackAnalyzer.updateDistance(event.getTimeStamp()-tempTimeStamp,power,state);
        tempTimeStamp = event.getTimeStamp();
        switch (state) {
            case STRAIGHT:
                if (Math.abs(gyroZ.currentMean()) > TURN_THRESHOLD) {
                    state = State.TURN;
                    trackAnalyzer.addTrackSectionToRound(State.TURN, event.getTimeStamp());
                    if(finishLearning){
                        pilot.tell(trackAnalyzer.calculateTrack(startRoundNr,faultyGoingStraightTime,faultyTurnTime), ActorRef.noSender());
                    }
                    //LOGGER.info("("+(event.getTimeStamp()-previousTimestamp)+"ms)");
                    //LOGGER.info("TURN");
                    previousTimestamp = event.getTimeStamp();
                }
                break;
            case TURN:
                if (Math.abs(gyroZ.currentMean()) < TURN_THRESHOLD) {
                    state = State.STRAIGHT;
                    trackAnalyzer.addTrackSectionToRound(State.STRAIGHT, event.getTimeStamp());
                    if(finishLearning){
                        pilot.tell(trackAnalyzer.calculateTrack(startRoundNr,faultyGoingStraightTime,faultyTurnTime), ActorRef.noSender());
                    }
                    //LOGGER.info("("+(event.getTimeStamp()-previousTimestamp)+"ms)");
                    //LOGGER.info("GOING STRAIGHT");
                    previousTimestamp = event.getTimeStamp();
                }
        }
    }
}
