package ch.trq.carrera.javapilot.akka.trackanalyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 26.11.2015.
 */
public class PhysicLearnHelper {

    private Track track;

    private long startTimeStamp = -1;
    private long timeFromStartToDestination;
    private int destinationTsId;
    private int destinationCpId;

    private int passedVelocityId = -1;

    public boolean isAtDestination = false;

    public PhysicLearnHelper(Track track){
        this.track = track;
        startMoveToFirstTrackSectionWithTwoVelocitySensors();
    }

    private void startMoveToFirstTrackSectionWithTwoVelocitySensors(){
        destinationTsId = getFirstTrackSectionIdWithTwoVelocitySensors();
        destinationCpId = getCheckpointDestinationId();
        timeFromStartToDestination = track.getSections().get(destinationTsId).getTimeStamp();
    }

    public void handleVelocityMessage(){
        passedVelocityId++;
        if(passedVelocityId==destinationCpId){
            isAtDestination = true;
        }
    }

    public void handleTrackSectionMessage(long timeStamp){
        if(startTimeStamp==-1){
            startTimeStamp = timeStamp;
        }else if(timeStamp-startTimeStamp>timeFromStartToDestination){
            isAtDestination = true;
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
}
