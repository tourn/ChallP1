package ch.trq.carrera.javapilot.akka.positiontracker;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

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

    public PositionTracker(Track track){
        this.track = track;
        pos = new Track.Position(track.getSections().get(0), 0);
        sectionIndex = 0;
    }

    public void update(SensorEvent e){
        if(tLastUpdate == -1){
            tLastUpdate = e.getTimeStamp();
            return;
        }
        long offset = e.getTimeStamp() - tLastUpdate;
        tLastUpdate = e.getTimeStamp();
        //calculate things

        pos.setDurationOffset(pos.getDurationOffset()+offset);
        if(sectionChanged()){
            //int index = track.getSections().indexOf(pos.getSection());
            sectionIndex = (sectionIndex +1) % track.getSections().size();
            TrackSection next = track.getSections().get(sectionIndex);
            pos.setSection(next);
            pos.setDurationOffset(0); //add overshoot?

            if(onSectionChange!=null){
                onSectionChange.onUpdate(sectionIndex, pos.getSection());
            }
        }
        if(onUpdate != null){
            onUpdate.onUpdate(sectionIndex, pos.getDurationOffset());
        }
    }

    private boolean sectionChanged(){
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
}
