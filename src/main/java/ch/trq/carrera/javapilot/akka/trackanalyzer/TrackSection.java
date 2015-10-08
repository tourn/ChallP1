package ch.trq.carrera.javapilot.akka.trackanalyzer;

/**
 * Created by Frank on 22.09.2015.
 */
public class TrackSection {
    private String direction;
    /// Duration in ms
    private long duration=0;
    private long timeStamp;

    public TrackSection(String direction, long timeStamp){
        this.direction = direction;
        this.timeStamp = timeStamp;
    }
    public void setDirection(String direction){
        this.direction = direction;
    }
    public void setDuration(long duration){
        this.duration = duration;
    }
    public void setTimeStamp(long timeStamp){
        this.timeStamp = timeStamp;
    }

    public String getDirection(){
        return direction;
    }

    public long getDuration(){
        return duration;
    }

    public long getTimeStamp(){
        return timeStamp;
    }
}
