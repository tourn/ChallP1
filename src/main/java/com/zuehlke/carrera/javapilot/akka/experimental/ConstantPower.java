package com.zuehlke.carrera.javapilot.akka.experimental;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.sun.tools.internal.jxc.ap.Const;
import com.zuehlke.carrera.javapilot.akka.JavaPilotActor;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
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
        } else {
            unhandled(message);
        }
    }

    private void handleVelocityMessage(VelocityMessage message) {
        // ignore for now
    }

    private void handleSensorEvent(SensorEvent event) {
        pilot.tell(new PowerAction(power), getSelf());

        gyroZ.shift(event.getG()[2]);
        switch(state){
            case STRAIGHT:
                if(gyroZ.currentMean() > TURN_THRESHOLD){
                    state = State.TURN_RIGHT;
                    LOGGER.info("RIGHT TURN");
                } else if( Math.abs(gyroZ.currentMean()) > TURN_THRESHOLD){
                    state = State.TURN_LEFT;
                    LOGGER.info("LEFT TURN");
                }
                break;
            case TURN_LEFT:
            case TURN_RIGHT:
                if(Math.abs(gyroZ.currentMean()) < TURN_THRESHOLD){
                    state = State.STRAIGHT;
                    LOGGER.info("GOING STRAIGHT");
                }
        }
    }
}
