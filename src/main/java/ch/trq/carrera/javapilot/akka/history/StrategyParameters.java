package ch.trq.carrera.javapilot.akka.history;

import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;

/**
 * Created by tourn on 17.12.15.
 */
public class StrategyParameters {
    private TrackSection section;
    private double brakePercentage;
    private long duration;
    private int power;
    private boolean penaltyOccurred = false;
    private boolean valid = true;
    private int powerIncrement = 10;

    public TrackSection getSection() {
        return section;
    }

    public void setSection(TrackSection section) {
        this.section = section;
    }

    public double getBrakePercentage() {
        return brakePercentage;
    }

    public void setBrakePercentage(double brakePercentage) {
        this.brakePercentage = brakePercentage;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public boolean isPenaltyOccurred() {
        return penaltyOccurred;
    }

    public void setPenaltyOccurred(boolean penaltyOccurred) {
        this.penaltyOccurred = penaltyOccurred;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getPowerIncrement() {
        return powerIncrement;
    }

    public void setPowerIncrement(int powerIncrement) {
        this.powerIncrement = powerIncrement;
    }
}
