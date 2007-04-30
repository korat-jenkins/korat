package korat.utils.cv;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import korat.config.ConfigManager;
import korat.utils.IIntList;
import korat.utils.IntList;
import korat.utils.cv.CVFinderDelta.FinderResult;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CVFinderDeltaTest extends TestCase {

    private static int[][] vectors = {
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 }, //0
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 }, //1
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0 }, //2
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 0 }, //3
        { 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1, 0, 1, 1, 0, 0 }, //4
        { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //5
        { 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //6
        { 0, 0, 3, 0, 0, 0, 1, 0, 1, 0, 1, 0, 3, 0, 0, 0 }, //7
        { 2, 3, 1, 3, 3, 2, 4, 3, 2, 1, 3, 2, 1, 4, 2, 1 }, //8
        { 2, 3, 1, 3, 3, 2, 4, 3, 3, 0, 0, 0, 0, 0, 0, 0 }  //9
    };
    
    private static int[][] inBetweenVectors = {
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, //0
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0 }, //1
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0 }, //2
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0 }, //3
        { 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 1, 1, 0, 0 }, //4
        { 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0 }, //5
        { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, //6
        { 0, 0, 2, 0, 0, 0, 1, 0, 1, 0, 1, 0, 3, 0, 0, 0 }, //7
        { 0, 2, 1, 3, 3, 2, 4, 3, 2, 1, 3, 2, 1, 4, 2, 1 }, //8
        { 2, 3, 1, 3, 3, 2, 4, 3, 2, 2, 3, 2, 1, 4, 2, 1 }, //9 
        { 2, 3, 1, 4, 3, 2, 4, 3, 3, 0, 0, 0, 0, 0, 0, 0 }  //10
    };
    
    private String fileName = "cand_finder_test.dat";
    
    private IIntList fal;
    protected void setUp() throws Exception {
        super.setUp();
        fal = new IntList();
        for (int i = 0; i < vectors[0].length; i++) {
            fal.add(i);
        }
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
        ConfigManager.getInstance().cvFullFormatRatio = 4;
        doCVDeltaTest();
    }
    
    public void testCVDelta5() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 5;
        doCVDeltaTest();
    }
    
    public void testCVDelta6() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 6;
        doCVDeltaTest();
    }
    
    public void testCVDelta7() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 7;
        doCVDeltaTest();
    }
    
    public void testCVDelta8() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 8;
        doCVDeltaTest();
    }
    
    public void testCVDelta9() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 9;
        doCVDeltaTest();
    }
    
    public void testCVDelta10() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 10;
        doCVDeltaTest();
    }
    
    public void testCVDelta15() throws Exception {
        ConfigManager.getInstance().cvFullFormatRatio = 15;
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
        CVWriterDelta cvWriter = new CVWriterDelta (fileName, vectors[0].length, max);
        for (int i = 0; i < vectors.length; i ++) {
            boolean predicate = i % 4 == 0;
            cvWriter.writeCV(vectors[i], predicate);
        }
        cvWriter.close();
        //read those vectors from file and check for correctness
        CVFinderDelta cvFinder = new CVFinderDelta(fileName);
        assertEquals(vectors.length, cvFinder.getNumCVs());
        for (int i = 0; i < vectors.length; i++) {
            FinderResult fr = cvFinder.find(vectors[i], new IntList());
            assertTrue(fr.found);
            assertEquals(i, fr.exactIdx);
            fr = cvFinder.find(vectors[i], fal);
            assertTrue(fr.found);
            assertEquals(i, fr.exactIdx);
        }
        
        int n = inBetweenVectors.length;
        for (int i = 0; i < n; i++) {
            int[] cvToFind = inBetweenVectors[i];
            FinderResult fr = cvFinder.find(cvToFind, new IntList());
            assertFalse(fr.found);
            assertEquals(-1, fr.exactIdx);
            assertEquals(i-1, fr.fromIdx);
            if (i < n - 1)
                assertEquals(i, fr.toIdx);
            else 
                assertEquals(-1, fr.toIdx);
        }
        
        
        cvFinder.close();
        //delete temp files
        new File(fileName).delete();
        new File(CVWriterDelta.getDeltaFileName(fileName)).delete();
    }

    @SuppressWarnings("unchecked")
    public void testAccList() throws Exception {
        String cvFileName = getClass().getResource("test-acclist.dat").getFile();
        //String cvFileName = "d:\\data\\cv7delta10000\\c7_delta_10000.dat";
        String acclistFileName = getClass().getResource("acclist.dat").getFile();
        
        //read those vectors from file and check for correctness
        CVFinderDelta cvFinder = new CVFinderDelta(cvFileName);
        DataInputStream din = new DataInputStream(
                new BufferedInputStream(new FileInputStream(acclistFileName)));
        
        List cvs = new LinkedList();
        List<IIntList> accLists = new ArrayList<IIntList>(); 
        List<Long> idxs = new ArrayList<Long>();
        
        while (true) {
            long idx; 
            try {
                idx = din.readLong();
            } catch (EOFException e) {
                break;
            }
            if (idx == -1) break;
            int cvLen = din.readInt();
            int[] cv = new int[cvLen];
            for (int i = 0; i < cvLen; i++) {
                cv[i] = din.readInt();
            }
            
            int accListLen = din.readInt();
            IntList accList = new IntList();
            for (int i = 0; i < accListLen; i++) {
                accList.add(din.readInt());
            }
            
            cvs.add(cv);
            accLists.add(accList);
            idxs.add(idx);
        }
        
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < cvs.size(); i++) {
            int[] cv = (int[])cvs.get(i);
            IIntList accList = accLists.get(i);
            long idx = idxs.get(i);
            FinderResult fr = cvFinder.find(cv, accList);
            assertTrue(fr.found);
            assertEquals(idx, fr.exactIdx);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("BinSearch (" + cvs.size() + "): " + (t2 - t1) / 1000.0);
        
        cvFinder.close();
    }
    

}
