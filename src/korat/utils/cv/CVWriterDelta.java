package korat.utils.cv;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import korat.config.ConfigManager;
import korat.utils.io.BitOutputStream;

/**
 * Writes candidate vectors using deltas. Every <code>N</code>th candidate vector
 * is written in full format (as with CVWriter) in a file that contains only 
 * full-format vectors. Vectors in between are stored in a separate file that
 * contains only the difference (delta) comparing to the last vector. 
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class CVWriterDelta implements ICVWriter {

    /**
     * Per how many vectors one should be stored in full format
     * (ratio of all vectors and full format vectors) 
     */
    protected static final int MANY_BITS_CHANGED = 1;

    protected static final int ONE_BIT_CHANGED = 0;

    protected static final int CODE_LEN = 2;

    protected static final int FOLLOWING_ZEROS_INC_CODE = 0;

    protected static final int FOLLOWING_ZEROS_CODE = 1;

    protected static final int FOLLOWING_SAME_CODE = 2;

    protected static final int FOLLOWING_SAME_INC_CODE = 3;

    private String fileName = "candidates.dat";

    private long numCVs = 0;

    private int numBitsPerElem;

    private int numElemsPerCV;

    private int numBitsPerCVIndex;
    
    private int[] lastCV;

    private BitOutputStream bos;

    private BitOutputStream bosDelta;
    
    private int cnt = 0;
    
    private long deltaOffset = 0;

    protected CVWriterDelta(String fileName, int numElemsPerCV, int maxElem)
            throws IOException {

        this.fileName = fileName;
        this.numElemsPerCV = numElemsPerCV;
        this.numBitsPerElem = (int) Math.ceil(Math.log(maxElem + 1) / Math.log(2));
        this.numBitsPerCVIndex = (int) Math.ceil(Math.log(numElemsPerCV) / Math.log(2));
        
        lastCV = new int[numElemsPerCV];
        
        String fileNameDelta = getDeltaFileName(fileName);
        bos = new BitOutputStream(new BufferedOutputStream(
                new FileOutputStream(fileName)));
        bosDelta = new BitOutputStream(new BufferedOutputStream(
                new FileOutputStream(fileNameDelta)));
        writeHeader();
        
    }
    
    private void writeHeader() throws IOException {
        bos.writeLong(-1); // placeholder for number of candidate vectors
        bos.writeInt(ConfigManager.getInstance().cvFullFormatRatio); // ratio of all vectors and full format vectors
        bos.writeInt(numElemsPerCV);
        bos.writeInt(numBitsPerElem);
    }

    public void close() throws IOException {
        if (bos != null)
            bos.close();
        if (bosDelta != null)
            bosDelta.close();
        // write num of CVs at the begining of the file.
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(fileName, "rw");
            f.writeLong(numCVs);
        } finally {
            if (f != null)
                f.close();
        }
    }
    
    public void writeCV(int[] cv, boolean predicateOK) throws IOException {
        if (cv.length != numElemsPerCV)
            throw new RuntimeException("Wrong number of element in candidate vector.");
        if (cnt == 0) { 
            writeFullFormatCV(cv, predicateOK);
            cnt = ConfigManager.getInstance().cvFullFormatRatio;
        } else {
            writeDeltaCV(cv, predicateOK);
        }
        System.arraycopy(cv, 0, lastCV, 0, cv.length); 
        numCVs++;
        cnt--;
    }

    private void writeFullFormatCV(int[] cv, boolean predicateOK) throws IOException {
        for (int elem : cv) {
            bos.writeBits(elem, numBitsPerElem);
        }
        bos.writeLong(deltaOffset);
        writePredicateBit(predicateOK, bos);
    }

    private boolean writeDeltaCV(int[] cv, boolean predicateOK) throws IOException {
        //check two consecutive vectors
        int k;
        for (k = 0; k < cv.length; k++) {
            if (lastCV[k] != cv[k])
                break;
        }
        if (k == cv.length) {
            return false;
        }
        
        List<Integer> changedIndexes = new ArrayList<Integer>(cv.length - k + 1);
        List<Integer> changedValues = new ArrayList<Integer>(cv.length - k + 1);
        changedIndexes.add(k);
        changedValues.add(cv[k]);
        
        boolean followingZeros = true;
        boolean followingSame = true;
        
        for (int j = k + 1; j < cv.length; j++) {
            if (cv[j] != 0) {
                followingZeros = false;
            }
            if (cv[j] != lastCV[j]) {
                followingSame = false;
                changedIndexes.add(j);
                changedValues.add(cv[j]);
            }
        }
        // based on the previous check, encode delta
        if (followingSame || followingZeros) {
            bosDelta.writeBits(ONE_BIT_CHANGED, 1);
            deltaOffset++;
        } else {
            bosDelta.writeBits(MANY_BITS_CHANGED, 1);
            deltaOffset++;
        }
        if (followingSame) {
            if (cv[k] == lastCV[k] + 1) {
                bosDelta.writeBits(FOLLOWING_SAME_INC_CODE, CODE_LEN);
                bosDelta.writeBits(k, numBitsPerCVIndex);
                deltaOffset += CODE_LEN + numBitsPerCVIndex;
            } else {
                bosDelta.writeBits(FOLLOWING_SAME_CODE, CODE_LEN);
                bosDelta.writeBits(k, numBitsPerCVIndex);
                bosDelta.writeBits(cv[k], numBitsPerElem);
                deltaOffset += CODE_LEN + numBitsPerCVIndex + numBitsPerElem;
            }
        } else if (followingZeros) {
            if (cv[k] == lastCV[k] + 1) {
                bosDelta.writeBits(FOLLOWING_ZEROS_INC_CODE, CODE_LEN);
                bosDelta.writeBits(k, numBitsPerCVIndex);
                deltaOffset += CODE_LEN + numBitsPerCVIndex;
            } else {
                bosDelta.writeBits(FOLLOWING_ZEROS_CODE, CODE_LEN);
                bosDelta.writeBits(k, numBitsPerCVIndex);
                bosDelta.writeBits(cv[k], numBitsPerElem);
                deltaOffset += CODE_LEN + numBitsPerCVIndex + numBitsPerElem;
            }
        } else { //worst case, write all changed fields
            int n = changedIndexes.size() - 1;
            bosDelta.writeBits(n, numBitsPerCVIndex);
            deltaOffset += numBitsPerCVIndex;
            for (int i = 0; i <= n; i++) {
                bosDelta.writeBits(changedIndexes.get(i), numBitsPerCVIndex);
                bosDelta.writeBits(changedValues.get(i), numBitsPerElem);
                deltaOffset += numBitsPerCVIndex + numBitsPerElem;
            }
        }
        writePredicateBit(predicateOK, bosDelta);
        deltaOffset++;
        return true;
    }
    
    private void writePredicateBit(boolean predicateOK, BitOutputStream os) throws IOException {
        if (predicateOK) {
            os.writeBits(1, 1);
        } else {
            os.writeBits(0, 1);
        }
    }

    protected static String getDeltaFileName(String fileName) {
        int i = fileName.lastIndexOf('.');
        String fileNameDelta = null;
        if (i > -1) {
            fileNameDelta = fileName.substring(0, i) + "_delta"
                    + fileName.substring(i);
        } else {
            fileNameDelta = fileName + "_delta";
        }
        return fileNameDelta;
    }

    public static void main(String[] args) {
        
        CVReader cvReader = null;
        CVWriterDelta cvWriter = null;
        try {
            cvReader = new CVReader("candidates.dat.old");
            long n = cvReader.getNumCVs();
            int elemsPerCV = cvReader.getNumElemsPerCV();
            cvWriter = new CVWriterDelta("candidates.dat.new", elemsPerCV, 8);
            int[] curr = null;
            for (long i = 0; i < n; i++) {
                curr = cvReader.readCV();
                if (curr == null) {
                    break;
                }
                cvWriter.writeCV(curr, cvReader.isPredicateOK());
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (cvReader != null) cvReader.close();
                if (cvWriter != null) cvWriter.close();
            } catch (IOException e) {
            }
        }
        
    }
    
}
