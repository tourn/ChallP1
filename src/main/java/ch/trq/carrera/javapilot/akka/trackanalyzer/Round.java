package ch.trq.carrera.javapilot.akka.trackanalyzer;

import java.util.ArrayList;

/**
 * Created by Frank on 22.09.2015.
 */
public class Round {

    private ArrayList<TrackSection> trackSections;
    private ArrayList<TrackVelocity> trackVelocities;
    private long startRoundTimeStamp;
    private long endRoundTimeStamp;
    private int pilotPower;

    public Round(long startRoundTimeStamp,int pilotPower){
        this.startRoundTimeStamp = startRoundTimeStamp;
        this.pilotPower = pilotPower;
        trackSections = new ArrayList<TrackSection>();
        trackVelocities = new ArrayList<TrackVelocity>();
    }

    public void addTrackSection(TrackSection trackSection){
        trackSections.add(trackSection);
    }
    public void addTrackSection(String direction, long timeStamp){
        trackSections.add(new TrackSection(direction,timeStamp));
    }

    public void addTrackVelocity(TrackVelocity trackVelocity){
        trackVelocities.add(trackVelocity);
    }
    public void addTrackVelocity(double velocity, long timeStamp) { trackVelocities.add(new TrackVelocity(velocity,timeStamp)); }

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

    public long getStartRoundTimeStamp(){
        return startRoundTimeStamp;
    }

    public long getEndRoundTimeStamp() { return endRoundTimeStamp; }

    public int getCountOfTrackSections(){
        return trackSections.size();
    }

    public int getCountOfTrackVelocities(){
        return trackVelocities.size();
    }

    public int getPilotPower() { return pilotPower; }
}
