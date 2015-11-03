package ch.trq.carrera.javapilot.akka.trackanalyzer;

import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Frank on 03.11.2015.
 */
public class TrackAnalyzerTest {

    private TrackAnalyzer trackAnalyzer;
    private static Round roundWaste;
    private static Round round1;
    private static Round round2;
    private static Round roundWrong;

    private static int AmountOfTrackSectionsX2 = 5;

    private static long round1StartTime = 0;
    private static int round1TimeTurn = 200;
    private static int round1TimeStraight = 300;

    private static long round2StartTime = 0;
    private static int round2TimeTurn = 220;
    private static int round2TimeStraight = 290;

    @Before
    public void CreateTestRounds(){
        //Create Waste Round
        roundWaste = new Round(0,0);
        //Create Round1
        round1 = new Round(round1StartTime,0);
        for(int i = 0; i<AmountOfTrackSectionsX2; i++){   // adds TrackSections
            TrackSection trackSection = new TrackSection(State.TURN, round1StartTime + i*(round1TimeTurn+round1TimeStraight));
            trackSection.setDuration(round1TimeTurn);
            round1.addTrackSection(trackSection);
             trackSection = new TrackSection(State.STRAIGHT, round1StartTime + i*(round1TimeTurn+round1TimeStraight)+round1TimeTurn);
            trackSection.setDuration(round1TimeStraight);
            round1.addTrackSection(trackSection);
        }
        round1.setEndRoundTimeStamp(round1StartTime + AmountOfTrackSectionsX2*(round1TimeTurn+round1TimeStraight));
        round1.addTrackVelocity(new TrackVelocity(500,round1StartTime+100));
        round1.addTrackVelocity(new TrackVelocity(400,round1StartTime+800));
        round1.addTrackVelocity(new TrackVelocity(600,round1StartTime+1200));
        //Create Round2
        round2StartTime = round1StartTime + AmountOfTrackSectionsX2*(round1TimeTurn+round1TimeStraight);
        round2 = new Round(round2StartTime,0);
        for(int i = 0; i<AmountOfTrackSectionsX2; i++){   // adds TrackSections
            TrackSection trackSection = new TrackSection(State.TURN, round2StartTime + i*(round2TimeTurn+round2TimeStraight));
            trackSection.setDuration(round2TimeTurn);
            round2.addTrackSection(trackSection);
            trackSection = new TrackSection(State.STRAIGHT, round2StartTime + i*(round2TimeTurn+round2TimeStraight)+round2TimeTurn);
            trackSection.setDuration(round2TimeStraight);
            round2.addTrackSection(trackSection);
        }
        round2.addTrackVelocity(new TrackVelocity(550,round2StartTime+90));
        round2.addTrackVelocity(new TrackVelocity(450,round2StartTime+900));
        round2.addTrackVelocity(new TrackVelocity(650,round2StartTime+1300));

    }

    @Before
    public void initialize(){
        trackAnalyzer = new TrackAnalyzer();
    }

    @Test
    public void newRoundShouldSetIsNewRoundFromFalseToTrue() {
        assertFalse(trackAnalyzer.isNewRound);
        trackAnalyzer.newRound(0,0);
        assertTrue(trackAnalyzer.isNewRound);
    }

    @Test
    public void newRoundShouldSetPilotPower() {
        trackAnalyzer.newRound(0, 100);
        assertEquals(100, trackAnalyzer.newPilotPower);
    }

    @Test
    public void addTrackSectionToRoundShouldSetIsNewRoundToFalseWhenItIsTrue() {
        trackAnalyzer.newRound(0, 0);
        assertTrue(trackAnalyzer.isNewRound);
        trackAnalyzer.addTrackSectionToRound(State.TURN, 0);
        assertFalse(trackAnalyzer.isNewRound);
    }

    @Test
    public void addTrackSectionToRoundWhenIsNewRoundShouldAddANewRound() {
        trackAnalyzer.newRound(0, 0);
        assertEquals(0, trackAnalyzer.roundCount());
        trackAnalyzer.addTrackSectionToRound(State.TURN, 0);
        assertEquals(1, trackAnalyzer.roundCount());
    }

    @Test
    public void addTrackSectionToRoundTheNewSectionShouldntHaveTheTurnDirectionWhichWasSet(){
        trackAnalyzer.newRound(0, 0);
        trackAnalyzer.addTrackSectionToRound(State.TURN, 0);
        assertEquals(State.TURN, trackAnalyzer.tempRound.getTrackSections().get(trackAnalyzer.tempRound.getCountOfTrackSections()-1).getDirection());
    }
    @Test
    public void addTrackSectionToRoundTheNewSectionShouldntHaveTheStraightDirectionWhichWasSet(){
        trackAnalyzer.newRound(0, 0);
        trackAnalyzer.addTrackSectionToRound(State.STRAIGHT, 0);
        assertEquals(State.STRAIGHT, trackAnalyzer.tempRound.getTrackSections().get(trackAnalyzer.tempRound.getCountOfTrackSections() - 1).getDirection());
    }

    @Test
    public void addTrackSectionToRoundTheNewSectionShouldntHaveADuration() {
        trackAnalyzer.newRound(0, 0);
        trackAnalyzer.addTrackSectionToRound(State.TURN, 123);
        assertEquals(0, trackAnalyzer.tempRound.getTrackSections().get(trackAnalyzer.tempRound.getCountOfTrackSections()-1).getDuration());
    }

    @Test
    public void addTrackSectionToRoundThePreviousSectionShouldHaveADuration() {
        trackAnalyzer.newRound(0, 0);
        trackAnalyzer.addTrackSectionToRound(State.STRAIGHT, 0);
        trackAnalyzer.addTrackSectionToRound(State.TURN, 123);
        assertEquals(trackAnalyzer.tempRound.getTrackSections().get(trackAnalyzer.tempRound.getCountOfTrackSections()-1).getTimeStamp()-trackAnalyzer.tempRound.getTrackSections().get(trackAnalyzer.tempRound.getCountOfTrackSections()-2).getTimeStamp(), trackAnalyzer.tempRound.getTrackSections().get(trackAnalyzer.tempRound.getCountOfTrackSections()-2).getDuration());
    }

    @Test
    public void addTrackVelocityToRoundShouldIncreaseAmountOfTrackVelocities(){
        assertEquals(0, trackAnalyzer.tempRound.getCountOfTrackVelocities());
        trackAnalyzer.addTrackVelocitiesToRound(0, 0);
        assertEquals(1, trackAnalyzer.tempRound.getCountOfTrackVelocities());
    }


    //////////////
    @Test
    public void removeFaultTurnTrackSectionsShouldRemoveAllTurns(){
        List<Round> roundList = new ArrayList<Round>();
        roundList.add(round1);
        trackAnalyzer.removeFaultTurnTrackSections(roundList, round1TimeTurn + 10);
        for(TrackSection trackSection : round1.getTrackSections()){
            assertEquals(State.STRAIGHT, trackSection.getDirection());
        }
        assertNotEquals("Has Removed Everything", 0, round1.getCountOfTrackSections());
    }

    @Test
    public void removeFaultTurnTrackSectionsShouldRemoveNothing(){
        List<Round> roundList = new ArrayList<Round>();
        roundList.add(round1);
        int trackSectionCount = round1.getCountOfTrackSections();
        trackAnalyzer.removeFaultTurnTrackSections(roundList, round1TimeTurn - 10);
        assertEquals(trackSectionCount, round1.getCountOfTrackSections());
    }

    @Test
    public void mergeAndRecalculateTrackSectionsShouldMergeWholeRoundIntoOneTrackSectionWhichIsAStraight(){
        List<Round> roundList = new ArrayList<Round>();
        int duration = 0;
        for(TrackSection trackSection : round1.getTrackSections()){
            duration += trackSection.getDuration();
        }
        roundList.add(round1);
        trackAnalyzer.removeFaultTurnTrackSections(roundList, round1TimeTurn + 10); //Should remove all Turns
        for(TrackSection trackSection : round1.getTrackSections()){
            assertEquals(State.STRAIGHT, trackSection.getDirection());
        }
        assertNotEquals("Has Removed Everything",0, round1.getCountOfTrackSections());
        trackAnalyzer.mergeAndRecalculateTrackSections(roundList);
        assertEquals(1, round1.getCountOfTrackSections());
        assertEquals("Should have the same duration as before.",duration, round1.getTrackSections().get(0).getDuration());
    }


    @Test
    public void removeFaultGoingStraightTrackSectionsShouldRemoveAllStraights(){
        List<Round> roundList = new ArrayList<Round>();
        roundList.add(round1);
        trackAnalyzer.removeFaultGoingStraightTrackSections(roundList, round1TimeStraight + 10);
        for(TrackSection trackSection : round1.getTrackSections()){
            assertEquals(State.TURN, trackSection.getDirection());
        }
        assertNotEquals("Has Removed Everything",0, round1.getCountOfTrackSections());
    }

    @Test
    public void removeFaultGoingStraightTrackSectionsShouldRemoveNothing(){
        List<Round> roundList = new ArrayList<Round>();
        roundList.add(round1);
        int trackSectionCount = round1.getCountOfTrackSections();
        trackAnalyzer.removeFaultGoingStraightTrackSections(roundList, round1TimeStraight - 10);
        assertEquals(trackSectionCount, round1.getCountOfTrackSections());
    }

    @Test
    public void mergeAndRecalculateTrackSectionsShouldMergeWholeRoundIntoOneTrackSectionWhichIsATurn(){
        List<Round> roundList = new ArrayList<Round>();
        int duration = 0;
        for(TrackSection trackSection : round1.getTrackSections()){
            duration += trackSection.getDuration();
        }
        roundList.add(round1);
        trackAnalyzer.removeFaultGoingStraightTrackSections(roundList, round1TimeStraight + 10); //Should remove all Straights
        for(TrackSection trackSection : round1.getTrackSections()){
            assertEquals(State.TURN, trackSection.getDirection());
        }
        assertNotEquals("Has Removed Everything",0, round1.getCountOfTrackSections());
        trackAnalyzer.mergeAndRecalculateTrackSections(roundList);
        assertEquals(1, round1.getCountOfTrackSections());
        assertEquals("Should have the same duration as before.",duration, round1.getTrackSections().get(0).getDuration());
    }
}
