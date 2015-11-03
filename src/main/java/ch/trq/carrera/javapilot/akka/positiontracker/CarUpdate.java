package ch.trq.carrera.javapilot.akka.positiontracker;

/**
 * Created by tourn on 22.10.15.
 */
public class CarUpdate {
    private int trackIndex;
    private long offset;
    private double percentage;

    public CarUpdate(int trackIndex, long offset, double percentage) {
        this.trackIndex = trackIndex;
        this.offset = offset;
        this.percentage = percentage;
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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
