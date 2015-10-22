package ch.trq.carrera.javapilot.akka.positiontracker;

/**
 * Created by tourn on 22.10.15.
 */
public class CarUpdate {
    private int trackIndex;
    private long offset;

    public CarUpdate(int trackIndex, long offset) {
        this.trackIndex = trackIndex;
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

}
