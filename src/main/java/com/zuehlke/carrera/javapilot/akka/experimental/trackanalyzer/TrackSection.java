package com.zuehlke.carrera.javapilot.akka.experimental.trackanalyzer;

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
    public void setDuration(long duration){
        this.duration = duration;
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
