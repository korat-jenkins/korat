package korat.utils.cv;

import junit.framework.TestCase;

public class CVDeltaReadersTest extends TestCase {

    private static final String TEST_FILE_NAME = "test_delta_candidates.dat";
    protected ICVReader cvReader;
    protected ICVFinder cvFinder;
    
    @Override
    protected void setUp() throws Exception {
        String fileName = getClass().getResource(TEST_FILE_NAME).getFile();
        cvReader = new CVReaderDelta(fileName);
        cvFinder = new CVFinderDelta(fileName);
    }
    
    public void test1() throws Exception {
        assertEquals(cvReader.getNumCVs(), cvFinder.getNumCVs());
        int n = cvReader.getNumElemsPerCV();
        assertEquals(n, cvFinder.getNumElemsPerCV());
        for (int i = 0; i < cvReader.getNumCVs(); i++) {
            int[] cv1 = cvReader.readCV();
            int[] cv2 = cvFinder.readCV(i);
            assertEquals(n, cv1.length);
            assertEquals(n, cv2.length);
            for (int k = 0; k < n; k++) {
                assertEquals(cv1[k], cv2[k]);
            }
            assertEquals(cvReader.isPredicateOK(), cvFinder.isPredicateOK());
            assertEquals(i, cvFinder.find(cv1));
        }
        cvReader.close();
    }

}
