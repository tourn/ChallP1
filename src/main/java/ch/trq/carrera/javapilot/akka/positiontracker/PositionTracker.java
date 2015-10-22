package ch.trq.carrera.javapilot.akka.positiontracker;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
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
    private int power;

    public PositionTracker(Track track){
        this.track = track;
        pos = new Track.Position(track.getSections().get(0), 0);
        sectionIndex = 0;
    }

    public void update(SensorEvent e){
        gyroZ.shift(e.getG()[2]);

        if(tLastUpdate == -1){
            tLastUpdate = e.getTimeStamp();
            return;
        }
        long offset = e.getTimeStamp() - tLastUpdate;
        tLastUpdate = e.getTimeStamp();
        //calculate things

        pos.setDurationOffset(pos.getDurationOffset()+offset);
        if(sectionChanged()){
            if(onSectionChange!=null){
                onSectionChange.onUpdate(sectionIndex, pos.getSection());
            }

            sectionIndex = (sectionIndex +1) % track.getSections().size();
            TrackSection next = track.getSections().get(sectionIndex);
            pos.setSection(next);
            pos.setDurationOffset(0); //add overshoot?

        }
        if(onUpdate != null){
            onUpdate.onUpdate(sectionIndex, pos.getDurationOffset());
        }
    }

    private boolean sectionChanged(){
        //LOGGER.info("Selection changed ???");
        if(pos.getSection().getDuration() < (2*pos.getDurationOffset())){
            if(pos.getSection().getDirection().equals("GOING STRAIGHT") && Math.abs(gyroZ.currentMean()) > TURN_THRESHOLD){
                LOGGER.info("going into TURN");
                return true;
            }
            if(pos.getSection().getDirection().equals("TURN") && Math.abs(gyroZ.currentMean()) < TURN_THRESHOLD){
                LOGGER.info("going into GOING STRAIGHT");
                return true;
            }
        }
        return pos.getSection().getDuration() < pos.getDurationOffset();
    }

    public static abstract class UpdateCallback{
        public abstract void onUpdate(int sectionIndex, long offset);
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

    public boolean isTurn(){
        return pos.getSection().getDirection().equals("TURN");
    }

    public double getPercentageDistance(){
        // TODO SOME MORE LOGICs
        return((double)pos.getDurationOffset())/((double)pos.getSection().getDuration());
    }
}
