package korat.utils.cv;

import java.io.IOException;

import korat.config.ConfigManager;

public class WriteCVListenerSim extends WriteCVListener {

    @Override
    protected void init(int cvLength) {
        try {
            cvWriter = CVFactory.getCVFactory().createCVWriter(ConfigManager.getInstance().cvFile, 10, 9);
            mode = new WriteNumMode(cvLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean started = false;
    
    public void writeCV(int[] cv) {
        if (!started) {
            init(cv.length);
            started = true;
        }
        mode.newTestCase(null, cv, false);
    }

}
