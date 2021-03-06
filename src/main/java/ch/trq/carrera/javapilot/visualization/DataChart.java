package ch.trq.carrera.javapilot.visualization;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ch.trq.carrera.javapilot.akka.messages.NewRoundUpdate;
import ch.trq.carrera.javapilot.positiontracker.SectionUpdate;
import ch.trq.carrera.javapilot.trackanalyzer.Track;
import ch.trq.carrera.javapilot.trackanalyzer.TrackSection;
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
    private final JFrame trackframe;
    private JTable table;
    private Track track;

    private long absolut_time = -1;

    private XYSeries firstPhaseSerie;
    private XYSeries secondPhaseSerie;
    private XYSeries speedSeries;
    private XYSeries runningSerie;
    private DefaultTableModel model;

    private Rectangle2D.Float rect = new Rectangle2D.Float(0, 0, 0, 0);
    private ArrayList<Rectangle2D.Double> checkpoints = new ArrayList<>();
    private double totalDuration = 0;
    private boolean trackanalyzerphase = true;
    private StandardXYItemRenderer renderer;
    private int[] sectionbegins;

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

        trackframe = new JFrame();
        trackframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        trackframe.setResizable(false);

        //temporary disabled
        this.setVisible(false);
        chart = createChart(datasetGyroZ);

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
        trackframe.setVisible(false);
        setVisible(false);

        setContentPane(container);
    }

    public void initDataTable(Track track) {
        this.track = track;
        model = new DefaultTableModel();
        totalDuration = 0;
        Object[] objects = new Object[track.getSections().size() + 1];
        for (int i = 0; i < track.getSections().size(); i++) {
            model.addColumn(i + ": " + Math.round(track.getSections().get(i).getDistance()) + " - " + track.getSections().get(i).getDirection());
            totalDuration += track.getSections().get(i).getDuration();
        }
        model.addColumn("RoundTime");
        table = new JTable(model);
        model.addRow(objects);
        table.setDefaultRenderer(Object.class, new CustomRenderer());
        resizeTableColumn();
        panel2.add(table, BorderLayout.CENTER);
        panel2.add(table.getTableHeader(), BorderLayout.NORTH);
        insertCheckpoints();
        DefaultTableCellRenderer colorColumn = new DefaultTableCellRenderer();
        colorColumn.setBackground(Color.GREEN);
        table.getColumnModel().getColumn(model.getColumnCount() - 1).setCellRenderer(colorColumn);
        setFramesVisible();
        panel2.repaint();
    }

    private void setFramesVisible() {
        trackframe.setVisible(true);
    }

    public void resizeTableColumn() {
        double durationWidth;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < model.getColumnCount() - 1; i++) {
            durationWidth = (double) track.getSections().get(i).getDuration() / totalDuration;
            int columnwidth = (int) ((double) (panel2.getWidth() - 100) * durationWidth);
            table.getColumnModel().getColumn(i).setMinWidth(columnwidth);
        }
        table.getColumnModel().getColumn(track.getSections().size()).setMinWidth(100);
    }

    class CustomRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null) {
                if (value.toString().contains("!")) {
                    c.setBackground(new java.awt.Color(255, 72, 72));
                }else{
                    c.setBackground(new java.awt.Color(214, 245, 245));
                }
            }else{
                c.setBackground(new java.awt.Color(214, 245, 245));
            }

            return c;
        }
    }

    public void updateDataTable(SectionUpdate update) {
        int sectionIndex = update.getSectionIndex();
        TrackSection section = update.getSection();
        if (sectionIndex == 0) {
            if (model.getValueAt(0, model.getColumnCount() - 1) == null) {
                model.setValueAt(totalDuration, 0, model.getColumnCount() - 1);
            }
            Object[] objects = {formatSectionUpdate(update)};
            model.insertRow(0, objects);
            totalDuration = 0;
        } else {
            totalDuration += section.getDuration();
            model.setValueAt(formatSectionUpdate(update), 0, sectionIndex);
        }
    }

    private String formatSectionUpdate(SectionUpdate update) {
        TrackSection section = update.getSection();
        String cellInput = section.getDuration() + " @ " + update.getPowerInSection();
        if (update.isPenaltyOccured()) {
            cellInput += "!";
        }
        return cellInput;
    }

    public void resetDataChart() {
        secondPhaseSerie.clear();
        firstPhaseSerie.clear();
        runningSerie.clear();
        runningSerie = firstPhaseSerie;
        trackanalyzerphase = true;
        absolut_time = -1;
        resetTable();
    }

    private void resetTable() {
        panel2.removeAll();
        checkpoints.clear();
        rect.setRect(0, 0, 0, 0);
        panel2.repaint();
    }

    public void newRoundMessage(NewRoundUpdate m) {
        if (trackanalyzerphase) {
            trackanalyzerphase = false;
        } else {
            renderer = new StandardXYItemRenderer();
            XYPlot plot = chart.getXYPlot();
            renderer.setSeriesPaint(1, Color.BLUE);
            plot.setRenderer(1, renderer);
            runningSerie = secondPhaseSerie;
            secondPhaseSerie.clear();
            absolut_time = -1;
        }
    }

    public void insertCheckpoints() {
        int xtable = table.getX();
        int ytable = table.getY();
        double xrectl = 0;
        double xrectr = 6;
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
            this.checkpoints.add(new Rectangle2D.Double(xrectl - 5, ytable - 30, xrectr, ytable + 70));
        }
    }

    public void updateCarPosition(int sectionIndex, double percentageDistance) {
        int xtable = table.getX();
        int ytable = table.getY();
        int sectionwidth = table.getColumnModel().getColumn(sectionIndex).getMinWidth();


        double xrectl = xtable + sectionbegins[sectionIndex] + percentageDistance * sectionwidth;
        double xrectr = 10;

        rect.setRect(xrectl, ytable - 10, xrectr, ytable + 15);
        panel2.repaint();
    }

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
        runningSerie = this.firstPhaseSerie;
        return result;
    }

    public void insertSpeedData(VelocityMessage message) {
        this.speedSeries.add(message.getTimeStamp(), message.getVelocity() * 10);
    }

    public void insertSensorData(SensorEvent message) {
        if (!trackanalyzerphase) {
            if (absolut_time == -1) {
                absolut_time = message.getTimeStamp();
            }
            this.runningSerie.add(Math.abs(absolut_time - message.getTimeStamp()), message.getG()[2]);
        }
    }
}
