package com.zuehlke.carrera.javapilot.akka.experimental;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.timeseries.FloatingHistory;
import org.apache.commons.lang.StringUtils;

/**
 *  this logic node increases the power level by 10 units per 0.5 second until it receives a penalty
 *  then reduces by ten units.
 */
public class PowerUpUntilPenalty extends UntypedActor {

    private final ActorRef kobayashi;

    private int currentPower = 0;

    private long lastIncrease;

    private int maxPower = 180; // Max for this phase;

    private boolean probing = true;

    private FloatingHistory gyrozHistory = new FloatingHistory(8);

    /**
     * @param kobayashi The central pilot actor = kobayashi
     * @param duration the period between two increases
     * @return the actor props
     */
    public static Props props( ActorRef kobayashi, int duration ) {
        return Props.create(
                PowerUpUntilPenalty.class, () -> new PowerUpUntilPenalty( kobayashi, duration ));
    }
    private final int duration;

    public PowerUpUntilPenalty(ActorRef kobayashi, int duration) {
        lastIncrease = System.currentTimeMillis();
        this.kobayashi = kobayashi;
        this.duration = duration;
    }


    @Override
    public void onReceive(Object message) throws Exception {

        if ( message instanceof SensorEvent ) {
            handleSensorEvent((SensorEvent) message);

        } else if ( message instanceof PenaltyMessage) {
            handlePenaltyMessage ( (PenaltyMessage) message );

        } else {
            unhandled(message);
        }
    }

    private void handlePenaltyMessage(PenaltyMessage message) {

        currentPower -= 10;
        kobayashi.tell(new PowerAction(currentPower), getSelf());
        probing = false;
    }

    /**
     * Strategy: increase without
     * @param message
     */
    private void handleSensorEvent(SensorEvent message) {

        gyrozHistory.shift(message.getG()[2]);
        // show (gyrz);

        if (probing) {
            if (iAmStillStanding()) {
                increase(5);
            } else if (message.getTimeStamp() > lastIncrease + duration) {
                lastIncrease = message.getTimeStamp();
                increase(3);
            }
        }

        kobayashi.tell(new PowerAction(currentPower), getSelf());
    }

    private int increase ( int val ) {
        currentPower = Math.min ( currentPower + val, maxPower );
        return currentPower;
    }

    private boolean iAmStillStanding() {
        return gyrozHistory.currentStDev() < 3;
    }

    private void show(int gyr2) {
        int scale = 120 * (gyr2 - (-10000) ) / 20000;
        System.out.println(StringUtils.repeat(" ", scale) + gyr2);
    }


}
