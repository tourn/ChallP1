package ch.trq.carrera.javapilot.akka.positiontracker;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ch.trq.carrera.javapilot.akka.SpeedOptimizer;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Direction;
import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import ch.trq.carrera.javapilot.math.PhysicModel;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.carrera.timeseries.FloatingHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.function.Consumer;

/**
 * Created by tourn on 22.10.15.
 */
public class PositionTracker {
    private final Track track;
    private long tLastUpdate = -1;

    private Track.Position carPosition;
    private FloatingHistory gyroZ = new FloatingHistory(8);
    private static double TURN_THRESHOLD = 1500;
    private final Logger LOGGER = LoggerFactory.getLogger(PositionTracker.class);
    private int power = 0;

    private long sectionStartTime;

    private int velocityPositionId;

    private PhysicModel physicModel = null;

    private Consumer<TrackSection> onSectionChanged;

    public PositionTracker(Track track, PhysicModel physicModel) {
        this.track = track;
        carPosition = track.getCarPosition();
        this.physicModel = physicModel;
        int sectionIndex = carPosition.getSection().getId();
        velocityPositionId = getNextVelocityPositionId(sectionIndex);
        carPosition.setVelocity(track.getCheckpoints().get(velocityPositionId).getVelocity());
        sectionStartTime = System.currentTimeMillis();
    }

    public void setOnSectionChanged(Consumer<TrackSection> onSectionChanged) {
        this.onSectionChanged = onSectionChanged;
    }

    private int getNextVelocityPositionId(int trackSectionId) {
        for (Track.Position checkpoint : track.getCheckpoints()) {
            if (checkpoint.getSection().getId() >= trackSectionId) return track.getCheckpoints().indexOf(checkpoint);
        }
        return 0;
    }


    private double calculateDistance(long dtime) {
        double distance = 0;
        double calculatedVelocity = carPosition.getVelocity();
        for (int i = 0; i < dtime; i++) {
            calculatedVelocity = physicModel.getVelocity(calculatedVelocity, carPosition.getSection(), power, 1);
            distance += calculatedVelocity * 1.0 / 1000.0;
        }
        carPosition.setVelocity(calculatedVelocity);
        return distance;
    }

    private void changeSection(TrackSection newSection){
        long sectionEndTime = System.currentTimeMillis();
        carPosition.getSection().setDuration(sectionEndTime - sectionStartTime);
        if(onSectionChanged != null) {
            onSectionChanged.accept(carPosition.getSection());
        }
        sectionStartTime = sectionEndTime;
        carPosition.setSection(getNextTrackSection(carPosition.getSection()));
        carPosition.setPercentage(0);
        carPosition.setDistanceOffset(0);
    }

    public void sensorUpdate(SensorEvent e) {
        if (tLastUpdate == -1) {
            tLastUpdate = e.getTimeStamp();
            return;
        }
        long timeOffset = e.getTimeStamp() - tLastUpdate;
        tLastUpdate = e.getTimeStamp();
        carPosition.setDistanceOffset(carPosition.getDistanceOffset() + calculateDistance(timeOffset));
        if (sectionChanged()){
            changeSection(getNextTrackSection(carPosition.getSection()));
        }else{
           carPosition.setPercentage(Math.min(carPosition.getDistanceOffset() / carPosition.getSection().getDistance(),1.0));
        }
    }

    private TrackSection getNextTrackSection(TrackSection trackSection){
       return track.getSections().get((trackSection.getId() + 1) % track.getSections().size());
    }


    private boolean sectionChanged() {
        if(carPosition.getSection().getDistance() < carPosition.getDistanceOffset()){
            if(carPosition.getSection().getId() != track.getCheckpoints().get(velocityPositionId).getSection().getId()){
                if(carPosition.getSection().getId()+1 != track.getCheckpoints().get(velocityPositionId).getSection().getId() ||
                        track.getCheckpoints().get(velocityPositionId).getDistanceOffset() != 0){
                    return true;
                }
            }
        }
        return false;
        //return carPosition.getSection().getDistance() < carPosition.getDistanceOffset();
    }

    public void velocityUpdate(VelocityMessage message) {
        carPosition.setVelocity(message.getVelocity());

        LOGGER.info("Sind an Position: " + velocityPositionId);
        if (carPosition.getSection().getId() != getTrackSectionId(velocityPositionId)){
            changeSection(getTrackSection(velocityPositionId));
        } else {
            carPosition.setPercentage(track.getCheckpoints().get(velocityPositionId).getPercentage());
            carPosition.setDistanceOffset(track.getCheckpoints().get(velocityPositionId).getDistanceOffset());
        }

        velocityPositionId = (++velocityPositionId) % track.getCheckpoints().size();
    }

    private int getTrackSectionId(int velocityPositionId) {
        return track.getSections().indexOf(track.getCheckpoints().get(velocityPositionId).getSection());
    }

    private TrackSection getTrackSection(int velocityPositionId) {
        return track.getSections().get(getTrackSectionId(velocityPositionId));
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getPower() {
        return power;
    }

    public Track.Position getCarPosition() {
        return carPosition;
    }

    public boolean isTurn() {
        return carPosition.getSection().getDirection().equals(Direction.LEFT) || carPosition.getSection().getDirection().equals(Direction.RIGHT);
    }

    public double getPercentageDistance() {
        return carPosition.getPercentage();
    }

}
