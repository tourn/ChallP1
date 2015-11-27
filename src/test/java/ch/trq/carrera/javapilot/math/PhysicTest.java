package ch.trq.carrera.javapilot.math;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Direction;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        straight = new TrackSection(Direction.STRAIGHT,0);
        turn = new TrackSection(Direction.LEFT,0);
        physicModel = new PhysicModel();
    }

    /*@Test
    public void testStraight1(){
        double v0 = 106.4834427832393;
        double v1 = 208.08107963611837;
        int power = 102;
        long time = 2875;
        straight.setFriction(0.000001);
        double v = physicModel.getVelocity(v0,straight,power,time);
        LOGGER.info("expected: " + v1 + "cm/s, calculated: " + v + "cm/s, difference: " + Math.abs(v1-v) + "cm/s");
        assertTrue(Math.abs(v1 - v) < 10.0);
    }*/

    @Test
    public void testStraight1(){
        double v0 = 110.408;
        double v1 = 230.192;
        int power = 110;
        long time = 4190;
        straight.setFriction((0.06173246));
        physicModel.setE(138.0167*1.05);
        double v = physicModel.getVelocity(v0,straight,power,time);
        LOGGER.info("S1.1 - expected: " + v1 + "cm/s, calculated: " + v + "cm/s, difference: " + Math.abs(v1-v) + "cm/s");
        assertTrue(Math.abs(v1 - v) < 10.0);
    }
    @Test
    public void testStraight2(){
        double v0 = 230.192;
        double v1 = 233.178;
        int power = 110;
        long time = 601;
        straight.setFriction((0.06173246));
        physicModel.setE(138.0167*1.05);
        double v = physicModel.getVelocity(v0,straight,power,time);
        LOGGER.info("S1.2 - expected: " + v1 + "cm/s, calculated: " + v + "cm/s, difference: " + Math.abs(v1-v) + "cm/s");
        assertTrue(Math.abs(v1 - v) < 10.0);
    }
    @Test
    public void testStraight3(){
        double v0 = 126.4250552157755;
        double v1 = 212.28289842110826;
        int power = 110;
        long time = 1799;
        straight.setFriction((0.06173246));
        physicModel.setE(138.0167*1.05);
        double v = physicModel.getVelocity(v0,straight,power,time);
        LOGGER.info("S2 - expected: " + v1 + "cm/s, calculated: " + v + "cm/s, difference: " + Math.abs(v1-v) + "cm/s");
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
