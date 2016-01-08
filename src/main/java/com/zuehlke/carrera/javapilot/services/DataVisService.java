package com.zuehlke.carrera.javapilot.services;

import org.jfree.ui.RefineryUtilities;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import ch.trq.carrera.javapilot.visualization.DataChart;

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
    }

    public DataChart getVisualizer(){

        return visualizer;
    }
}
