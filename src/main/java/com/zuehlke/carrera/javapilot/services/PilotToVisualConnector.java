package com.zuehlke.carrera.javapilot.services;

import ch.trq.carrera.javapilot.akka.messages.NewRoundUpdate;
import ch.trq.carrera.javapilot.positiontracker.SectionUpdate;
import ch.trq.carrera.javapilot.trackanalyzer.Track;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import ch.trq.carrera.javapilot.visualization.DataChart;

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
    public void sectionUpdate(SectionUpdate update) {
        dataChart.updateDataTable(update);
    }


    @Override
    public void carUpdate(int trackSectionIndex, double percentageDistance) {
        dataChart.updateCarPosition(trackSectionIndex, percentageDistance);
    }

    @Override
    public void newRoundMessage(NewRoundUpdate message) {
        dataChart.newRoundMessage(message);
    }

    @Override
    public void reset() {
        dataChart.resetDataChart();
    }
}
