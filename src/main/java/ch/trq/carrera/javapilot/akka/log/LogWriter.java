package ch.trq.carrera.javapilot.akka.log;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tourn on 6.11.15.
 */
public class LogWriter {
    private final Logger LOGGER = LoggerFactory.getLogger(LogWriter.class);

    private FileWriter logWriter;
    private Gson gson = new Gson();
    private boolean firstLogEntry = true;

    public LogWriter() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        File logFile = new File("logs/log" + format.format(new Date()) + ".json");
        try {
            logWriter = new FileWriter(logFile);
            logWriter.append("{ \"raceData\": [");
            LOGGER.info("Logging to" + logFile.getAbsolutePath());
        } catch ( IOException e ) {
            LOGGER.error("Could not open logfile", e);
        }
    }

    public void append(Object message){
        StringBuilder msg = new StringBuilder();
        if(firstLogEntry){
            firstLogEntry = false;
        } else {
            msg.append(",");
        }
        msg.append(gson.toJson(message));
        msg.append("\n");
        try {
            logWriter.append(msg.toString());
        } catch (IOException e) {
            LOGGER.error("Error writing log", e);
        }

    }

    public void close(){
        LOGGER.info("Finalizing");
        try {
            logWriter.append("]}");
            logWriter.close();
        } catch (IOException e) {
            LOGGER.error("Error closing logfile", e);
        }
    }
}
