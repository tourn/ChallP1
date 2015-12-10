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
    private int power;

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

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

    public Position getCarPosition() {
        return car;
    }

    public void setCar(Position car) {
        this.car = car;
    }

    public List<Track.Position> getCheckpoints(TrackSection section){
        List<Track.Position> checkpoints = new ArrayList<>();
        for(Track.Position checkpoint : this.getCheckpoints()){
            if(checkpoint.getSection().getId() == section.getId()){
                checkpoints.add(checkpoint);
            }
        }
        return checkpoints;
    }


    public static class Position{
        private TrackSection section;
        private long durationOffset;
        private double distanceOffset;
        private double percentage;
        private double velocity;

        public Position(TrackSection section, long durationOffset) {

            this.section = section;
            this.durationOffset = durationOffset;
            this.distanceOffset = 0;
            this.percentage = 0;
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

        public double getPercentage() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage = percentage;
        }

        public double getDistanceOffset() {
            return distanceOffset;
        }

        public void setDistanceOffset(double distanceOffset) {
            this.distanceOffset = distanceOffset;
        }

        public double getVelocity() {
            return velocity;
        }

        public void setVelocity(double velocity) {
            this.velocity = velocity;
        }
    }
}
