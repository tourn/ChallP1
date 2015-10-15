package ch.trq.carrera.javapilot.akka.trackanalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

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
            LOGGER.info("Velocity" + n + ": " + trackVelocity.getVelocity() + ", TimeStamp: " + trackVelocity.getTimeStamp());
            n++;
        }
        for(Iterator<TrackSection> i = round.getTrackSections().iterator(); i.hasNext();){
            TrackSection trackSection = i.next();
            LOGGER.info("Direction: " + trackSection.getDirection() + ", Duration:" + trackSection.getDuration() + "ms, TimeStamp: " + trackSection.getTimeStamp());
        }
        LOGGER.info("===================================================================");
        printTrack(round);
    }

    // CALCULATE TRACK PRIVATE METHODS -- begin
    private void iterateEachRound(List<Round> tempRoundList,Consumer<Round> c){
        for(Iterator<Round> roundIterator = tempRoundList.iterator(); roundIterator.hasNext(); ) {
            Round round = roundIterator.next();
            c.accept(round);
        }
    }

    private void mergeAndRecalculateTrackSections(List<Round> tempRoundList){
        //merge same directions (which follows each other)!!!
        iterateEachRound(tempRoundList, round -> {
            for (int i = 0; i < round.getCountOfTrackSections(); i++) {
                if (i < round.getCountOfTrackSections() - 1) {
                    if (round.getTrackSections().get(i).getDirection().equals(round.getTrackSections().get(i + 1).getDirection())) {
                        round.getTrackSections().remove(i + 1);
                        i--;
                    }
                }
            }
        });
        //RECALCULATE THE DURATION FOR EACH TRACKSECTION!!!
        iterateEachRound(tempRoundList, round -> {
            for (int i = 0; i < round.getCountOfTrackSections(); i++) {
                if (i < round.getCountOfTrackSections() - 1) {
                    round.getTrackSections().get(i).setDuration(round.getTrackSections().get(i + 1).getTimeStamp() - round.getTrackSections().get(i).getTimeStamp());
                } else {
                    round.getTrackSections().get(i).setDuration(round.getEndRoundTimeStamp() - round.getTrackSections().get(i).getTimeStamp());
                }
            }
        });
    }

    private void removeFaultGoingStraightTrackSections(List<Round> tempRoundList, int faultyGoingStraightTime){
        //remove "fault" "GOING STRAIGHT" trackSections, sections, which are not longer than <faultyGoingStraightTime> ms.
        iterateEachRound(tempRoundList, round -> {
            for(Iterator<TrackSection> trackSectionIterator = round.getTrackSections().iterator(); trackSectionIterator.hasNext(); ) {
                TrackSection trackSection = trackSectionIterator.next();
                if(trackSection.getDuration() < faultyGoingStraightTime && trackSection.getDirection().equals("GOING STRAIGHT")){
                    trackSectionIterator.remove();
                }
            }
        });
    }

    private void removeFaultTurnTrackSections(List<Round> tempRoundList, int faultyTurnTime){
        //remove "fault" "Turn" trackSections, sections, which are not longer than <faultyTurnTime> ms.
        iterateEachRound(tempRoundList, round -> {
            for(Iterator<TrackSection> trackSectionIterator = round.getTrackSections().iterator(); trackSectionIterator.hasNext(); ) {
                TrackSection trackSection = trackSectionIterator.next();
                if(trackSection.getDuration() < 150){
                    trackSectionIterator.remove();
                }
            }
        });
    }

    private boolean haveSameAmountOfTrackSections(List<Round> tempRoundList){
        //Check that all rounds have the same count of TrackSections
        int trackSectionCounter = tempRoundList.get(0).getCountOfTrackSections();
        for(Iterator<Round> roundIterator = tempRoundList.iterator(); roundIterator.hasNext(); ) {
            Round round = roundIterator.next();
            if(round.getCountOfTrackSections()!=trackSectionCounter){
                //ERROR FAIL
                LOGGER.info("Track-Calculation failed. Not the same amout of Tracksections");
                return false;
            }
        }
        return true;
    }

    private boolean haveAllRoundsSameDirections(List<Round> tempRoundList){
        //TODO: Check that all rounds have the same Tracksection-Directions
        for (int i = 0; i < tempRoundList.get(0).getCountOfTrackSections(); i++) {
            String direction = tempRoundList.get(0).getTrackSections().get(i).getDirection();
            for (Iterator<Round> roundIterator = tempRoundList.iterator(); roundIterator.hasNext(); ) {
                Round round = roundIterator.next();
                if (!(round.getTrackSections().get(i).getDirection().equals(direction))) {
                    //ERROR FAIL
                    LOGGER.info("Track-Calculation failed. Not the same TrackDirections");
                    LOGGER.info("WE GOT A BIG PROBLEM");
                    return false;
                }
            }
        }
        return true;
    }

    private void useLowestTrackSectionRoundOnly(List<Round> tempRoundList){
        // If Calculation failed -> the calculatet track will be the track with the lowest Tracksection;
        Round roundWithLowestTrackSection = tempRoundList.get(0);
        for(Round round : tempRoundList){
            if(round.getCountOfTrackSections() < roundWithLowestTrackSection.getCountOfTrackSections()){
                roundWithLowestTrackSection = round;
            }
        }
        tempRoundList.clear();
        tempRoundList.add(roundWithLowestTrackSection);
    }

    private Round createCalculatedRound(List<Round> tempRoundList){
        Round calculatedRound = new Round(0,rounds.get(1).getPilotPower());

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
                //LOGGER.info("TS: "+calculatedRound.getTrackSections().get(1).getTimeStamp());
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

        return calculatedRound;
    }
    // CALCULATE TRACK PRIVATE METHODS -- end



    public Track calculateTrack(){
        int faultyGoingStraightTime = 300;
        int faultyTurnTime = 150;
        List<Round> tempRoundList = new ArrayList<Round>(rounds);

        // Remove the first and second round (first is broken)
        tempRoundList.remove(0);

        removeFaultGoingStraightTrackSections(tempRoundList,faultyGoingStraightTime);

        mergeAndRecalculateTrackSections(tempRoundList);

        removeFaultTurnTrackSections(tempRoundList,faultyTurnTime);

        mergeAndRecalculateTrackSections(tempRoundList);

        if(!(haveSameAmountOfTrackSections(tempRoundList) &&haveAllRoundsSameDirections(tempRoundList))) {
            useLowestTrackSectionRoundOnly(tempRoundList);
        }

        Round calculatedRound = createCalculatedRound(tempRoundList);

        //printRound(calculatedRound);
        printTrack(calculatedRound);
        return generateTrack(calculatedRound);
    }




    private Track generateTrack(Round round){
        Track track = new Track();
        round.getTrackSections().stream().forEach(s -> track.getSections().add(s));
        /*
        round.getTrackVelocites().stream().forEach(v -> {
            TrackSection last = null;
            for(Iterator<TrackSection> it = round.getTrackSections().iterator();; it.hasNext()){
                TrackSection section = it.next();
                if(section.getTimeStamp() > v.getTimeStamp()){
                    track.getCheckpoints().add(new Track.Position(last, v.getTimeStamp()-last.getTimeStamp()));
                    break;
                } else {
                    last = section;
                }
            }
        });
        */
        return track;
    }

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
