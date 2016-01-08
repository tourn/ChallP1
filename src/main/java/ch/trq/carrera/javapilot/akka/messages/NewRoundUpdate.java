package ch.trq.carrera.javapilot.akka.messages;

/**
 * Created by mario on 09.11.15.
 */
public class NewRoundUpdate {
    private long roundtime;

    public NewRoundUpdate(long roundtime){
        this.roundtime = roundtime;
    }

    public long getRoundtime() {
        return roundtime;
    }

    public void setRoundtime(long roundtime) {
        this.roundtime = roundtime;
    }
}
