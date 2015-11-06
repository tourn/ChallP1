package ch.trq.carrera.javapilot.math;

import ch.trq.carrera.javapilot.akka.trackanalyzer.State;

/**
 * a very simple model of the physical track properties
 */
public class TrackPhysicsModel {

    /*
     *  These parameters are adjusted empirically so that the behaviour of
     *  the simulator matches the real track. They're not intended to be measured,
     *  although, theoretically that would be an option
     */
    // motor efficicency in kgcm/s2
    private final double e;

    // kinetic friction factor on straights in kg/s
    private final double kfs;

    // kinetic friction factor on a curve
    private final double kfc;

    // static friction constant
    private final double sfc;

    // static friction factor
    private final double sff;

    // mass of the car in g
    private final double m;
    private double turn_radius;

    /**
     * a reasonable set of physical constants with the following observable characteristics
     * <p>
     * 1) on a straight section, the car needs at least p = 100 to overcome static friction
     * thus determines sfc
     * 2) on a curve section of r=30cm, that is p = 120
     * thus determines sff
     * 3) the frictional noise is at 10%
     * 4) at p = 140, the max speed on a straight is at 300cm/s
     * thus determines kfs
     * 5) the friction on a curve of 30cm is such that it brings the car from 200 to 100 at the end of the curve
     * thus determines kfc (empirically)
     * 6) With negligible friction, the car would accelerate from 0 to 100cm/s within 0.1 sec with p = 50;
     * thus determines e
     */
    public TrackPhysicsModel() {
        double p_min_straight = 100;
        double p_min_curve = 120;
        double p_for_vmax400 = 140;
        double ri = 1 / 30.0f;
        double dt_for_acc_to100_at_p150 = 0.5f;

        turn_radius = ri;
        m = 300.0f;
        e = m * 100 / dt_for_acc_to100_at_p150 / 50.0f;
        kfs = p_for_vmax400 / 300.0f * e;
        sfc = p_min_straight * e;

        // from characteristic 5;
        kfc = 1.2f * kfs / ri;

        sff = (p_min_curve * e - sfc) / ri;
    }

    public TrackPhysicsModel(double motor_efficiency, double friction_factor,
                             double curve_friction_factor, double static_friction_constant, double static_friction_factor,
                             double mass) {
        this.e = motor_efficiency;
        this.kfs = friction_factor;
        this.kfc = curve_friction_factor;
        this.sfc = static_friction_constant;
        this.sff = static_friction_factor;
        this.m = mass;
    }

    /**
     * @param v0 the speed at the beginning of the period
     * @param ri the inverse radius of the curve
     * @param p  the digital power value at the start of the period
     * @param dt timespan in ms
     * @return the average velocity during a given period of time, given
     */
    public double average_velocity(double v0, double ri, int p, double dt) {
        return v0 + acceleration(v0, ri, p) * dt / 2;
    }

    public double average_velocity(double v0, State turn, int p, double dt) {
        double ri =  State.STRAIGHT == turn ? 0 : turn_radius;
        return average_velocity(v0, ri, p, dt);
    }

    public double velocity(double v0, State turn, int p, double dt) {
        double ri =  State.STRAIGHT == turn ? 0 : turn_radius;
        return v0 + acceleration(v0, ri, p) * dt;
    }

    public double distance(double v0, State turn, int p, double dt){
        double ri =  State.STRAIGHT == turn ? 0 : turn_radius;
        double v = average_velocity(v0,ri,p,dt);
        return v*dt;
    }

    /**
     * @param v0 the speed at the beginning of the period, given
     * @param ri the inverse radius of the curve
     * @param p  the digital power value at the start of the period
     * @return the acceleration of the car given
     */
    public double acceleration(double v0, double ri, int p) {
        return total_force(p, v0, ri) / m;
    }

    /**
     * @param v0 the speed at the beginning of the period, given
     * @param ri the inverse radius of the curve
     * @param p  the digital power value at the start of the period
     * @return the total force active on the car, given
     */
    public double total_force(int p, double v0, double ri) {
        if (v0 == 0) {
            return Math.max(motor_force(p) - friction_force(v0, ri), 0);
        } else {
            double fm = motor_force(p);
            double ff = friction_force(v0, ri);
            return fm - ff;
        }
    }

    /**
     * @param p the digital power value at the start of the period
     * @return the physical force of the motor, given
     */
    public double motor_force(int p) {
        return p * e;
    }

    /**
     * @param v0 the speed at the start of the period
     * @param ri the inverse radius at the start of the period
     * @return the total friction force with noise applied, given
     */
    public double friction_force(double v0, double ri) {
        double kffc = kinetic_friction_force_curve(ri, v0);
        double kffs = kinetic_friction_force_straight(v0);
        double kinetic_friction = kffc + kffs;
        double sf = static_friction(ri, v0);
        return kinetic_friction + sf;
    }

    public double static_friction(double ri, double v) {
        if (v <= 0) {
            return sff * ri + sfc;
        } else {
            return 0;
        }
    }

    /**
     * @param v0 the velocity of the car
     * @return the physical friction force on a straight section, given
     */
    public double kinetic_friction_force_straight(double v0) {
        return kfs * v0;
    }

    /**
     * @param ri the inverse radius
     * @param v  the velocity of the car
     * @return the physical friction force in a curve section, given
     */
    public double kinetic_friction_force_curve(double ri, double v) {
        return kfc * Math.abs(ri) * v;
    }
}
