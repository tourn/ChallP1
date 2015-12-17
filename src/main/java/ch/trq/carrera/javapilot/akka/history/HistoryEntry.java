package ch.trq.carrera.javapilot.akka.history;

import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;

/**
 * Created by tourn on 17.12.15.
 */
public class HistoryEntry {
    private TrackSection section;
    private double brakePercentage;
    private long duration;
    private int power;
    private boolean penaltyOcurred = false;
    private boolean recoveringFromPenalty = false;
    private boolean powerIncreaseFrozen = false;

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

    public boolean isPenaltyOcurred() {
        return penaltyOcurred;
    }

    public void setPenaltyOcurred(boolean penaltyOcurred) {
        this.penaltyOcurred = penaltyOcurred;
    }

    public boolean isRecoveringFromPenalty() {
        return recoveringFromPenalty;
    }

    public void setRecoveringFromPenalty(boolean recoveringFromPenalty) {
        this.recoveringFromPenalty = recoveringFromPenalty;
    }

    public boolean isPowerIncreaseFrozen() {
        return powerIncreaseFrozen;
    }

    public void setPowerIncreaseFrozen(boolean powerIncreaseFrozen) {
        this.powerIncreaseFrozen = powerIncreaseFrozen;
    }
}
