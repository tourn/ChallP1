package ch.trq.carrera.javapilot.akka.trackanalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 26.11.2015.
 */
public class PhysicLearnHelper {

    private Track track;

    private static final int WAIT_TIME_FOR_BRAKE = 3000;
    private static final int TIME_TO_DRIVE_FOR_MEASURE = 1500;

    private long startTimeStamp = -1;
    private long timeFromStartToDestination;
    private long startBreakTimeStamp = -1;
    private long startMeasureTimeStamp = -1;
    private long reachCheckPointTimeStamp = -1;
    private int destinationTsId;
    private int destinationCpId;

    private int passedVelocityId = -1;

    private long measureTime = 0;

    public boolean isAtDestination = false;

    public State state;

    public PhysicLearnHelper(Track track){
        this.track = track;
        startMoveToFirstTrackSectionWithTwoVelocitySensors();
    }

    private void startMoveToFirstTrackSectionWithTwoVelocitySensors(){
        destinationTsId = getFirstTrackSectionIdWithTwoVelocitySensors();
        destinationCpId = getCheckpointDestinationId();
        timeFromStartToDestination = track.getSections().get(destinationTsId).getTimeStamp();
        state = State.MOVE_TO_CHECKPOINTDESTINATION;
    }

    public double getMeasureTime(){
        return (double)measureTime;
    }
    public double getV2(){
        return track.getCheckpoints().get(destinationCpId).getVelocity();
    }
    public double getDV2(){
        return track.getCheckpoints().get(destinationCpId+1).getVelocity()-track.getCheckpoints().get(destinationCpId).getVelocity();
    }
    public double getT2(){
        return track.getCheckpoints().get(destinationCpId+1).getDurationOffset()-track.getCheckpoints().get(destinationCpId).getDurationOffset();
    }

    public void handleVelocityMessage(long timeStamp){
        switch(state){
            case MOVE_TO_CHECKPOINTDESTINATION:
                passedVelocityId++;
                if(passedVelocityId==destinationCpId){
                    //isAtDestination = true;
                    if(track.getCheckpoints().get(destinationCpId).getSection().getDuration()>1000){
                        state = State.MOVE_TO_HALF_TRACKSECTION;
                    }else{
                        isAtDestination = true;
                        state = State.BRAKE;
                    }
                }
                break;
            case MEASURE:
                state = State.FINISHED;
                measureTime = timeStamp-startMeasureTimeStamp;
                break;
            default:
                break;
        }
    }

    public void handleTrackSectionMessage(long timeStamp){
        switch(state){
            case MOVE_TO_HALF_TRACKSECTION:
                if(reachCheckPointTimeStamp==-1){
                    reachCheckPointTimeStamp = timeStamp;
                }else if(track.getCheckpoints().get(destinationCpId).getSection().getDuration()-TIME_TO_DRIVE_FOR_MEASURE<timeStamp-reachCheckPointTimeStamp){
                    isAtDestination = true;
                    state = State.BRAKE;
                }
                break;
            case BRAKE:
                if(startBreakTimeStamp==-1){
                    startBreakTimeStamp = timeStamp;
                }else if(timeStamp-startBreakTimeStamp>WAIT_TIME_FOR_BRAKE){
                    startMeasureTimeStamp = timeStamp;
                    state = State.MEASURE;
                }
                break;
        }
    }

    private int getFirstTrackSectionIdWithTwoVelocitySensors(){
        for(int i = 0; i<track.getSections().size();i++){
            List<Track.Position> checkpointList = new ArrayList<>();
            for(Track.Position position : track.getCheckpoints()){
                if(position.getSection().getId()==i){
                    checkpointList.add(position);
                }
            }
            if(checkpointList.size()==2){
                return i;
            }
        }
        return -1;
    }

    private int getCheckpointDestinationId(){
        for(int i = 0; i < track.getCheckpoints().size();i++){
            if(track.getCheckpoints().get(i).getSection().getId()==destinationTsId){
                return i;
            }
        }
        return -1;
    }

    public enum State{
        MOVE_TO_CHECKPOINTDESTINATION,
        MOVE_TO_HALF_TRACKSECTION,
        BRAKE,
        MEASURE,
        FINISHED
    }

    public Track.Position GetCarPositionOnTrack(){
        return new Track.Position(track.getSections().get((destinationTsId+1)%track.getSections().size()),0);
    }
}
