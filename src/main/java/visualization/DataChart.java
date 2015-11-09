package visualization;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

public class DataChart extends ApplicationFrame {

    private final JPanel panel2;
    private final JPanel container;
    private final JFreeChart chart;
    private final XYSeriesCollection datasetGyroZ;
    //speed not shown at the moment
    private final XYSeriesCollection datasetSpeed;
    private JTable table;
    private Track track;
    /**
     * The time series data.
     */
    private long absolut_time = -1;
    private XYSeries firstPhaseSerie;
    private XYSeries secondPhaseSerie;
    private XYSeries tmpSeries;
    private DefaultTableModel model;
    private Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
    private ArrayList<Rectangle2D.Double> checkpoints = new ArrayList<>();
    private double holeduration = 0;
    private boolean first = true;
    private StandardXYItemRenderer renderer;
    private int[] sectionbegins;
    private XYSeries speedSeries;

    /**
     * @param title the frame title.
     */

    public DataChart(final String title) {

        super(title);
        setResizable(false);
        this.firstPhaseSerie = new XYSeries("Sensor Z Model");
        this.secondPhaseSerie = new XYSeries("Sensor Z Round");
        this.speedSeries = new XYSeries("Speed");
        datasetGyroZ = new XYSeriesCollection(this.firstPhaseSerie);
        datasetGyroZ.addSeries(this.secondPhaseSerie);
        //speed not shown at the moment
        datasetSpeed = new XYSeriesCollection(this.speedSeries);
        chart = createChart(datasetGyroZ);
        JFrame trackframe = new JFrame();
        trackframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final ChartPanel chartPanelModel = new ChartPanel(chart);

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
                //print CarPos
                g.fill(rect);
                //print Checkpoints
                myColor = new Color(0, 102, 255, 180);
                g.setColor(myColor);
                for (Rectangle2D check : checkpoints) {
                    g.fill(check);
                }
            }
        };

        chartPanelModel.setPreferredSize(new Dimension(1200, 450));
        //chartPanelRound.setPreferredSize(new Dimension(1200, 450));
        panel1.add(chartPanelModel);
        //panel1.add(chartPanelRound);
        panel1.setPreferredSize(new Dimension(1200, 500));
        panel2.setPreferredSize(new Dimension(1400, 500));

        container.add(panel1);

        trackframe.setContentPane(panel2);
        trackframe.pack();
        trackframe.setVisible(true);

        setContentPane(container);
    }

    public void initDataTable(Track track) {
        this.track = track;
        model = new DefaultTableModel();
        holeduration = 0;
        Object[] objects = new Object[track.getSections().size()];
        for (int i = 0; i < track.getSections().size(); i++) {
            model.addColumn(i + ": " + Math.round(track.getSections().get(i).getDistance()) + " - " + track.getSections().get(i).getDirection());
            objects[i] = track.getSections().get(i).getDuration();
            holeduration += track.getSections().get(i).getDuration();
        }
        table = new JTable(model);
        resizeTableColumn();
        panel2.add(table, BorderLayout.CENTER);
        panel2.add(table.getTableHeader(), BorderLayout.NORTH);
        model.addRow(objects);
        insertCheckpoints();
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
            model.insertRow(0, objects);
        } else {
            model.setValueAt(section.getDuration(), 0, index);
        }
    }

    public void resetDataChart() {
        secondPhaseSerie.clear();
        firstPhaseSerie.clear();
        tmpSeries.clear();
        tmpSeries = firstPhaseSerie;
        first = true;
        absolut_time = -1;
        resetTable();
    }

    private void resetTable() {
        panel2.removeAll();
        checkpoints.clear();
        rect.setRect(0, 0, 0, 0);
        panel2.repaint();
    }


    public void newRoundMessage() {
        if (first) {
            first = false;
        } else {
            renderer = new StandardXYItemRenderer();
            XYPlot plot = chart.getXYPlot();
            renderer.setSeriesPaint(1, Color.BLUE);
            plot.setRenderer(1, renderer);
            tmpSeries = secondPhaseSerie;
            secondPhaseSerie.clear();
            absolut_time = -1;
        }
    }

    public void insertCheckpoints() {
        int xtable = table.getX();
        int ytable = table.getY();
        double xrectl = 0;
        double xrectr = 10;
        int sectionwidth = 0;
        List<Track.Position> checkpoints = track.getCheckpoints();

        sectionbegins = new int[track.getSections().size()];

        for (int i = 0; i < track.getSections().size(); i++) {
            if (i > 0) {
                sectionbegins[i] = sectionbegins[i - 1] + table.getColumnModel().getColumn(i - 1).getMinWidth();
            } else {
                sectionbegins[i] = 0;
            }
        }

        for (Track.Position p : checkpoints) {
            sectionwidth = table.getColumnModel().getColumn(track.getSections().indexOf(p.getSection())).getMinWidth();
            xrectl = xtable + sectionbegins[track.getSections().indexOf(p.getSection())] + p.getPercentage() * sectionwidth;
            this.checkpoints.add(new Rectangle2D.Double(xrectl, ytable - 30, xrectr, ytable + 50));
        }
    }

    public void updateCarPosition(int tracksection, double percentageDistance) {
        int xtable = table.getX();
        int ytable = table.getY();
        int sectionwidth = table.getColumnModel().getColumn(tracksection).getMinWidth();


        double xrectl = xtable + sectionbegins[tracksection] + percentageDistance * sectionwidth;
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
        ((DateAxis) axis).setDateFormatOverride(new SimpleDateFormat("ss"));
        axis = plot.getRangeAxis();
        axis.setRange(-5000, 6000);
        tmpSeries = this.firstPhaseSerie;
        return result;
    }

    public void insertSpeedData(VelocityMessage message) {
        this.speedSeries.add(message.getTimeStamp(), message.getVelocity() * 10);
    }

    public void insertSensorData(SensorEvent message) {
        if (!first) {
            if (absolut_time == -1) {
                absolut_time = message.getTimeStamp();
            }
            this.tmpSeries.add(Math.abs(absolut_time - message.getTimeStamp()), message.getG()[2]);
        }
    }
}
