package ch.trq.carrera.javapilot.akka.trackanalyzer;

import ch.trq.carrera.javapilot.akka.log.LogMessage;
import ch.trq.carrera.javapilot.akka.log.LogWriter;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import org.junit.Test;

/**
 * Created by tourn on 6.11.15.
 */
public class LogWriterTest {

    @Test
    public void testWrite(){
        SensorEvent event1 = new SensorEvent("foo", new int[]{ 1, 2, 3}, new int[]{4,5,6}, new int[]{7,8,9}, 2);
        SensorEvent event2 = new SensorEvent("foo", new int[]{ 2, 3, 4}, new int[]{4,5,6}, new int[]{7,8,9}, 4);
        LogMessage mess1 = new LogMessage(event1, 5);
        mess1.setAcceleration(5);
        LogMessage mess2 = new LogMessage(event2, 10);
        mess2.setAcceleration(10);
        LogWriter writer = new LogWriter();
        writer.append(mess1);
        writer.append(mess2);
        writer.close();


    }
}
