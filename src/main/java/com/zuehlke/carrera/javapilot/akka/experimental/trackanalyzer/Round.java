package com.zuehlke.carrera.javapilot.akka.experimental.trackanalyzer;

import java.util.ArrayList;

/**
 * Created by Frank on 22.09.2015.
 */
public class Round {

    private ArrayList<TrackSection> trackSections;
    private ArrayList<TrackVelocity> trackVelocities;
    private long startRoundTimeStamp;
    private long endRoundTimeStamp;

    public Round(long startRoundTimeStamp){
        this.startRoundTimeStamp = startRoundTimeStamp;
        trackSections = new ArrayList<TrackSection>();
        trackVelocities = new ArrayList<TrackVelocity>();
    }

    public void addTrackSection(TrackSection trackSection){
        trackSections.add(trackSection);
    }

    public void addTrackVelocity(TrackVelocity trackVelocity){
        trackVelocities.add(trackVelocity);
    }

    public void setEndRoundTimeStamp(long endRoundTimeStamp){
        this.endRoundTimeStamp = endRoundTimeStamp;
    }

    public ArrayList<TrackSection> getTrackSections(){
        return trackSections;
    }

    public ArrayList<TrackVelocity> getTrackVelocites(){
        return trackVelocities;
    }

    public long getRoundTime(){
        return endRoundTimeStamp-startRoundTimeStamp;
    }

    public int getCountOfTrackSections(){
        return trackSections.size();
    }

    public int getCountOfTrackVelocities(){
        return trackVelocities.size();
    }
}
