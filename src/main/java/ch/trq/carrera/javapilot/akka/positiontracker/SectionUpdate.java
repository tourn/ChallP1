package ch.trq.carrera.javapilot.akka.positiontracker;

import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;

/**
 * Created by tourn on 22.10.15.
 */
public class SectionUpdate {
    private TrackSection section;
    private int powerInSection;
    private boolean penaltyOccured;

    public SectionUpdate(TrackSection section) {
        this.section = section;
    }

    public TrackSection getSection() {
        return section;
    }

    public void setSection(TrackSection section) {
        this.section = section;
    }

    public int getSectionIndex() {
        return section.getId();
    }

    public int getPowerInSection() {
        return powerInSection;
    }

    public void setPowerInSection(int powerInSection) {
        this.powerInSection = powerInSection;
    }

    public boolean isPenaltyOccured() {
        return penaltyOccured;
    }

    public void setPenaltyOccured(boolean penaltyOccured) {
        this.penaltyOccured = penaltyOccured;
    }
}
