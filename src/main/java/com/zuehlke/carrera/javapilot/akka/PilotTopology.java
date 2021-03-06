package com.zuehlke.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

/**
 *      creates the complete topology and provides a Map of well-defined entry-points
 */
public class PilotTopology {

    public static final String SENSOR_ENTRYPOINT = "SENSOR_ENTRYPOINT";
    public static final String VELOCITY_ENTRYPOINT = "VELOCITY_ENTRYPOINT";
    public static final String PENALTY_ENTRYPOINT = "PENALTY_ENTRYPOINT";
    public static final String ROUNDTIME_ENTRYPOINT = "ROUNDTIME_ENTRYPOINT";

    private final ActorSystem system;
    private final ActorRef kobayashi;
    private final Map<String, ActorRef> entryPoints = new HashMap<>();

    public PilotTopology(ActorRef kobayashi, ActorSystem system) {
        this.kobayashi = kobayashi;
        this.system = system;
    }

    public Map<String, ActorRef> create(Props props) {
        ActorRef initialProcessor = system.actorOf(props);
        entryPoints.put(PENALTY_ENTRYPOINT, initialProcessor);
        entryPoints.put(SENSOR_ENTRYPOINT, initialProcessor);
        entryPoints.put(VELOCITY_ENTRYPOINT, initialProcessor);
        entryPoints.put(ROUNDTIME_ENTRYPOINT, initialProcessor);
        return entryPoints;
    }

}
