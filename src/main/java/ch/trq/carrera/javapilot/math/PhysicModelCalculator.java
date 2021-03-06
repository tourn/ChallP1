package ch.trq.carrera.javapilot.math;

import ch.trq.carrera.javapilot.trackanalyzer.Direction;
import ch.trq.carrera.javapilot.trackanalyzer.Track;
import ch.trq.carrera.javapilot.trackanalyzer.TrackSection;
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

    public Track getTrack(){
        return track;
    }
    public PhysicModel getPhysicModel(){
        return physicModel;
    }

    public boolean hasStraightWithThreeCheckpoints(){
        for(int i = 0; i<track.getSections().size();i++){
            List<Track.Position> checkpointList = new ArrayList<>();
            for(Track.Position position : track.getCheckpoints()){
                if(position.getSection().getId()==i){
                    checkpointList.add(position);
                }
            }
            if(checkpointList.size()>=3){
                return true;
            }
        }
        return false;
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
        calcFrictions();
        /*calcFrictionForStraights(list);
        turnsBetweenTwoVelocitySensors(list);*/
        LOGGER.info("Startpower for moving: " + physicModel.getStartPower());
    }

    public void calcFrictions(){
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
        calcFrictionForStraights(list);
        turnsBetweenTwoVelocitySensors(list);
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
            if(track.getSections().get(i).getDirection()== Direction.STRAIGHT && list.get(i).size()<=1){
                track.getSections().get(i).setFriction(friction);
            }
        }
    }

    private double calcFriction(double v0, double v1, long t){
        double dv = v1-v0;
        double v = v0+dv;
        int p = track.getLearningPower();
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
                    long t = track.getSections().get(id+1).getDuration();
                    double friction = calcFriction(v0, v1, t);
                    track.getSections().get(id+1).setFriction(friction);
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
            if(track.getSections().get(i).getDirection()!= Direction.STRAIGHT && track.getSections().get(i).getFriction()==0.0){
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
            LOGGER.info("v0: " + v0 + "cm/s, v1: " + v1 + "cm/s, t: " + t + "ms, Power: " + track.getLearningPower());
        }else{
            String s = "";
            for(int i = 0; i < list.size();i++){
                s+="v"+i+": "+list.get(i).getVelocity()+"cm/s ("+list.get(i).getDurationOffset()+"ms), ";
            }
            s+="Power: "+track.getLearningPower();
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
        double p = (double)track.getLearningPower();

        calcConstE(v1,dv1,t1,v2,dv2,t2,p);

    }

    public void calcConstE(double v1,double dv1, double t1,double v2,double dv2, double t2, double p){
        double e = ((dv2*t1-dv1*t2)*v1*v2)/(p*t1*t2*(v1-v2));

        physicModel.setE(e);
        LOGGER.info("Const e: " + e);
    }

    private void showTurnBetweenToVelocitySensors(double v0, double v1, long t){
        LOGGER.info("v0: " + v0 + "cm/s, v1: " + v1 + "cm/s, t: " + t + "ms, Power: " + track.getLearningPower());
    }

    private DistanceCalculator distanceCalculator;
    public void calculateDistances() {
        distanceCalculator = new DistanceCalculator();
        distanceCalculator.calculateTrackModel();
    }

    public double getV(){
        return distanceCalculator.getV();
    }

    private class DistanceCalculator {
        private double v;
        public double getV(){
            return v;
        }

        private void calculateTrackModel() {
            Track.Position rootCheckpoint = getFirstCheckpointWithDurationOffsetZero();
            TrackSection rootTrackSection = rootCheckpoint.getSection();
            v = rootCheckpoint.getVelocity();

            for (int i = rootTrackSection.getId(); i < track.getSections().size() + rootTrackSection.getId(); i++) {
                int sectionIndex = i % track.getSections().size();
                TrackSection currentSection = track.getSections().get(sectionIndex);

                calculateTrackSection(currentSection);
            }
            for(Track.Position p : track.getCheckpoints()){
                p.setPercentage(p.getDistanceOffset()/p.getSection().getDistance());
            }
        }


        private void calculateTrackSection(TrackSection section) {
            List<Track.Position> checkpoints = track.getCheckpoints(section);
            long t_offset = 0;
            double distance = 0;
            for (Track.Position checkpoint : checkpoints) {
                distance += calculateSubsection(t_offset, checkpoint.getDurationOffset(), section);
                t_offset = checkpoint.getDurationOffset();
                checkpoint.setDistanceOffset(distance);
                v = checkpoint.getVelocity(); //adjust velocity with measured value
            }
            distance += calculateSubsection(t_offset, section.getDuration(), section);
            section.setDistance(distance);
        }

        private double calculateSubsection(long t_from, long t_to, TrackSection section) {
            double distance = 0;
            for (long t = t_from; t < t_to; t++) {
                v = physicModel.getVelocity(v, section, track.getLearningPower(), 1);
                distance += v * 1.0 / 1000.0;
            }
            return distance;
        }

        private Track.Position getFirstCheckpointWithDurationOffsetZero() {
            for(Track.Position checkpoint : track.getCheckpoints()){
                if(checkpoint.getDurationOffset()==0){
                    return checkpoint;
                }
            }
            //the current calculation can't handle this. This could be fixed by calculating backwards in a tracksection
            return null;
        }
    }


}
