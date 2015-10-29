package com.zuehlke.carrera.javapilot.services;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

import java.util.ArrayList;

/**
 * Created by mario on 08.10.15.
 */
public interface PilotToVisualConnection {
    void send(SensorEvent message);
    void send(VelocityMessage message);
    void initializeTrack(Track track);
    void initializeCheckpoints(ArrayList<Track.Position> checkpoints);
    void sectionUpdate(int index, TrackSection section);
    void carUpdate(int trackSectionIndex, int offset, double percentageDistance);
    void newRoundMessage(RoundTimeMessage message);
}
