package visualization;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
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
    private final JFreeChart chartModel;
    private final JFreeChart chartRound;
    private final XYSeriesCollection datasetRound;
    private final XYSeriesCollection datasetModel;
    private JTable table;
    private Track track;
    private int second = 0;
    /**
     * The time series data.
     */
    private long absolut_time = -1;
    private XYSeries series;
    private XYSeries secondPhaseSerie;
    private XYSeries tmpSeries;
    private XYSeries speedSeries;
    private XYSeriesCollection speeddata;
    private DefaultTableModel model;
    private Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
    private double holeduration = 0;
    private boolean notfirst = false;

    /**
     * @param title the frame title.
     */

    public DataChart(final String title) {

        super(title);
        this.series = new XYSeries("Sensor Z Model");
        this.secondPhaseSerie = new XYSeries("Sensor Z Round");
        this.speedSeries = new XYSeries("Speed");
        datasetModel = new XYSeriesCollection(this.series);
        datasetRound = new XYSeriesCollection(this.secondPhaseSerie);
        speeddata = new XYSeriesCollection(this.speedSeries);
        chartModel = createChart(datasetModel);
        chartRound = createChart(datasetRound);
        JFrame trackframe = new JFrame();
        trackframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final ChartPanel chartPanelModel = new ChartPanel(chartModel);
        final ChartPanel chartPanelRound = new ChartPanel(chartRound);

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));

        JPanel panel1 = new JPanel();
        panel2 = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics graphics) {
                super.paint(graphics);
                Graphics2D g = (Graphics2D) graphics;
                Color myColor = new Color(255, 53, 51, 180);
                g.setColor(myColor);
                g.fill(rect);
            }
        };

        chartPanelModel.setPreferredSize(new java.awt.Dimension(1200, 450));
        chartPanelRound.setPreferredSize(new java.awt.Dimension(1200, 450));
        panel1.add(chartPanelModel);
        panel1.add(chartPanelRound);
        panel1.setPreferredSize(new java.awt.Dimension(1200, 900));
        panel2.setPreferredSize(new java.awt.Dimension(1200, 900));

        container.add(panel1);

        trackframe.setContentPane(panel2);
        trackframe.pack();
        trackframe.setVisible(true);

        setContentPane(container);
    }

    public void initDataTable(Track track) {
        this.track = track;
        model = new DefaultTableModel();
        Object[] objects = new Object[track.getSections().size()];
        for (int i = 0; i < track.getSections().size(); i++) {
            model.addColumn(i + ": " + track.getSections().get(i).getDirection());
            objects[i] = track.getSections().get(i).getDuration();
            holeduration += track.getSections().get(i).getDuration();
        }
        model.addRow(objects);
        table = new JTable(model);
        resizeTableColumn();
        panel2.add(table, BorderLayout.CENTER);
        panel2.add(table.getTableHeader(), BorderLayout.NORTH);
        panel2.repaint();
    }

    public void resizeTableColumn() {
        double durationWidth;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < model.getColumnCount(); i++) {
            durationWidth = (double) track.getSections().get(i).getDuration() / holeduration;
            int columnwidth = (int) ((double) panel2.getWidth() * durationWidth);
            table.getColumnModel().getColumn(i).setMinWidth(columnwidth);
        }
    }

    public void updateDataTable(int index, TrackSection section) {
        if (index == 0) {
            Object[] objects = {section.getDuration()};
            resizeTableColumn();
            model.insertRow(0, objects);
        } else {
            model.setValueAt(section.getDuration(), 0, index);
        }
    }

    public void newRoundMessage(RoundTimeMessage m) {
        if (notfirst) {
            tmpSeries = secondPhaseSerie;
            if (second == 2) {
                XYPlot plot = chartModel.getXYPlot();
                secondPhaseSerie.clear();
                plot.setDataset(1, datasetRound);
                plot.setRenderer(1, new StandardXYItemRenderer());
                absolut_time = -1;
            }
        } else {
            notfirst = true;
        }
        second++;
    }

    public void updateCarPosition(int tracksection, int offset) {
        int twidth = table.getWidth();
        int xtable = table.getX();
        int ytable = table.getY();
        int sectionwidth = twidth / table.getColumnCount();

        double prozentualoffeset = (double) offset / (double) track.getSections().get(tracksection).getDuration();

        double xrectl = xtable + tracksection * sectionwidth + prozentualoffeset * sectionwidth;
        double xrectr = 10;

        rect.setRect(xrectl, ytable - 10, xrectr, ytable + 15);
        panel2.repaint();
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
        XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        ((DateAxis)axis).setDateFormatOverride(new SimpleDateFormat("ss"));
        axis = plot.getRangeAxis();
        axis.setRange(-5000, 6000);
        tmpSeries = this.series;
        return result;
    }


    public void insertSpeedData(VelocityMessage message) {
        this.speedSeries.add(message.getTimeStamp(), message.getVelocity() * 10);
    }

    public void insertSensorData(SensorEvent message) {
        if (notfirst) {
            if (absolut_time == -1) {
                absolut_time = message.getTimeStamp();
            }
            if(second <= 3){
                this.tmpSeries.add(Math.abs(absolut_time - message.getTimeStamp()), message.getG()[2]);
            }
        }
    }

}
