package com.zuehlke.carrera.javapilot.services;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import visualization.DataChart;

/**
 * Created by mario on 08.10.15.
 */
public class PilotToVisualConnector implements PilotToVisualConnection{

    private DataChart dataChart;

    public PilotToVisualConnector(DataChart dataChart){
        this.dataChart = dataChart;
    }

    @Override
    public void send(SensorEvent message) {
        dataChart.insertSensorData(message);
    }

    @Override
    public void send(VelocityMessage message) {
        dataChart.insertSpeedData(message);
    }

    @Override
    public void initializeTrack(Track track) {
        dataChart.initDataTable(track);
    }

    @Override
    public void sectionUpdate(int index, TrackSection section) {
        dataChart.updateDataTable(index, section);
    }

    @Override
    public void carUpdate(int trackSectionIndex, int offset) {
        dataChart.updateCarPosition(trackSectionIndex, offset);
    }
}