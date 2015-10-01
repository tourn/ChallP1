package visualization;

        import java.awt.BorderLayout;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;

        import javax.swing.JButton;
        import javax.swing.JPanel;

        import com.zuehlke.carrera.relayapi.messages.SensorEvent;
        import org.jfree.chart.ChartFactory;
        import org.jfree.chart.ChartPanel;
        import org.jfree.chart.JFreeChart;
        import org.jfree.chart.axis.ValueAxis;
        import org.jfree.chart.plot.XYPlot;
        import org.jfree.data.time.Millisecond;
        import org.jfree.data.time.TimeSeries;
        import org.jfree.data.time.TimeSeriesCollection;
        import org.jfree.data.xy.XYDataset;
        import org.jfree.data.xy.XYSeries;
        import org.jfree.data.xy.XYSeriesCollection;
        import org.jfree.ui.ApplicationFrame;
        import org.jfree.ui.RefineryUtilities;

public class DataChart extends ApplicationFrame implements ActionListener{

    /** The time series data. */
    private XYSeries series;

    /** The most recent value added. */
    private double lastValue = 100.0;

    /**
     * Constructs a new demonstration application.
     *
     * @param title  the frame title.
     */
    public DataChart(final String title) {

        super(title);
        this.series = new XYSeries("Sensor Z");
        final XYSeriesCollection dataset = new XYSeriesCollection(this.series);
        final JFreeChart chart = createChart(dataset);

        final ChartPanel chartPanel = new ChartPanel(chart);

        final JPanel content = new JPanel(new BorderLayout());
        content.add(chartPanel);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 900));
        setContentPane(content);
    }

    /**
     * Creates a sample chart.
     *
     * @param dataset  the dataset.
     *
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
        final XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(-5000, 5000);
        return result;
    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    *
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************

    /**
     * Handles a click on the button by adding new (random) data.
     *
     * @param e  the action event.
     */
    public void actionPerformed(final ActionEvent e) {
    }

    public void insertSensorData(SensorEvent message){
        this.series.add(message.getTimeStamp(), message.getG()[2]);
    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
}
