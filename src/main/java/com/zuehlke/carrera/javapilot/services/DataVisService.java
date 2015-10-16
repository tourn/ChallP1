package com.zuehlke.carrera.javapilot.services;

import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import org.jfree.ui.RefineryUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import visualization.DataChart;

import javax.annotation.PostConstruct;
import javax.xml.crypto.Data;
import java.net.ServerSocket;

/**
 * Manages Data Visualization instance
 */
@Service
@EnableScheduling
@EnableAsync
public class DataVisService {

    private final DataChart visualizer;

    public DataVisService(){
        System.setProperty("java.awt.headless", "false");
        //Create DataChart Swing Application
        visualizer = new DataChart("Sensor Data");
        visualizer.pack();
        RefineryUtilities.centerFrameOnScreen(visualizer);
        visualizer.setVisible(true);
    }

    public DataChart getVisualizer(){
        return visualizer;
    }
}
