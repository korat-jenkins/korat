package korat.utils.cv;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;
import korat.config.ConfigManager;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CVDeltaTest extends TestCase {

    private static int[][] vectors = {
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 }, 
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0 }, 
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0 }, 
        { 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 1, 1, 0, 0 }, 
        { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
        { 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 
        { 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 3, 0, 0, 0 }, 
        { 2, 3, 1, 3, 3, 2, 4, 3, 2, 1, 3, 2, 1, 4, 2, 1 }, 
        { 2, 3, 1, 3, 3, 2, 4, 3, 3, 0, 0, 0, 0, 0, 0, 0 }  
    };
    
    private ICVWriter cvWriter;
    private ICVReader cvReader;
    private String fileName = "cand_test.dat";

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void test1() throws Exception {
        doCVDeltaTest();
    }
    
    public void testCVDelta1() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 1;
        doCVDeltaTest();
    }
    
    public void testCVDelta2() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 2;
        doCVDeltaTest();
    }
    
    public void testCVDelta3() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 3;
        doCVDeltaTest();
    }
    
    public void testCVDelta4() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 10;
        doCVDeltaTest();
    }
    
    public void testCVDelta5() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 20;
        doCVDeltaTest();
    }

    private int findMax() {
        int max = 0;
        for (int i = 0; i < vectors.length; i++) {
            int[] cv = vectors[i];
            for (int j = 0; j < cv.length; j++) {
                int k = cv[j];
                if (k > max)
                    max = k;
            }
        }
        return max;
    }
    
    private void doCVDeltaTest() throws Exception {
        int max = findMax();
        //write vectors to temp file
        ICVFactory factory = new CVFactoryDelta();
        cvWriter = factory.createCVWriter(fileName, vectors[0].length, max);
        for (int i = 0; i < vectors.length; i ++) {
            boolean predicate = i % 4 == 0;
            cvWriter.writeCV(vectors[i], predicate);
        }
        cvWriter.close();
        //read those vectors from file and check for correctness
        cvReader = factory.createCVReader(fileName);
        assertEquals(vectors.length, cvReader.getNumCVs());
        for (int i = 0; i < vectors.length; i++) {
            boolean predicate = i % 4 == 0;
            int[] cv = cvReader.readCV();
            System.out.println(Arrays.toString(cv));
            assertEquals(vectors[i].length, cv.length);
            for (int k = 0; k < cv.length; k++)
                assertEquals(vectors[i][k], cv[k]);
            assertEquals(predicate, cvReader.isPredicateOK());
        }
        cvReader.close();
        //delete temp files
        new File(fileName).delete();
        new File(CVWriterDelta.getDeltaFileName(fileName)).delete();
    }

}
