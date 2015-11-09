package ch.trq.carrera.javapilot.akka.trackanalyzer;

/**
 * Created by Frank on 22.09.2015.
 */
public class TrackSection {
    private State direction;
    /// Duration in ms
    private long duration=0;
    private long timeStamp;

    private double distance=0;

    private int id;

    private double friction;

    public TrackSection(State direction, long timeStamp){
        this.direction = direction;
        this.timeStamp = timeStamp;
    }
    public void setDirection(State direction){
        this.direction = direction;
    }
    public void setDuration(long duration){
        this.duration = duration;
    }
    public void setTimeStamp(long timeStamp){
        this.timeStamp = timeStamp;
    }

    public State getDirection(){
        return direction;
    }

    public long getDuration(){
        return duration;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public double getFriction() {
        return friction;
    }

    public void setFriction(double friction) {
        this.friction = friction;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void addDuration(long dDuration){
        duration += dDuration;
    }
}
