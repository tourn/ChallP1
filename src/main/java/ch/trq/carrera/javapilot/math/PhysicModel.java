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

    private double g = 981.0;

    private double e;

    public PhysicModel(){

    }

    public int getStartPower() {
        return startPower;
    }

    public void setStartPower(int startPower) {
        this.startPower = startPower;
    }

    public void setE(double e){
        this.e = e;
    }

    public double getE(){
        return e;
    }

    public double getG(){
        return g;
    }

    // Calculation

    /**
     * @param v0 the speed at the beginning of the period in cm/s
     * @param trackSection the TrackSection
     * @param power  the digital power value at the start of the period
     * @param time timespan in ms
     * @return the velocity after the time period
     */
    public double getVelocity(double v0, TrackSection trackSection, int power, long time){
        for(int i = 0; i < time; i++){ // for each ms
            v0 += acceleration(v0,trackSection,power) * 1.0/1000.0;
        }
        return v0;
    }

    /**
     * @param v0 the speed at the beginning of the period in cm/s
     * @param trackSection the TrackSection
     * @param power  the digital power value at the start of the period
     * @return the acceleration in cm/s^2
     */
    public double acceleration(double v0, TrackSection trackSection, int power) {
        // F_tot = F_p - F_r
        return getEngineForce(v0,power) - trackSection.getFriction()*g;
    }

    public double getEngineForce(double v0, int power){
        return power*e/(v0);
    }



    /**
     * TODO may nod be used
     *
     * @param v0 the speed at the beginning of the period in cm/s
     * @param trackSection the TrackSection
     * @param power  the digital power value at the start of the period
     * @param time timespan in ms
     * @return the Distance over the period
     */
    public double getDistance(double v0, TrackSection trackSection, int power, long time){
        double distance = 0;
        for(int i = 0; i < time; i++){
            //v0 = ...
            distance += v0*1/1000;
        }
        return distance;
    }

}
