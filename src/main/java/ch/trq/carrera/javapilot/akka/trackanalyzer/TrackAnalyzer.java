package ch.trq.carrera.javapilot.akka.trackanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Frank on 22.09.2015.
 */
public class TrackAnalyzer {
    private ArrayList<Round> rounds;
    private Round tempRound = new Round(0,0);
    private TrackSection tempTrackSection = new TrackSection("",0);

    private final Logger LOGGER = LoggerFactory.getLogger(TrackAnalyzer.class);

    public TrackAnalyzer() {
        rounds = new ArrayList<Round>();
    }

    /**
     * This method ends the old round (add the end time), add it to the Rounds-List and starts a new round
     * @param timeStamp
     */
    public void newRound(long timeStamp,int pilotPower){
        tempTrackSection.setDuration(timeStamp - tempTrackSection.getTimeStamp());
        tempRound.setEndRoundTimeStamp(timeStamp);
        rounds.add(tempRound);
        tempRound = new Round(timeStamp,pilotPower);
    }

    public void addTrackSectionToRound(String direction, long timeStamp){
        tempTrackSection.setDuration(timeStamp - tempTrackSection.getTimeStamp());
        tempTrackSection = new TrackSection(direction,timeStamp);
        tempRound.addTrackSection(tempTrackSection);
    }

    public void addTrackVelocitiesToRound(double velocity, long timeStamp){
        tempRound.addTrackVelocity(new TrackVelocity(velocity, timeStamp));
    }

    public int roundCount(){
        return rounds.size();
    }

    public void printLastRound(){
        printRound(rounds.size()-1);
    }

    public void printRound(int roundnr){
        if (roundnr < roundCount()){
            Round round = rounds.get(roundnr);
            LOGGER.info("Round Nr. " + roundnr);
            printRound(round);
        } else {
            LOGGER.info("Round Nr. " + roundnr + "doesn't exist!");
        }
    }

    public void printRound(Round round){
        LOGGER.info("===================================================================");
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
        printTrack(round);
    }

    /*public void calculateTrack(){
        List<Round> tempRoundList = new ArrayList<Round>(rounds);
        Round calculatedRound = new Round(0);

        TODO: remove "fault" rounds/tracks

        tempRoundList.remove(0);
        tempRoundList.remove(0);
        for(int i=0; i<tempRoundList.get(0).getCountOfTrackSections(); i++){
            calculatedRound.addTrackSection(tempRoundList.get(0).getTrackSections().get(i).getDirection(),0);
        }
        for(int i=0; i<tempRoundList.get(0).getCountOfTrackVelocities(); i++){
            calculatedRound.addTrackVelocity(0,0);
        }
        for(Iterator<Round> roundIterator = tempRoundList.iterator(); roundIterator.hasNext();){
            Round round = roundIterator.next();
            for(int i=0; i<round.getCountOfTrackSections(); i++){
                calculatedRound.getTrackSections().get(i).setDuration(calculatedRound.getTrackSections().get(i).getDuration() + round.getTrackSections().get(i).getDuration());
                calculatedRound.getTrackSections().get(i).setTimeStamp((calculatedRound.getTrackSections().get(i).getTimeStamp() + (round.getTrackSections().get(i).getTimeStamp() - round.getStartRoundTimeStamp())));
                LOGGER.info("TS: "+calculatedRound.getTrackSections().get(1).getTimeStamp());
            }
            for(int i=0; i<round.getCountOfTrackVelocities(); i++){
                calculatedRound.getTrackVelocites().get(i).setVelocity(calculatedRound.getTrackVelocites().get(i).getVelocity()+round.getTrackVelocites().get(i).getVelocity());
                calculatedRound.getTrackVelocites().get(i).setTimeStamp((calculatedRound.getTrackVelocites().get(i).getTimeStamp() + (round.getTrackVelocites().get(i).getTimeStamp() - round.getStartRoundTimeStamp())));
            }
        }
        for(int i=0; i<calculatedRound.getCountOfTrackSections(); i++){
            calculatedRound.getTrackSections().get(i).setDuration(calculatedRound.getTrackSections().get(i).getDuration() / tempRoundList.size());
            calculatedRound.getTrackSections().get(i).setTimeStamp(calculatedRound.getTrackSections().get(i).getTimeStamp() / tempRoundList.size());
        }
        for(int i=0; i<calculatedRound.getCountOfTrackVelocities(); i++){
            calculatedRound.getTrackVelocites().get(i).setVelocity(calculatedRound.getTrackVelocites().get(i).getVelocity() / tempRoundList.size());
            calculatedRound.getTrackVelocites().get(i).setTimeStamp(calculatedRound.getTrackVelocites().get(i).getTimeStamp() / tempRoundList.size());
        }

        printRound(calculatedRound);
        printTrack(calculatedRound);
    }*/

    public void printTrack(Round round){
        String track = "";
        long time = round.getStartRoundTimeStamp();
        char dir;
        for(int i=0; i<round.getCountOfTrackSections(); i++){
            switch(round.getTrackSections().get(i).getDirection()){
                case "LEFT TURN":
                    dir = '/';
                    break;
                case "RIGHT TURN":
                    dir = '\\';
                    break;
                case "GOING STRAIGHT":
                    dir = '-';
                    break;
                default:
                    dir = 'X';
            }
            for(int j=0; j<round.getTrackSections().get(i).getDuration()/100;j++){
                track += dir;
            }
        }
        LOGGER.info("\n"+track);
    }

}
