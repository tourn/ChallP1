package ch.trq.carrera.javapilot.math;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackVelocity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by Frank on 09.11.2015.
 */
public class PhysicModel {

    private int startPower;

    public PhysicModel(){

    }

    public int getStartPower() {
        return startPower;
    }

    public void setStartPower(int startPower) {
        this.startPower = startPower;
    }


}
