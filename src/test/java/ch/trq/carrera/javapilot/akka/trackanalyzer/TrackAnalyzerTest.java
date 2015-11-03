package ch.trq.carrera.javapilot.akka.trackanalyzer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Frank on 03.11.2015.
 */
public class TrackAnalyzerTest {

    private TrackAnalyzer trackAnalyzer;

    @Before
    public void initialize(){
        trackAnalyzer = new TrackAnalyzer();
    }

    @Test
    public void newRoundTestShouldSetIsNewRoundFromFalseToTrue() {
        assertFalse(trackAnalyzer.isNewRound);
        trackAnalyzer.newRound(0,0);
        assertTrue(trackAnalyzer.isNewRound);
    }

    @Test
    public void addTrackSectionToRoundShouldSetIsNewRoundToFalseWhenItIsTrue() {
        trackAnalyzer.newRound(0, 0);
        assertTrue(trackAnalyzer.isNewRound);
        trackAnalyzer.addTrackSectionToRound(State.TURN, 0);
        assertFalse(trackAnalyzer.isNewRound);
    }
}
