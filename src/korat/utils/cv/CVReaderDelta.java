package korat.utils.cv;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import korat.utils.Pair;
import korat.utils.io.BitInputStream;
import korat.utils.io.IBitReader;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class CVReaderDelta implements ICVReader {

    private BitInputStream bis;

    private BitInputStream bisDelta;

    private long numCVs;

    private int numElemsPerCV;

    private int numBitsPerElem;

    private int numBitsPerCVIndex;

    private int fullFormatRatio;

    private long numCVsRead = 0;

    private boolean predicateOK;

    private int[] lastCV;

    private int cnt = 0;

    protected CVReaderDelta(String fileName) throws IOException {
        this(new BufferedInputStream(new FileInputStream(fileName)),
                new BufferedInputStream(new FileInputStream(
                        CVWriterDelta.getDeltaFileName(fileName))));
    }

    protected CVReaderDelta(InputStream isFull, InputStream isDelta)
            throws IOException {
        bis = new BitInputStream(isFull);
        bisDelta = new BitInputStream(isDelta);
        readHeader();
        lastCV = new int[numElemsPerCV];
        numBitsPerCVIndex = (int) Math.ceil(Math.log(numElemsPerCV)
                / Math.log(2));
    }

    private void readHeader() throws IOException {
        numCVs = bis.readLong();
        fullFormatRatio = bis.readInt();
        numElemsPerCV = bis.readInt();
        numBitsPerElem = bis.readInt();
        numCVsRead = 0;
    }

    public void close() throws IOException {
        if (bis != null)
            bis.close();
        if (bisDelta != null)
            bisDelta.close();
    }

    public int[] readCV() throws IOException {
        int[] cv = new int[numElemsPerCV];

        if (cnt == 0) {
            Pair<Long, Boolean> pair = readFullFormatVector(cv, bis, numBitsPerElem);
            predicateOK = pair.getData2();
            cnt = fullFormatRatio;
        } else {
            predicateOK = readDelta(cv, lastCV, bisDelta, numBitsPerCVIndex,
                    numBitsPerElem);
        }

        System.arraycopy(cv, 0, lastCV, 0, cv.length);
        numCVsRead++;
        cnt--;
        return cv;
    }

    protected static Pair<Long, Boolean> readFullFormatVector(int[] cv,
            IBitReader br, int numBitsPerElem) throws IOException {

        Pair<Long, Boolean> ret = new Pair<Long, Boolean>();
        
        for (int i = 0; i < cv.length; i++) {
            cv[i] = br.readBitsAsInt(numBitsPerElem);
        }
        ret.setData1(br.readLong()); // delta offset
        int predicateOKBit = br.readBitsAsInt(1);
        if (predicateOKBit == 0) {
            ret.setData2(false);
        } else {
            ret.setData2(true);
        }
        
        return ret;
        
    }

    protected static boolean readDelta(int[] cv, int[] lastCV,
            IBitReader bisDelta, int numBitsPerCVIndex, int numBitsPerElem)
            throws IOException {

        // TODO: add checks to see if it hss reached the end of the file

        System.arraycopy(lastCV, 0, cv, 0, cv.length);
        int oneOrManyBit = bisDelta.readBitsAsInt(1);
        if (oneOrManyBit == CVWriterDelta.ONE_BIT_CHANGED) {
            int code = bisDelta.readBitsAsInt(CVWriterDelta.CODE_LEN);
            int k;
            int elem;
            switch (code) {
            case CVWriterDelta.FOLLOWING_SAME_INC_CODE:
                k = bisDelta.readBitsAsInt(numBitsPerCVIndex);
                cv[k]++;
                break;
            case CVWriterDelta.FOLLOWING_SAME_CODE:
                k = bisDelta.readBitsAsInt(numBitsPerCVIndex);
                elem = bisDelta.readBitsAsInt(numBitsPerElem);
                cv[k] = elem;
                break;
            case CVWriterDelta.FOLLOWING_ZEROS_INC_CODE:
                k = bisDelta.readBitsAsInt(numBitsPerCVIndex);
                cv[k]++;
                for (int i = k + 1; i < cv.length; i++) {
                    cv[i] = 0;
                }
                break;
            case CVWriterDelta.FOLLOWING_ZEROS_CODE:
                k = bisDelta.readBitsAsInt(numBitsPerCVIndex);
                elem = bisDelta.readBitsAsInt(numBitsPerElem);
                cv[k] = elem;
                for (int i = k + 1; i < cv.length; i++) {
                    cv[i] = 0;
                }
                break;
            }
        } else { // oneOrManyBit == CVWriterDelta.MANY_BITS_CHANGED
            int n = bisDelta.readBitsAsInt(numBitsPerCVIndex);
            int k, elem;
            for (int i = 0; i <= n; i++) {
                k = bisDelta.readBitsAsInt(numBitsPerCVIndex);
                elem = bisDelta.readBitsAsInt(numBitsPerElem);
                cv[k] = elem;
            }
        }
        int predicateOKBit = bisDelta.readBitsAsInt(1);
        if (predicateOKBit == -1) {
            return false;
        } else if (predicateOKBit == 0) {
            return false;
        } else {
            return true;
        }
    }

    protected void skipDeltaBits(long bits) throws IOException {
        bisDelta.skip(bits);
    }

    public long getNumCVs() {
        return numCVs;
    }

    public long getNumCVsRead() {
        return numCVsRead;
    }

    public int getNumElemsPerCV() {
        return numElemsPerCV;
    }

    public boolean hasNext() {
        return numCVsRead < numCVs;
    }

    public boolean isPredicateOK() {
        return predicateOK;
    }

}
