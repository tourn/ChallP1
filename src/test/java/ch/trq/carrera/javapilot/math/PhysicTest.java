package ch.trq.carrera.javapilot.math;

import ch.trq.carrera.javapilot.akka.trackanalyzer.State;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Frank on 09.11.2015.
 */
public class PhysicTest {
    TrackSection straight;
    TrackSection turn;
    PhysicModel physicModel;

    private final Logger LOGGER = LoggerFactory.getLogger(PhysicTest.class);

    @Before
    public void initialize(){
        straight = new TrackSection(State.STRAIGHT,0);
        turn = new TrackSection(State.LEFT,0);
        physicModel = new PhysicModel();
    }

    @Test
    public void testStraight1(){
        double v0 = 106.4834427832393;
        double v1 = 208.08107963611837;
        int power = 102;
        long time = 2875;
        straight.setFriction(0.000001);
        double v = physicModel.getVelocity(v0,straight,power,time);
        LOGGER.info("expected: " + v1 + "cm/s, calculated: " + v + "cm/s, difference: " + Math.abs(v1-v) + "cm/s");
        assertTrue(Math.abs(v1 - v) < 10.0);
    }

    @Test
    public void testTurn1(){
        double v0 = 209.16770840785466;
        double v1 = 106.4834427832393;
        int power = 102;
        double time = 1656;
    }
}
