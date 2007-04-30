package korat.utils.cv;

import korat.utils.IIntList;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class CVCmp {

    public static boolean equal(int[] cv, int[] cv2) {
        for (int i = 0; i < cv.length; i++) {
            if (cv[i] != cv2[i])
                return false;
        }
        return true;
    }

    /**
     * Lexicographic comparison of two given candidate vectors.
     * 
     * @param cv1
     *            first candidate vector
     * @param cv2
     *            second candidate vector
     * @param fieldAccessList
     *            field access list that corresponds to either one of the two
     *            given candidate vectors
     * @return 1 if cv1 > cv2, -1 if cv1 < cv2, 0 if cv1 == cv2
     */
    public static int compare(int[] cv1, int[] cv2, IIntList fieldAccessList) {
        if (cv1.length != cv2.length)
            throw new RuntimeException("Vectors differ in length!");
        if (fieldAccessList.numberOfElements() > cv1.length)
            throw new RuntimeException(
                    "Field access list is longer than vectors!");
        boolean[] comparedPositions = new boolean[cv1.length];
        for (int i = 0; i < fieldAccessList.numberOfElements(); i++) {
            int idx = fieldAccessList.get(i);
            comparedPositions[idx] = true;
            if (cv1[idx] > cv2[idx]) // larger
                return 1;
            if (cv1[idx] < cv2[idx]) // smaller
                return -1;
        }
        for (int i = 0; i < cv1.length; i++) {
            if (comparedPositions[i])
                continue;
            if (cv1[i] > cv2[i]) // larger
                return 1;
            if (cv1[i] < cv2[i]) // smaller
                return -1;
        }
        return 0;
    }

}
