package korat.utils.cv;

import junit.framework.TestCase;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CVReadersTest extends TestCase {

    private static final String TEST_FILE_NAME = "test_candidates.dat";
    protected CVReader cvReader;
    protected CVFinder cvFinder;
    
    @Override
    protected void setUp() throws Exception {
        String fileName = getClass().getResource(TEST_FILE_NAME).getFile();
        cvReader = new CVReader(fileName);
        cvFinder = new CVFinder(fileName);
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
