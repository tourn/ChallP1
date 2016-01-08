package ch.trq.carrera.javapilot.trackanalyzer;

import ch.trq.carrera.javapilot.math.PhysicModel;

/**
 * Created by Frank on 03.12.2015.
 */
public class TrackAndPhysicModelStorage {
    private PhysicModel physicModel;
    private Track track;
    public TrackAndPhysicModelStorage(Track track, PhysicModel physicModel){
        this.physicModel = physicModel;
        this.track = track;
    }

    public PhysicModel getPhysicModel() {
        return physicModel;
    }

    public Track getTrack() {
        return track;
    }
}
