package com.zuehlke.carrera.javapilot.services;

import com.zuehlke.carrera.relayapi.messages.SensorEvent;

import java.util.ArrayList;

/**
 * Created by mario on 08.10.15.
 */
public interface PilotToVisualConnection {
    void send(SensorEvent message);
    void sendTrackData();
}
