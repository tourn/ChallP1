package com.zuehlke.carrera.javapilot.akka.experimental.trackanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Frank on 22.09.2015.
 */
public class TrackAnalyzer {
    private ArrayList<Round> rounds;
    private Round tempRound = new Round(0);
    private TrackSection tempTrackSection = new TrackSection("",0);

    private final Logger LOGGER = LoggerFactory.getLogger(TrackAnalyzer.class);

    public TrackAnalyzer() {
        rounds = new ArrayList<Round>();

    }

    /**
     * This method ends the old round (add the end time), add it to the Rounds-List and starts a new round
     * @param timeStamp
     */
    public void newRound(long timeStamp){
        tempRound.setEndRoundTimeStamp(timeStamp);
        rounds.add(tempRound);
        tempRound = new Round(timeStamp);
    }

    public void addTrackSectionToRound(String direction, long timeStamp){
        tempTrackSection.setDuration(timeStamp - tempTrackSection.getTimeStamp());
        tempTrackSection = new TrackSection(direction,timeStamp);
        tempRound.addTrackSection(tempTrackSection);
    }

    public void addTrackVelocitiesToRound(double velocity, long timeStamp){
        tempRound.addTrackVelocity(new TrackVelocity(velocity,timeStamp));
    }

    public int roundCount(){
        return rounds.size();
    }

    public void printRound(int roundnr){
        if (roundnr < roundCount()){
            Round round = rounds.get(roundnr);
            LOGGER.info("===================================================================");
            LOGGER.info("Round Nr. " + roundnr);
            LOGGER.info("Time: " + round.getRoundTime() + "ms");
            int n = 0;
            for(Iterator<TrackVelocity> i = round.getTrackVelocites().iterator(); i.hasNext();){
                TrackVelocity trackVelocity = i.next();
                LOGGER.info("Velocity"+n+": "+trackVelocity.getVelocity()+", TimeStamp: "+trackVelocity.getTimeStamp());
                n++;
            }
            for(Iterator<TrackSection> i = round.getTrackSections().iterator(); i.hasNext();){
                TrackSection trackSection = i.next();
                LOGGER.info("Direction: "+trackSection.getDirection()+", Duration:"+trackSection.getDuration()+"ms, TimeStamp: "+trackSection.getTimeStamp());
            }
            LOGGER.info("===================================================================");
        } else {
            LOGGER.info("Round Nr. " + roundnr + "doesn't exist!");
        }
    }

}
