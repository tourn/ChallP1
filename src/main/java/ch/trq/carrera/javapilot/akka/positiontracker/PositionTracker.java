package ch.trq.carrera.javapilot.akka.positiontracker;

import ch.trq.carrera.javapilot.akka.trackanalyzer.State;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import ch.trq.carrera.javapilot.math.TrackPhysicsModel;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.carrera.timeseries.FloatingHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tourn on 22.10.15.
 */
public class PositionTracker {
    private final Track track;
    private long tLastUpdate = -1;
    private UpdateCallback onUpdate;
    private SectionChangeCallback onSectionChange;
    private Track.Position pos;
    private int sectionIndex;
    private FloatingHistory gyroZ = new FloatingHistory(8);
    private static double TURN_THRESHOLD = 1500;
    private final Logger LOGGER = LoggerFactory.getLogger(PositionTracker.class);
    private int power=0;

    private double sectionPercentage=0;
    private int velocityPositionId;
    private long roundStartTimeStamp;
    private long rountTime;

    private TrackPhysicsModel trackPhysicsModel = new TrackPhysicsModel();
    double calculatedVelocity = 0;

    public PositionTracker(Track track){
        this.track = track;
        pos = new Track.Position(track.getSections().get(0), 0);
        sectionIndex = 0;
        velocityPositionId = -1;
    }

    private double calculateDistance(long dtime, State turn){
        double distance = 0;
        for(int i = 0; i<dtime;i++){
            calculatedVelocity = trackPhysicsModel.average_velocity(calculatedVelocity, turn, power, 1.0/1000.0);
            distance += calculatedVelocity * 1.0/1000.0;
        }
        return distance;
    }

    public void update(SensorEvent e){
        gyroZ.shift(e.getG()[2]);

        if(tLastUpdate == -1){
            tLastUpdate = e.getTimeStamp();
            roundStartTimeStamp = e.getTimeStamp();
            return;
        }
        long offset = e.getTimeStamp() - tLastUpdate;
        tLastUpdate = e.getTimeStamp();
        //calculate things
        pos.setDurationOffset(pos.getDurationOffset() + offset);
        //<NOT FINAL> TODO
        //pos.setPercentage(pos.getPercentage()+((double)offset)/((double)pos.getSection().getDuration()));
        pos.setDistanceOffset(pos.getDistanceOffset()+calculateDistance(offset,pos.getSection().getDirection()));
        pos.setPercentage(pos.getDistanceOffset()/pos.getSection().getDistance());

        if(sectionChanged()){
            if(onSectionChange!=null){
                onSectionChange.onUpdate(sectionIndex, pos.getSection());
            }
            sectionIndex = (sectionIndex +1) % track.getSections().size();
            TrackSection next = track.getSections().get(sectionIndex);
            pos.setSection(next);
            pos.setDurationOffset(0); //add overshoot?
            pos.setPercentage(0);
            pos.setDistanceOffset(0);
        }
        if(onUpdate != null){
            //LOGGER.info("SENDING: SID: " + sectionIndex + ", Offset: " + pos.getDurationOffset() + "ms, Percentage: " + pos.getPercentage() + "%");
            onUpdate.onUpdate(sectionIndex, pos.getDurationOffset(),pos.getPercentage());
        }
    }

    private boolean sectionChanged(){
        //LOGGER.info("Selection changed ???");
       /*if(pos.getPercentage() > 0.5){
            if(pos.getSection().getDirection().equals("GOING STRAIGHT") && Math.abs(gyroZ.currentMean()) > TURN_THRESHOLD){
                LOGGER.info("going into TURN");
                return true;
            }
            if(pos.getSection().getDirection().equals("TURN") && Math.abs(gyroZ.currentMean()) < TURN_THRESHOLD){
                LOGGER.info("going into GOING STRAIGHT");
                return true;
            }
       }else if(pos.getPercentage()>=1){//100% oder mehr?
           return true;
       }*/
        return pos.getSection().getDistance() < pos.getDistanceOffset();
    }

    public void velocityUpdate(VelocityMessage message){
        LOGGER.info("Velocity: " + message.getVelocity() + ", calculated: " + calculatedVelocity + ", difference: " + Math.abs(message.getVelocity()-calculatedVelocity));
        calculatedVelocity = message.getVelocity();


        /*if(Math.abs(message.getTimeStamp()-roundStartTimeStamp)<500){
            velocityPositionId = 0;
        } else{
            velocityPositionId = (++velocityPositionId)%track.getCheckpoints().size();
        }*/
        velocityPositionId = (++velocityPositionId)%track.getCheckpoints().size();
        LOGGER.info("Sind an Position: " + velocityPositionId);
        // TODO Zurückgeben auf welcher SectionID das auto sich befindet + wieviel Prozent der Section abgeschlossen ist -> meistens entweder 0.00 oder 1.00, gibt aber ausnahmen
        if(sectionIndex != getTrackSectionId(velocityPositionId)/*track.getSections().indexOf(track.getCheckpoints().get(velocityPositionId).getSection())*/) {
            LOGGER.info("WRONG SECTION");
            setNewSection(track.getCheckpoints().get(velocityPositionId));
        }else{
            LOGGER.info("RIGHT SECTION");
            pos.setPercentage(track.getCheckpoints().get(velocityPositionId).getPercentage());
            pos.setDistanceOffset(track.getCheckpoints().get(velocityPositionId).getDistanceOffset());
            pos.setDurationOffset(track.getCheckpoints().get(velocityPositionId).getDurationOffset());
        }
    }

    private void setNewSection(Track.Position position){
        if((sectionIndex +1) % track.getSections().size()==getTrackSectionId(velocityPositionId)){
            if(onSectionChange!=null){
                onSectionChange.onUpdate(sectionIndex, pos.getSection());
            }
        }
        //sectionIndex = getTrackSectionId(velocityPositionId);
        pos.setPercentage(position.getPercentage());
        pos.setSection(position.getSection());
        pos.setDurationOffset(position.getDurationOffset());
        pos.setDistanceOffset(position.getDistanceOffset());
    }

    private int getTrackSectionId(int velocityPositionId){
        return track.getSections().indexOf(track.getCheckpoints().get(velocityPositionId).getSection());
    }

    public void roundTimeUpdate(RoundTimeMessage message){
        rountTime = message.getRoundDuration();
        LOGGER.info("LETZTE RUNDENZEIT: " + rountTime);
        roundStartTimeStamp = message.getTimestamp();
    }

    public static abstract class UpdateCallback{
        public abstract void onUpdate(int sectionIndex, long offset, double percentage);
    }

    public static abstract class SectionChangeCallback{
        public abstract void onUpdate(int sectionIndex, TrackSection section);
    }

    public void setOnSectionChange(SectionChangeCallback onSectionChange) {
        this.onSectionChange = onSectionChange;
    }

    public void setOnUpdate(UpdateCallback onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void setPower(int power){
        this.power = power;
    }
    public int getPower(){
        return power;
    }

    public int getPower(){
        return power;
    }

    public boolean isTurn(){
        return pos.getSection().getDirection().equals(State.TURN);
    }

    public double getPercentageDistance(){
        // TODO SOME MORE LOGICs
        return pos.getPercentage();
    }
}
