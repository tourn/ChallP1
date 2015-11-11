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
    private PhysicModel physicModel;

    public PhysicModelCalculator(Track track, PhysicModel physicModel){
        this.track = track;
        this.physicModel = physicModel;
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
        LOGGER.info("Startpower for moving: " + physicModel.getStartPower());
    }

    private void straightsWithMoreThanOneVelocitySensor(List<List<Track.Position>> list){
        int count = 0;
        int i = 0;
        for(List<Track.Position> poslist : list){
            if(poslist.size()>1){
                count++;
                handleStraightWithMoreThanOneVelocitySensor(poslist,i);
            }
            i++;
        }
        LOGGER.info(count + " TrackSections with more than one Velocity-Sensor.");
    }

    private void turnsBetweenTwoVelocitySensors(List<List<Track.Position>> list){
        int id = -10;
        double v0 = 0,v1 = 0;
        if(!list.get(list.size()-1).isEmpty()){
            Track.Position temp = list.get(list.size() - 1).get(list.get(list.size() - 1).size()-1);
            if (temp.getDurationOffset()==temp.getSection().getDuration()) {
                //id = list.get(list.size() - 1).get(list.get(list.size() - 1).size() - 1).getSection().getId();
                id = -1;
                v0 = temp.getVelocity();
            }
        }
        int count = 0;
        for(List<Track.Position> poslist : list){
            if(!poslist.isEmpty()){

                if((poslist.get(0).getDurationOffset() == 0) && (id+2 == poslist.get(0).getSection().getId())){
                    count++;
                    v1 = poslist.get(0).getVelocity();
                    handleTurnBetweenToVelocitySensors(v0,v1,id+1);
                }
                if(poslist.get(poslist.size()-1).getSection().getDuration() == poslist.get(poslist.size()-1).getDurationOffset()){
                    id = poslist.get(poslist.size()-1).getSection().getId();
                    v0 = poslist.get(poslist.size()-1).getVelocity();
                }
            }
        }
        LOGGER.info(count + " TrackSections-Turns between two Velocity-Sensors.");
    }

    private void handleStraightWithMoreThanOneVelocitySensor(List<Track.Position> list,int id){
        if(list.size()==2){
            double v0 = list.get(0).getVelocity();
            double v1 = list.get(1).getVelocity();
            long t = list.get(1).getDurationOffset()-list.get(0).getDurationOffset();
            LOGGER.info("v0: " + v0 + "cm/s, v1: " + v1 + "cm/s, t: " + t + "ms, Power: " + track.getPower());
        }else{
            String s = "";
            for(int i = 0; i < list.size();i++){
                s+="v"+i+": "+list.get(i).getVelocity()+"cm/s ("+list.get(i).getDurationOffset()+"ms), ";
            }
            s+="Power: "+track.getPower();
            LOGGER.info(s);
            //LOGGER.info("STRAIGHT WITH MORE THAN TWO VELOCITY-SENSORS ... NOT HANDLED ATM!!!");
        }
    }

    private void handleTurnBetweenToVelocitySensors(double v0, double v1, int id){
        long t = track.getSections().get(id).getDuration();
        LOGGER.info("v0: " + v0 + "cm/s, v1: " + v1 + "cm/s, t: " + t + "ms, Power: " + track.getPower());
    }
}
