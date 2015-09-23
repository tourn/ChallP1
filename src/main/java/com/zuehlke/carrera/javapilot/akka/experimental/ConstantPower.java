package com.zuehlke.carrera.javapilot.akka.experimental;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
//import com.sun.tools.internal.jxc.ap.Const;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.javapilot.akka.experimental.trackanalyzer.TrackAnalyzer;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.carrera.timeseries.FloatingHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  A very simple actor that determines the power value by a configurable Threshold on any of the 10 observables
 */
public class ConstantPower extends UntypedActor {

    private ThresholdConfiguration configuration;
    private int power;
    private ActorRef pilot;

    private final Logger LOGGER = LoggerFactory.getLogger(ConstantPower.class);

    enum State{
        STRAIGHT,
        TURN_LEFT,
        TURN_RIGHT
    }

    private State state = State.STRAIGHT;
    private FloatingHistory gyroZ = new FloatingHistory(8);
    private static double TURN_THRESHOLD = 1000;
    private static long previousTimestamp = 0;
    private static int roundCounter = 0;
    private static TrackAnalyzer trackAnalyzer = new TrackAnalyzer();

    public ConstantPower(ActorRef pilot, int power) {
        this.pilot = pilot;
        this.power = power;
    }

    public static Props props ( ActorRef pilot, int power ) {
        return Props.create( ConstantPower.class, ()->new ConstantPower( pilot, power ));
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
        trackAnalyzer.newRound(message.getTimestamp());
        trackAnalyzer.printRound(roundCounter);
        roundCounter++;
        //LOGGER.info("Round Nr. "+roundCounter);
    }

    private void handleSensorEvent(SensorEvent event) {
        pilot.tell(new PowerAction(power), getSelf());

        gyroZ.shift(event.getG()[2]);
        switch(state){
            case STRAIGHT:
                if(gyroZ.currentMean() > TURN_THRESHOLD){
                    state = State.TURN_RIGHT;
                    trackAnalyzer.addTrackSectionToRound("RIGHT TURN", event.getTimeStamp());
                    LOGGER.info("("+(event.getTimeStamp()-previousTimestamp)+"ms)");
                    LOGGER.info("RIGHT TURN");
                    previousTimestamp = event.getTimeStamp();
                } else if( Math.abs(gyroZ.currentMean()) > TURN_THRESHOLD){
                    state = State.TURN_LEFT;
                    trackAnalyzer.addTrackSectionToRound("LEFT TURN", event.getTimeStamp());
                    LOGGER.info("("+(event.getTimeStamp()-previousTimestamp)+"ms)");
                    LOGGER.info("LEFT TURN");
                    previousTimestamp = event.getTimeStamp();
                }
                break;
            case TURN_LEFT:
            case TURN_RIGHT:
                if(Math.abs(gyroZ.currentMean()) < TURN_THRESHOLD){
                    state = State.STRAIGHT;
                    trackAnalyzer.addTrackSectionToRound("GOING STRAIGHT", event.getTimeStamp());
                    LOGGER.info("("+(event.getTimeStamp()-previousTimestamp)+"ms)");
                    LOGGER.info("GOING STRAIGHT");
                    previousTimestamp = event.getTimeStamp();
                }
        }
    }
}
