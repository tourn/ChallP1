package ch.trq.carrera.javapilot.math;

import ch.trq.carrera.javapilot.akka.trackanalyzer.State;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
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
        calcFrictionForStraights(list);
        turnsBetweenTwoVelocitySensors(list);
        LOGGER.info("Startpower for moving: " + physicModel.getStartPower());
    }

    private void calcFrictionForStraights(List<List<Track.Position>> list){
        int count = 0;
        double dFriction = 0;
        for(int i = 0; i < list.size();i++){
            if(list.get(i).size()>1){
                count++;
                double friction = calcFriction(list.get(i).get(0).getVelocity(),list.get(i).get(1).getVelocity(),list.get(i).get(1).getDurationOffset()-list.get(i).get(0).getDurationOffset());
                track.getSections().get(i).setFriction(friction);
                dFriction+=friction;
            }
        }
        double friction = dFriction/(double)count;
        for(int i = 0; i < list.size();i++){
            if(track.getSections().get(i).getDirection()== State.STRAIGHT && list.get(i).size()<=1){
                track.getSections().get(i).setFriction(friction);
            }
        }
    }

    private double calcFriction(double v0, double v1, long t){
        double dv = v1-v0;
        double v = v0+dv;
        int p = track.getPower();
        double e = physicModel.getE();
        return ((p/v)*e*t-dv)/(physicModel.getG()*t);
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
        double dFriction = 0;
        for(List<Track.Position> poslist : list){
            if(!poslist.isEmpty()){

                if((poslist.get(0).getDurationOffset() == 0) && (id+2 == poslist.get(0).getSection().getId())){
                    count++;
                    v1 = poslist.get(0).getVelocity();
                    long t = track.getSections().get(id).getDuration();
                    double friction = calcFriction(v0, v1, t);
                    track.getSections().get(id).setFriction(friction);
                    dFriction+=friction;
                    showTurnBetweenToVelocitySensors(v0,v1,t);
                }
                if(poslist.get(poslist.size()-1).getSection().getDuration() == poslist.get(poslist.size()-1).getDurationOffset()){
                    id = poslist.get(poslist.size()-1).getSection().getId();
                    v0 = poslist.get(poslist.size()-1).getVelocity();
                }
            }
        }
        double friction = dFriction/(double)count;
        for(int i = 0; i < list.size();i++){
            if(track.getSections().get(i).getDirection()!= State.STRAIGHT && track.getSections().get(i).getFriction()==0.0){
                track.getSections().get(i).setFriction(friction);
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
            if(list.size()==3){
                calcConstEForPhysicModel(list);
            }
        }
    }

    private void calcConstEForPhysicModel(List<Track.Position> list){
        double dv1 = list.get(1).getVelocity()-list.get(0).getVelocity();
        double dv2 = list.get(2).getVelocity()-list.get(1).getVelocity();
        double t1 = (double)(list.get(1).getDurationOffset()-list.get(0).getDurationOffset())/1000.0;
        double t2 = (double)(list.get(2).getDurationOffset()-list.get(1).getDurationOffset())/1000.0;
        double v1 = list.get(0).getVelocity()+dv1/2;
        double v2 = list.get(1).getVelocity()+dv2/2;
        double p = (double)track.getPower();

        double e = ((dv2*t1-dv1*t2)*v1*v2)/(p*t1*t2*(v1-v2));

        physicModel.setE(e);
        LOGGER.info("Const e: " + e);
    }

    private void showTurnBetweenToVelocitySensors(double v0, double v1, long t){
        LOGGER.info("v0: " + v0 + "cm/s, v1: " + v1 + "cm/s, t: " + t + "ms, Power: " + track.getPower());
    }

    public void calculateDistances() {
        Track.Position position = getFirstCheckpointWithDurationOffsetZero();
        int posId = track.getCheckpoints().indexOf(position);
        int tsId = position.getSection().getId();
        double v = position.getVelocity();
        if(track.getCheckpoints().get((tsId+1)%track.getCheckpoints().size()).getSection().getId()==tsId){
            if(position.getSection().getDuration()==track.getCheckpoints().get((tsId + 1) % track.getCheckpoints().size()).getDurationOffset()){
                double dist=0;
                for(int i=0; i < position.getSection().getDuration();i++){
                    v=physicModel.getVelocity(v,position.getSection(),track.getPower(),1);
                    dist+=v*1.0/1000.0;
                }
                position.getSection().setDistance(dist);
            }
        }

    }

    private Track.Position getFirstCheckpointWithDurationOffsetZero() {
        for(Track.Position checkpoint : track.getCheckpoints()){
            if(checkpoint.getDurationOffset()==0){
                return checkpoint;
            }
        }
        return null;
    }
}
