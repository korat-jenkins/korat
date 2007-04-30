package korat.utils.cv;

import java.io.IOException;

import korat.config.ConfigManager;
import korat.finitization.impl.StateSpace;
import korat.testing.ITestCaseListener;
import korat.testing.impl.TestCradle;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class WriteCVListener implements ITestCaseListener {

    /**
     * Writes all vectors to file.
     *  
     * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
     * 
     */
    abstract class WriterMode {
        public abstract void newTestCase(Object testCase, int[] cv, boolean ok);
        public abstract void testFinished();
    }
    
    /**
     * Writes only given number of vectors to file. Tries to chose equi-distant
     * vectors without knowing the number of total explored vectors in advance.
     * Uses one-pass algorithm that doesn't guarantee the optimal results. 
     * 
     * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
     *
     */
    class WriteAllMode extends WriterMode {
        @Override
        public void newTestCase(Object testCase, int[] cv, boolean ok) {
            try {
                if (cvWriter != null) {
                    cvWriter.writeCV(cv, ok);
                }
            } catch (IOException e) {
                System.err.println("WARNING: Cannot write to candidate vector file!");
            }
        }
        @Override
        public void testFinished() {
        }
    }
    
    /**
     * Writes only given number of vectors to file knowing the expected number of
     * explored vectors in advance. Guarantees optimal results if the expected number of
     * vectors is correct.
     * 
     * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
     *
     */
    class WriteNumMode extends WriterMode {

        protected int[][] buff;
        
        protected boolean[] oks;
        
        protected int distance = 1;
        
        protected int cnt = 0;
        
        protected int index = 0;
        
        protected void initCandArray(int cvLength) {
            int size = numToWrite * 2;
            buff = new int[size][cvLength];
            oks = new boolean[size];
        }
        
        WriteNumMode(int cvLength) {
            initCandArray(cvLength);
        }
        
        @Override
        public void newTestCase(Object testCase, int[] cv, boolean ok) {
            if (cnt == distance) {
                //TODO: buff[index] = new int[cv.length];
                System.arraycopy(cv, 0, buff[index], 0, cv.length);
                oks[index] = ok;
                cnt = 0;
                index++;
                if (index == buff.length) {
                    int n = buff.length / 2;
                    for (int i = 0; i < n; i++) {
                        //TODO: buff[i] = buff[2 * i + 1];
                        System.arraycopy(buff[2*i+1], 0, buff[i], 0, cv.length);
                    }
                    distance *= 2;
                    index = n;
                }
            }
            cnt++;
        }

        @Override
        public void testFinished() {
            try {
                for (int i = 0; i < index; i++) {
                    cvWriter.writeCV(buff[i], oks[i]);
                }
            } catch (IOException e) {
                System.err.println("WARNING: Cannot write to candidate vector file!");
            }
        }
        
    }
    
    /**
     * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
     * 
     */
    class WriteNumExpectedMode extends WriteNumMode {

        private long len;
        
        private long mod;
        
        WriteNumExpectedMode(int cvLength) {
            super(cvLength);
            len = expectedNumberOfVectors / (numToWrite + 1);
            mod = expectedNumberOfVectors % (numToWrite + 1);
            cnt = 0;
            index = 0;
        }

        @Override
        protected void initCandArray(int cvLength) {
            buff = new int[numToWrite][cvLength];
            oks = new boolean[numToWrite];
        }

        @Override
        public void newTestCase(Object testCase, int[] cv, boolean ok) {
            long  x = len;
            if (mod > 0) {
                x++;
            }
            if (cnt == x) {
                if (index < buff.length) {
                    System.arraycopy(cv, 0, buff[index], 0, cv.length);
                    index++;
                } else {
                    System.out.println("WARNING: there are more vectors than expected");
                }
                cnt = 0;
                mod--;
            }
            cnt++;
        }

        @Override
        public void testFinished() {
            try {
                for (int i = 0; i < buff.length; i++) {
                    cvWriter.writeCV(buff[i], oks[i]);
                }
            } catch (IOException e) {
                System.err.println("WARNING: Cannot write to candidate vector file!");
            }
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Options that may be set before Korat execution starts
     * -------------------------------------------------------------------------
     */
    
    protected ICVWriter cvWriter;
    
    private boolean started = false;


    /*
     * -------------------------------------------------------------------------
     * Internal stuff.
     * -------------------------------------------------------------------------
     */

    protected WriterMode mode;

    protected int numToWrite;

    protected long expectedNumberOfVectors;
    
    protected void init(int cvLength) {
        numToWrite = ConfigManager.getInstance().cvWriteNum;
        expectedNumberOfVectors = ConfigManager.getInstance().cvExpected;
        initCVWriter();
        initMode(cvLength);
    }

    private void initCVWriter() {
        StateSpace stateSpace = TestCradle.getInstance().getStateSpace();
        int numElems = stateSpace.getTotalNumberOfFields();
        int max = 0;
        for (int i = 0; i < numElems; i++) {
            int m = stateSpace.getFieldDomain(i).getNumberOfElements();
            if (m > max) {
                max = m;
            }
        }
        try {
            String cvFileName = ConfigManager.getInstance().cvFile;
            cvWriter = CVFactory.getCVFactory().createCVWriter(cvFileName, numElems, max);
        } catch (Exception e) {
            cvWriter = null;
            System.err.println("WARNING: Cannot init WriteCVListener!");
        }
    }

    private void initMode(int cvLength) {
        if (numToWrite <= 0) {
            mode = new WriteAllMode();
        } else if (expectedNumberOfVectors <= 0) {
            mode = new WriteNumMode(cvLength);
        } else {
            mode = new WriteNumExpectedMode(cvLength);
        }
    }
    
    public void notifyNewTestCase(Object testCase) {
        int[] cv = TestCradle.getInstance().getCandidateVector();
        boolean ok = TestCradle.getInstance().isPredicateOK();
        if (!started) {
            init(cv.length);
            started = true;
        }
        mode.newTestCase(testCase, cv, ok);
    }

    public void notifyTestFinished(long numOfExplored, long numOfGenerated) {
        mode.testFinished();
        try {
            cvWriter.close();
        } catch (IOException e) {
            System.err.println("WARNING: Cannot close CVWriter!");
        }
    }
    
    
    static void f(int index, int n) {
        if (index > n) {
            index--;
        }
        int k = index - n;
        for (int i = 0; i < k; i++) {
            System.out.println(2*i + 1);
        }
        for (int i = 2 * k; i < index; i++) {
            System.out.println(i);
        }
    }

    static void g(int totalWorkers, int workerIndex, long cvsInFile) {
        long cvStart = workerIndex == 0 ? -1 : workerIndex * (cvsInFile + 1) / totalWorkers; 
        long cvEnd = (workerIndex + 1) * (cvsInFile + 1) / totalWorkers;
        if (cvEnd >= cvsInFile) {
            cvEnd = -1;
        }
        System.out.println(cvStart + " " + cvEnd);
    }
    
    public static void main(String[] args) {
        int totalWorkers = 20;
        for (int i = 0; i < totalWorkers; i++) {
            g(totalWorkers, i, 99);
        }
    }
}
