package com.zuehlke.carrera.javapilot.services;

import com.zuehlke.carrera.relayapi.messages.SensorEvent;
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
    public void sendTrackData() {
        //TODO
    }
}
