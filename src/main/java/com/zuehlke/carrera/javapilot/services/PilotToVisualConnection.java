package com.zuehlke.carrera.javapilot.services;

import ch.trq.carrera.javapilot.akka.messages.NewRoundUpdate;
import ch.trq.carrera.javapilot.positiontracker.SectionUpdate;
import ch.trq.carrera.javapilot.trackanalyzer.Track;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

/**
 * Created by mario on 08.10.15.
 */
public interface PilotToVisualConnection {
    void send(SensorEvent message);
    void send(VelocityMessage message);
    void initializeTrack(Track track);
    void sectionUpdate(SectionUpdate update);
    void carUpdate(int trackSectionIndex, double percentageDistance);
    void newRoundMessage(NewRoundUpdate message);
    void reset();
}
