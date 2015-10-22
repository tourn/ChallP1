package ch.trq.carrera.javapilot.akka.positiontracker;

import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;

/**
 * Created by tourn on 22.10.15.
 */
public class SectionUpdate {
    private TrackSection section;
    private int sectionIndex;

    public SectionUpdate(TrackSection section, int sectionIndex) {
        this.section = section;
        this.sectionIndex = sectionIndex;
    }

    public TrackSection getSection() {
        return section;
    }

    public void setSection(TrackSection section) {
        this.section = section;
    }

    public int getSectionIndex() {
        return sectionIndex;
    }

    public void setSectionIndex(int sectionIndex) {
        this.sectionIndex = sectionIndex;
    }
}
