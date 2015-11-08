package ch.trq.carrera.javapilot.akka.trackanalyzer;

import ch.trq.carrera.javapilot.math.TrackPhysicsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Created by Frank on 22.09.2015.
 */
public class TrackAnalyzer {
    private List<TrackSection> trackSections = new ArrayList<>();
    private List<TrackVelocity> trackVelocities = new ArrayList<>();
    private TrackSection tempTrackSection;
    private final Logger LOGGER = LoggerFactory.getLogger(TrackAnalyzer.class);

    private TrackRecognitionCallback onTrackRecognized;

    private int faultyGoingStraightTime = 200;
    private int faultyTurnTime = 250;
    private int ignoredTrackSections = 3;
    private int trackVelocitiesPerRound = 4;
    private int minTrackSectionsPerRound = 4;


    public TrackAnalyzer(){

    }

    /**
     *
     *
     * @param velocity The velocity from the velocity-sensor-message
     * @param timeStamp The received time-stamp from the velocity-sensor-message
     */
    public void addTrackVelocity(double velocity, long timeStamp){
        trackVelocities.add(new TrackVelocity(velocity, timeStamp));
    }


    public void addTrackSection(State state, long timeStamp){
        if(tempTrackSection != null){
            tempTrackSection.setDuration(timeStamp - tempTrackSection.getTimeStamp());
            switch(tempTrackSection.getDirection()){
                case STRAIGHT:
                    if(tempTrackSection.getDuration()<faultyGoingStraightTime){
                        if(getLastTrackSection()!=null){
                            getLastTrackSection().addDuration(tempTrackSection.getDuration());
                        }
                    }else if(getLastTrackSection()!=null && getLastTrackSection().getDirection()==State.STRAIGHT){
                        getLastTrackSection().addDuration(tempTrackSection.getDuration());
                    }else{
                        trackSections.add(tempTrackSection);
                    }
                    break;
                case LEFT:
                case RIGHT:
                    if(tempTrackSection.getDuration()<faultyTurnTime){
                        if(getLastTrackSection()!=null){
                            getLastTrackSection().addDuration(tempTrackSection.getDuration());
                        }
                    }else if(getLastTrackSection()!=null && (getLastTrackSection().getDirection()==State.LEFT || getLastTrackSection().getDirection()==State.RIGHT)){
                        getLastTrackSection().addDuration(tempTrackSection.getDuration());
                    }else{
                        trackSections.add(tempTrackSection);
                    }
                    break;
            }
        }
        tempTrackSection = new TrackSection(state,timeStamp);
        tryToFindRoundCycle();
        //printTrackSections();
    }

    private void tryToFindRoundCycle(){
        if(foundTrackCycle()){
            if(onTrackRecognized != null){
                Track track = buildTrack();
                onTrackRecognized.onTrackRecognized(track);
            }
            LOGGER.info("FOUND TRACK CYCLE: " + (trackSections.size()-ignoredTrackSections)/2 + " sections");
            printTrackSections();
        }
    }

    private boolean foundTrackCycle(){
        if(trackSections.size()>=ignoredTrackSections+2*minTrackSectionsPerRound && (trackSections.size()-ignoredTrackSections)%2==0){
            int halfTrackSectionCount = (trackSections.size()-ignoredTrackSections)/2;
            for(int i = ignoredTrackSections; i < ignoredTrackSections+halfTrackSectionCount; i++){
                if(trackSections.get(i).getDirection()!=trackSections.get(i+halfTrackSectionCount).getDirection()){
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }

    private TrackSection getLastTrackSection(){
        if(trackSections.size()==0){
            return null;
        }
        return trackSections.get(trackSections.size()-1);
    }

    private Track buildTrack(){
        removeIgnoredTrackSections();
        removeIgnoredTrackVelocities();
        Track track = new Track();
        track.getSections().addAll(getAveragesTrackSectionData());
        return track;
    }

    private void removeIgnoredTrackSections(){
        for(int i = 0; i<ignoredTrackSections; i++){
            trackSections.remove(0);
        }
    }

    private void removeIgnoredTrackVelocities(){
        while(trackVelocities.get(0).getTimeStamp()<trackSections.get(0).getTimeStamp()){
            trackVelocities.remove(0);
        }
    }

    private List<TrackSection> getAveragesTrackSectionData(){
        List<TrackSection> list = new ArrayList<>();
        int trackSectionsPerRound = trackSections.size()/2;
        for(int i = 0; i < trackSectionsPerRound; i++){
            long timeStamp;
            if(i==0){
                timeStamp = 0;
            }else{
                timeStamp = list.get(i-1).getTimeStamp()+list.get(i-1).getDuration();
            }
            State state = trackSections.get(i).getDirection();
            TrackSection trackSection = new TrackSection(state,timeStamp);
            trackSection.setDuration((trackSections.get(i).getDuration()+trackSections.get(i+trackSectionsPerRound).getDuration())/2);
            list.add(trackSection);
        }
        return list;
    }

    public static abstract class TrackRecognitionCallback{
        public abstract void onTrackRecognized(Track track);
    }

    public void setOnTrackRecognized(TrackRecognitionCallback onTrackRecognized){
        this.onTrackRecognized = onTrackRecognized;
    }

    private void printTrackSections(){
        String s="";
        for(TrackSection trackSection : trackSections){
            switch(trackSection.getDirection()){
                case STRAIGHT:
                    s+="S";
                    break;
                case LEFT:
                    s+="L";
                    break;
                case RIGHT:
                    s+="R";
                    break;
            }
        }
        LOGGER.info("\n"+s);
    }
}
