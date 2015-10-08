package ch.trq.carrera.javapilot.akka.trackanalyzer;

/**
 * Created by Frank on 22.09.2015.
 */
public class TrackVelocity {
    private double velocity;
    private long timeStamp;

    public TrackVelocity(double velocity, long timeStamp){
        this.velocity = velocity;
        this.timeStamp = timeStamp;
    }

    public double getVelocity(){
        return velocity;
    }
    public long getTimeStamp(){
        return timeStamp;
    }

    public void setVelocity(double velocity) { this.velocity = velocity; }
    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }
}
