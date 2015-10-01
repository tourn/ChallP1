package visualization;

import org.jfree.ui.RefineryUtilities;

/**
 * Created by mario on 01.10.15.
 */
public class ThreadWrapper implements Runnable{
    @Override
    public void run() {

        DataChart demo = new DataChart("Dynamic Data Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
    }
}
