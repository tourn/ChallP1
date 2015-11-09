package ch.trq.carrera.javapilot.math;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 09.11.2015.
 */
public class PhysicModelCalculator {
    private final Logger LOGGER = LoggerFactory.getLogger(PhysicModelCalculator.class);
    private Track track;

    public PhysicModelCalculator(Track track){
        this.track = track;
    }

    public void calculateTrackPhysics(){
        List<List<Track.Position>> list = new ArrayList<>();
        for(int i = 0; i<track.getSections().size();i++){
            List<Track.Position> checkpointList = new ArrayList<>();
            for(Track.Position position : track.getCheckpoints()){
                if(position.getSection().getId()==i){
                    checkpointList.add(position);
                }
            }
            list.add(checkpointList);
        }
        straightsWithMoreThanOneVelocitySensor(list);
        turnsBetweenTwoVelocitySensors(list);
    }

    private void straightsWithMoreThanOneVelocitySensor(List<List<Track.Position>> list){
        int count = 0;
        for(List<Track.Position> poslist : list){
            if(poslist.size()>1){
               count++;
            }
        }
        LOGGER.info(count + " TrackSections with more than one Velocity-Sensor.");
    }
    private void turnsBetweenTwoVelocitySensors(List<List<Track.Position>> list){
        int id = -10;
        if(!list.get(list.size()-1).isEmpty()){
            Track.Position temp = list.get(list.size() - 1).get(list.get(list.size() - 1).size()-1);
            if (temp.getDurationOffset()==temp.getSection().getDuration()) {
                //id = list.get(list.size() - 1).get(list.get(list.size() - 1).size() - 1).getSection().getId();
                id = -1;
            }
        }
        int count = 0;
        for(List<Track.Position> poslist : list){
            if(!poslist.isEmpty()){

                if((poslist.get(0).getDurationOffset() == 0) && (id+2 == poslist.get(0).getSection().getId())){
                    count++;
                }
                if(poslist.get(poslist.size()-1).getSection().getDuration() == poslist.get(poslist.size()-1).getDurationOffset()){
                    id = poslist.get(poslist.size()-1).getSection().getId();
                }
            }
        }
        LOGGER.info(count + " TrackSections-Turns between two Velocity-Sensors.");
    }
}
