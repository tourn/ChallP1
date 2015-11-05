package ch.trq.carrera.javapilot.math;

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
    private final float e;

    // kinetic friction factor on straights in kg/s
    private final float kfs;

    // kinetic friction factor on a curve
    private final float kfc;

    // static friction constant
    private final float sfc;

    // static friction factor
    private final float sff;

    // mass of the car in g
    private final float m;

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
        float p_min_straight = 100;
        float p_min_curve = 120;
        float p_for_vmax400 = 140;
        float ri = 1 / 30.0f;
        float dt_for_acc_to100_at_p150 = 0.5f;

        m = 300.0f;
        e = m * 100 / dt_for_acc_to100_at_p150 / 50.0f;
        kfs = p_for_vmax400 / 300.0f * e;
        sfc = p_min_straight * e;

        // from characteristic 5;
        kfc = 1.2f * kfs / ri;

        sff = (p_min_curve * e - sfc) / ri;
    }

    public TrackPhysicsModel(float motor_efficiency, float friction_factor,
                             float curve_friction_factor, float static_friction_constant, float static_friction_factor,
                             float mass) {
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
    public float average_velocity(float v0, float ri, int p, float dt) {
        return v0 + acceleration(v0, ri, p) * dt / 2;
    }

    /**
     * @param v0 the speed at the beginning of the period, given
     * @param ri the inverse radius of the curve
     * @param p  the digital power value at the start of the period
     * @return the acceleration of the car given
     */
    public float acceleration(float v0, float ri, int p) {
        return total_force(p, v0, ri) / m;
    }

    /**
     * @param v0 the speed at the beginning of the period, given
     * @param ri the inverse radius of the curve
     * @param p  the digital power value at the start of the period
     * @return the total force active on the car, given
     */
    public float total_force(int p, float v0, float ri) {
        if (v0 == 0) {
            return Math.max(motor_force(p) - friction_force(v0, ri), 0);
        } else {
            float fm = motor_force(p);
            float ff = friction_force(v0, ri);
            return fm - ff;
        }
    }

    /**
     * @param p the digital power value at the start of the period
     * @return the physical force of the motor, given
     */
    public float motor_force(int p) {
        return p * e;
    }

    /**
     * @param v0 the speed at the start of the period
     * @param ri the inverse radius at the start of the period
     * @return the total friction force with noise applied, given
     */
    public float friction_force(float v0, float ri) {
        float kffc = kinetic_friction_force_curve(ri, v0);
        float kffs = kinetic_friction_force_straight(v0);
        float kinetic_friction = kffc + kffs;
        float sf = static_friction(ri, v0);
        return kinetic_friction + sf;
    }

    public float static_friction(float ri, float v) {
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
    public float kinetic_friction_force_straight(float v0) {
        return kfs * v0;
    }

    /**
     * @param ri the inverse radius
     * @param v  the velocity of the car
     * @return the physical friction force in a curve section, given
     */
    public float kinetic_friction_force_curve(float ri, float v) {
        return kfc * Math.abs(ri) * v;
    }
}
