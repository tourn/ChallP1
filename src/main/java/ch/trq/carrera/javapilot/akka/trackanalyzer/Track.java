package ch.trq.carrera.javapilot.akka.trackanalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tourn on 8.10.15.
 */
public class Track {
    private List<TrackSection> sections = new ArrayList<>();
    private List<Position> checkpoints = new ArrayList<>();
    private Position car = null;

    public List<TrackSection> getSections() {
        return sections;
    }

    public void setSections(List<TrackSection> sections) {
        this.sections = sections;
    }

    public List<Position> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(List<Position> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public Position getCar() {
        return car;
    }

    public void setCar(Position car) {
        this.car = car;
    }

    public static class Position{
        private TrackSection section;
        private long durationOffset;

        public Position(TrackSection section, long durationOffset) {

            this.section = section;
            this.durationOffset = durationOffset;
        }

        public TrackSection getSection() {
            return section;
        }

        public void setSection(TrackSection section) {
            this.section = section;
        }

        public long getDurationOffset() {
            return durationOffset;
        }

        public void setDurationOffset(long durationOffset) {
            this.durationOffset = durationOffset;
        }
    }
}
