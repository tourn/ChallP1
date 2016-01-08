package ch.trq.carrera.javapilot.log;

import com.zuehlke.carrera.relayapi.messages.SensorEvent;

/**
 * Created by tourn on 6.11.15.
 */
public class LogMessage {

    SensorEvent event;

    double acceleration;
    double velocity;
    double positionAbsolute;
    double positionRelative;
    String actorDescription;

    int power;
    int trackSectionId;

    long tEvent;
    long tReceived;
    long tAfterCalculation;

    public LogMessage(SensorEvent event, long tReceived){
        this.event = event;
        tEvent = event.getTimeStamp();
        this.tReceived = tReceived;
    }

    public SensorEvent getEvent() {
        return event;
    }

    public void setEvent(SensorEvent event) {
        this.event = event;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public long gettEvent() {
        return tEvent;
    }

    public void settEvent(long tEvent) {
        this.tEvent = tEvent;
    }

    public long gettReceived() {
        return tReceived;
    }

    public void settReceived(long tReceived) {
        this.tReceived = tReceived;
    }

    public long gettAfterCalculation() {
        return tAfterCalculation;
    }

    public void settAfterCalculation(long tAfterCalculation) {
        this.tAfterCalculation = tAfterCalculation;
    }

    public double getPositionAbsolute() {
        return positionAbsolute;
    }

    public void setPositionAbsolute(double positionAbsolute) {
        this.positionAbsolute = positionAbsolute;
    }

    public double getPositionRelative() {
        return positionRelative;
    }

    public void setPositionRelative(double positionRelative) {
        this.positionRelative = positionRelative;
    }

    public String getActorDescription() {
        return actorDescription;
    }

    public void setActorDescription(String actorDescription) {
        this.actorDescription = actorDescription;
    }

    public int getTrackSectionId() {
        return trackSectionId;
    }

    public void setTrackSectionId(int trackSectionId) {
        this.trackSectionId = trackSectionId;
    }
}
