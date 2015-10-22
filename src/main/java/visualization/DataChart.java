package visualization;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesTableModel;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import sun.rmi.runtime.Log;

public class DataChart extends ApplicationFrame {

    private final JPanel panel2;
    private final JPanel container;
    private JTable table;
    private Track track;
    /**
     * The time series data.
     */
    private XYSeries series;
    private XYSeries speedSeries;
    private XYPlot plot;
    private XYSeriesCollection speeddata;
    private DefaultTableModel model;
    private Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);

    /**
     * @param title the frame title.
     */

    public DataChart(final String title) {

        super(title);
        this.series = new XYSeries("Sensor Z");
        this.speedSeries = new XYSeries("Speed");
        final XYSeriesCollection dataset = new XYSeriesCollection(this.series);
        speeddata = new XYSeriesCollection(this.speedSeries);
        final JFreeChart chart = createChart(dataset);

        final ChartPanel chartPanel = new ChartPanel(chart);

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));

        JPanel panel1 = new JPanel();
        panel2 = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics graphics) {
                super.paint(graphics);
                Graphics2D g = (Graphics2D) graphics;
                Color myColor = new Color(255, 53,51, 180 );
                g.setColor(myColor);
                g.fill(rect);
            }
        };

        chartPanel.setPreferredSize(new java.awt.Dimension(600, 900));
        panel1.add(chartPanel);
        panel2.setPreferredSize(new java.awt.Dimension(900, 900));

        container.add(panel1);
        container.add(panel2);

        setContentPane(container);
    }

    public void initDataTable(Track track) {
        model = new DefaultTableModel();
        Object[] objects = new Object[track.getSections().size()];
        for (int i = 0; i < track.getSections().size(); i++) {
            model.addColumn(i + ": " + track.getSections().get(i).getDirection());
            objects[i] = track.getSections().get(i).getDuration();
        }
        model.addRow(objects);
        table = new JTable(model);
        panel2.add(table, BorderLayout.CENTER);
        panel2.add(table.getTableHeader(), BorderLayout.NORTH);
        setContentPane(container);
        int xtable = table.getX();
        int ytable = table.getY();
        rect.setRect(xtable, ytable-10, xtable + 10, ytable + 15);
        panel2.repaint();
    }

    public void updateDataTable(int index, TrackSection section) {
        if (index == 0) {
            Object[] objects = {section.getDuration()};
            model.insertRow(0, objects);
        } else {
            model.setValueAt(section.getDuration(), 0, index);
        }
        table.repaint();
    }

    public void updateCarPosition(int tracksection, int offset) {
        int twidth = table.getWidth();
        int xtable = table.getX();
        int ytable = table.getY();
        int sectionwidth = twidth / model.getColumnCount();

        double prozentualoffeset = offset / track.getSections().get(tracksection).getDuration();

        if(offset <= track.getSections().get(tracksection).getDuration()){
            rect.setRect(xtable + prozentualoffeset*sectionwidth, ytable-10, xtable + 10 + prozentualoffeset*sectionwidth, ytable + 15);
        }
    }


    /**
     * Creates a sample chart.
     *
     * @param dataset the dataset.
     * @return A sample chart.
     */
    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
                "Sensor Data",
                "Time",
                "Value",
                dataset,
                true,
                true,
                false
        );
        plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(-5000, 5000);

        plot.setDataset(1, speeddata);
        plot.setRenderer(1, new StandardXYItemRenderer());

        return result;
    }

    public void insertSpeedData(VelocityMessage message) {
        this.speedSeries.add(message.getTimeStamp(), message.getVelocity() * 10);
    }

    public void insertSensorData(SensorEvent message) {
        this.series.add(message.getTimeStamp(), message.getG()[2]);
    }

}
