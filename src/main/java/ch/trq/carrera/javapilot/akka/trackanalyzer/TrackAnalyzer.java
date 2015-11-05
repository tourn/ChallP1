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
    protected ArrayList<Round> rounds;
    protected Round tempRound = new Round(0,0);
    protected TrackSection tempTrackSection = new TrackSection(State.TURN,0);

    protected boolean isNewRound;
    protected int newPilotPower;

    private final Logger LOGGER = LoggerFactory.getLogger(TrackAnalyzer.class);

    public TrackAnalyzer() {
        rounds = new ArrayList<Round>();
        isNewRound = false;
    }


    /**
     * Sets the status, that there has started a new Round. The TrackAnalyzer will end the last round at the next
     * TrackSection and setup a new Round
     *
     * @param  pilotPower  the Pilot-Power from the last Round
     */
    public void newRound(int pilotPower){
        isNewRound = true;
        newPilotPower = pilotPower;
    }

    /**
     * Creates a new Track-Section with the Direction and the start-time-stamp and adds it to the round.
     * Calculates and sets also the duration of the last Tracksection.
     * If there is a new round (after Calling newRound), it creates also a new round before adding the new track-section
     * to the round.
     *
     * @param direction The Direction of the track-section
     * @param timeStamp The start-time-stamp of the next tracksection
     */
    public void addTrackSectionToRound(State direction, long timeStamp){
        if(isNewRound){
            isNewRound = false;
            tempTrackSection.setDuration(timeStamp - tempTrackSection.getTimeStamp());
            tempRound.setEndRoundTimeStamp(timeStamp);
            rounds.add(tempRound);
            tempRound = new Round(timeStamp,newPilotPower);
        }else{
            tempTrackSection.setDuration(timeStamp - tempTrackSection.getTimeStamp());
        }
        tempTrackSection = new TrackSection(direction,timeStamp);
        tempRound.addTrackSection(tempTrackSection);
    }

    /**
     * Creates a new track-velocity and adds it to the actual round
     *
     * @param velocity The velocity from the velocity-sensor-message
     * @param timeStamp The received time-stamp from the velocity-sensor-message
     */
    public void addTrackVelocitiesToRound(double velocity, long timeStamp){
        tempRound.addTrackVelocity(new TrackVelocity(velocity, timeStamp));
    }

    /**
     * @return The amount of all completed rounds in the TrackAnalyzer
     */
    public int getCompletedRoundsCount(){
        return rounds.size();
    }

    public void printLastRound(){
        printRound(rounds.size()-1);
    }

    public void printRound(int roundnr){
        if (roundnr < getCompletedRoundsCount()){
            Round round = rounds.get(roundnr);
            LOGGER.info("Round Nr. " + roundnr);
            printRound(round);
        } else {
            LOGGER.info("Round Nr. " + roundnr + "doesn't exist!");
        }
    }

    /**
     * Prints the round into the console (Logger). The Information of the round which wil be printed are:
     * <ul></ul>
     *  <li>Roundtime
     *  <li>List of all TrackVelocities with their velocity and timestamp
     *  <li>List of all Tracksections with their direction, duration and timestamp
     * </ul>
     *
     * @param round The round which will be printed
     */
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

    protected void mergeAndRecalculateTrackSections(List<Round> tempRoundList){
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
            // If the first TrackSection dont have the same startTimeStamp like the RoundStartTimeStamp, recalculate this
            if(round.getTrackSections().get(0).getTimeStamp()!= round.getStartRoundTimeStamp()){
                round.getTrackSections().get(0).setDuration(round.getTrackSections().get(0).getDuration() + round.getTrackSections().get(0).getTimeStamp() - round.getStartRoundTimeStamp());
                round.getTrackSections().get(0).setTimeStamp(round.getStartRoundTimeStamp());
            }
            for (int i = 0; i < round.getCountOfTrackSections(); i++) {
                if (i < round.getCountOfTrackSections() - 1) {
                    round.getTrackSections().get(i).setDuration(round.getTrackSections().get(i + 1).getTimeStamp() - round.getTrackSections().get(i).getTimeStamp());
                } else {
                    round.getTrackSections().get(i).setDuration(round.getEndRoundTimeStamp() - round.getTrackSections().get(i).getTimeStamp());
                }
            }
        });
    }

    protected void removeFaultGoingStraightTrackSections(List<Round> tempRoundList, int faultyGoingStraightTime){
        //remove "fault" "GOING STRAIGHT" trackSections, sections, which are not longer than <faultyGoingStraightTime> ms.
        iterateEachRound(tempRoundList, round -> {
            for(Iterator<TrackSection> trackSectionIterator = round.getTrackSections().iterator(); trackSectionIterator.hasNext(); ) {
                TrackSection trackSection = trackSectionIterator.next();
                if(trackSection.getDuration() < faultyGoingStraightTime && trackSection.getDirection().equals(State.STRAIGHT)){
                    trackSectionIterator.remove();
                }
            }
        });
    }

    protected void removeFaultTurnTrackSections(List<Round> tempRoundList, int faultyTurnTime){
        //remove "fault" "Turn" trackSections, sections, which are not longer than <faultyTurnTime> ms.
        iterateEachRound(tempRoundList, round -> {
            for(Iterator<TrackSection> trackSectionIterator = round.getTrackSections().iterator(); trackSectionIterator.hasNext(); ) {
                TrackSection trackSection = trackSectionIterator.next();
                if(trackSection.getDuration() < faultyTurnTime && trackSection.getDirection().equals(State.TURN)){
                    trackSectionIterator.remove();
                }
            }
        });
    }

    protected boolean haveSameAmountOfTrackSections(List<Round> tempRoundList){
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

    protected boolean haveAllRoundsSameDirections(List<Round> tempRoundList){
        //TODO: Check that all rounds have the same Tracksection-Directions
        for (int i = 0; i < tempRoundList.get(0).getCountOfTrackSections(); i++) {
            State direction = tempRoundList.get(0).getTrackSections().get(i).getDirection();
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

    protected void useLowestTrackSectionRoundOnly(List<Round> tempRoundList){
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

    protected Round createCalculatedRound(List<Round> tempRoundList){
        Round calculatedRound = new Round(0,tempRoundList.get(0).getPilotPower());

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
        // SetRoundTime
        int roundTime=0;
        for(int i=0;i<calculatedRound.getCountOfTrackSections();i++){
            roundTime+=calculatedRound.getTrackSections().get(i).getDuration();
        }
        calculatedRound.setEndRoundTimeStamp(roundTime);
        return calculatedRound;
    }
    // CALCULATE TRACK PRIVATE METHODS -- end


    /**
     * Merges and calculate from the completed rounds in an average round which starts at time Zero. This
     * round will transformed afterwards in a Track
     *
     * @param  startRoundNr  The amount of rounds which will be skipped at the beginning
     * @param  faultyGoingStraightTime The minimal duration for Straight-TrackSections. Each Straight-TrackSection
     *                                 below this duration will be removed.
     * @param  faultyTurnTime The minimal duration for Turn-TrackSections. Each Turn-TrackSection below this duration
     *                        will be removed.
     * @return      The calculated Track
     * @see         Track
     */
    public Track calculateTrack(int startRoundNr,int faultyGoingStraightTime, int faultyTurnTime){
        /*int startRoundNr;
        int faultyGoingStraightTime = 300;
        int faultyTurnTime = 150;*/
        List<Round> tempRoundList = new ArrayList<Round>(rounds);

        // Remove all Rounds until startRoundNr.
        for(int i = 0; i < startRoundNr; i++){
            tempRoundList.remove(0);
        }

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



    protected Track generateTrack(Round round){
        Track track = new Track();
        round.getTrackSections().stream().forEach(s -> track.getSections().add(s));
        for(TrackVelocity trackVelocity : round.getTrackVelocites()){
            /*for(Iterator<TrackSection> trackSectionIterator = round.getTrackSections().iterator(); trackSectionIterator.hasNext(); ) {
                if(trackVelocity.getTimeStamp()>trackSection.getTimeStamp()){
                    trackSection = trackSectionIterator.next();
                } else{
                    break;
                }
            }*/
            int trackSectionId=0;
            for(int i=0;i<round.getTrackSections().size();i++){
                if(round.getTrackSections().get(i).getTimeStamp()<trackVelocity.getTimeStamp() && round.getTrackSections().get(i).getTimeStamp()+round.getTrackSections().get(i).getDuration()>trackVelocity.getTimeStamp()){
                    trackSectionId=i;
                }
            }
            TrackSection trackSection = round.getTrackSections().get(trackSectionId);
            long offset = trackVelocity.getTimeStamp()-trackSection.getTimeStamp();
            //LOGGER.info("offset: " +offset+", TVTS: " + trackVelocity.getTimeStamp() + ", TSTS: " + trackSection.getTimeStamp());
            Track.Position p = new Track.Position(trackSection,offset);
            p.setPercentage((double)offset/(double)trackSection.getDuration());
            //LOGGER.info("offset: " + offset + "ms, TracksectionID: " + round.getTrackSections().indexOf(trackSection));
            LOGGER.info("CHECKPOINT %: " + p.getPercentage());
            track.getCheckpoints().add(p);
        }
        track.setPower(round.getPilotPower());
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
                /*case "LEFT TURN":
                    dir = '/';
                    break;
                case "RIGHT TURN":
                    dir = '\\';
                    break;*/
                case STRAIGHT:
                    dir = '-';
                    break;
                case TURN:
                    dir = 'X';
                    break;
                default:
                    dir = '#';
            }
            for(int j=0; j<round.getTrackSections().get(i).getDuration()/100;j++){
                track += dir;
            }
        }
        LOGGER.info("\n"+track);
    }

}
