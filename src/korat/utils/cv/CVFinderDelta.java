package korat.utils.cv;

import java.io.IOException;

import korat.utils.IIntList;
import korat.utils.Pair;
import korat.utils.io.BitRandomAccessFile;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class CVFinderDelta implements ICVFinder {

    private long numCVs;

    private int numElemsPerCV;

    private int numBitsPerElem;

    private int fullFormatRatio;
    
    private int numFullFormatVectors;

    private long bodyStart;

    private boolean predicateOK;

    private String cvFileName;

    private String cvDeltaFileName;

    private BitRandomAccessFile cvFile;

    private BitRandomAccessFile cvDeltaFile;

    private int dataSizeInBits;

    private int numBitsPerCVIndex;

    protected CVFinderDelta(String cvFileName) throws IOException {
        this.cvFileName = cvFileName;
        cvDeltaFileName = CVWriterDelta.getDeltaFileName(cvFileName);
        cvFile = new BitRandomAccessFile(cvFileName);
        cvDeltaFile = new BitRandomAccessFile(cvDeltaFileName);
        readHeader();
        bodyStart = cvFile.getFilePointer();
        dataSizeInBits = (numElemsPerCV * numBitsPerElem) + 64 + 1;
        numBitsPerCVIndex = (int) Math.ceil(Math.log(numElemsPerCV)
                / Math.log(2));
        numFullFormatVectors = (int)((numCVs + fullFormatRatio - 1)/ fullFormatRatio);
    }

    private void readHeader() throws IOException {
        numCVs = cvFile.readLong();
        fullFormatRatio = cvFile.readInt();
        numElemsPerCV = cvFile.readInt();
        numBitsPerElem = cvFile.readInt();
    }

    public long getNumCVs() {
        return numCVs;
    }

    public int getNumElemsPerCV() {
        return numElemsPerCV;
    }

    public boolean isPredicateOK() {
        return predicateOK;
    }

    public int[] readCV(long idx) throws IOException {
        int[] lastCV = new int[numElemsPerCV];

        long offsetInBits = readOffset(idx, lastCV);

        long noInDelta = idx % fullFormatRatio;
        if (noInDelta == 0)
            return lastCV;

        cvDeltaFile.seek(offsetInBits);
        int[] cv = new int[lastCV.length];
        for (int i = 0; i < noInDelta; i++) {
            predicateOK = CVReaderDelta.readDelta(cv, lastCV, cvDeltaFile,
                    numBitsPerCVIndex, numBitsPerElem);
            System.arraycopy(cv, 0, lastCV, 0, cv.length);
        }

        return cv;
    }

    private long readOffset(long idx, int[] cv) throws IOException {
        // calc position in file of full-format vectors
        long fullFormatVectIdx = idx / fullFormatRatio;
        long pos = bodyStart;
        long bitsOff = dataSizeInBits * fullFormatVectIdx;
        pos += bitsOff;
        cvFile.seek(pos);

        for (int i = 0; i < cv.length; i++)
            cv[i] = cvFile.readBitsAsInt(numBitsPerElem);

        long deltaOffset = cvFile.readLong();

        int b = cvFile.readBitsAsInt(1);
        if (b == 0) {
            predicateOK = false;
        } else {
            predicateOK = true;
        }

        return deltaOffset;
    }

    /**
     * sequential search
     */
    public long find(int[] cv) throws IOException {
        int[][] cvs = new int[1][];
        cvs[0] = cv;
        return find(cvs)[0];
    }

    /**
     * sequential search
     */
    public long[] find(int[][] cvs) throws IOException {
        long[] result = new long[cvs.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = -1;
        }
        CVReaderDelta reader = new CVReaderDelta(cvFileName);
        int cnt = 0;
        for (int i = 0; i < numCVs; i++) {
            int[] cv2 = reader.readCV();
            for (int j = 0; j < cvs.length; j++) {
                int[] cv = cvs[j];
                if (CVCmp.equal(cv, cv2)) {
                    result[j] = i;
                    cnt++;
                    if (cnt == cvs.length)
                        break;
                }
            }
        }
        return result;
    }

    public static class FinderResult {
        public boolean found;
        public long exactIdx;
        public long fromIdx;
        public long toIdx;
        public FinderResult() {
        }
        public FinderResult(boolean found, long exactIdx, long fromIdx, long toIdx) {
            super();
            this.found = found;
            this.exactIdx = exactIdx;
            this.fromIdx = fromIdx;
            this.toIdx = toIdx;
        }
    }
    
    private int[][] buffer = null;

    private long[] offsets = null;

    private void initBuffer() throws IOException {
        buffer = new int[numFullFormatVectors][];
        offsets = new long[numFullFormatVectors];
        cvFile.seek(bodyStart);
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = new int[numElemsPerCV];
            Pair<Long, Boolean> pair = CVReaderDelta.readFullFormatVector(
                    buffer[i], cvFile, numBitsPerElem);
            offsets[i] = pair.getData1();
        }
    }

    public FinderResult find(int[] cv, IIntList fieldAccessList) throws IOException {
        if (buffer == null) {
            try {
                initBuffer();
            } catch (IOException e) {
                throw new RuntimeException(
                        "CVFinderDelta: Cannot init buffer for binary searc", e);
            }
        }
        //run bin search through full-format vectors
        binSearch(cv, fieldAccessList);
        if (binSearchFound) {
            return new FinderResult(true, binSearchIdx * fullFormatRatio, -1, -1);
        }
        
        // if binSearchIdx is -1 than given vector is lesser than the first one, 
        // meaning that it can't be found in this file
        if (binSearchIdx < 0) {
            return new FinderResult(false, -1, -1, 0);
        }
        
        //not found during binSearch, continue with sequential search through deltas
        long offset = offsets[binSearchIdx];        
        cvDeltaFile.seek(offset);
        return seqSearch(cv, buffer[binSearchIdx], fieldAccessList);
    }

    private FinderResult seqSearch(int[] cvToFind, int[] lastCV, IIntList fieldAccessList) throws IOException {
        long idx = binSearchIdx * fullFormatRatio + 1;
        long n = idx + fullFormatRatio - 1;
        if (n > numCVs)
            n = numCVs;
        int[] cv;
        for (long i = idx; i < n; i++) {
            cv = new int[lastCV.length];
            CVReaderDelta.readDelta(cv, lastCV, cvDeltaFile, numBitsPerCVIndex, numBitsPerElem);
            int x = CVCmp.compare(cvToFind, cv, fieldAccessList);
            if (x == 0) {
                return new FinderResult(true, i, -1, -1);
            } else if (x < 0) {
                return new FinderResult(false, -1, i -1, i);
            }
            lastCV = cv;
        }
        long toIdx = (n == numCVs) ? -1 : n;
        return new FinderResult(false, -1, n-1, toIdx);
    }

    int binSearchIdx;
    boolean binSearchFound;

    private void binSearch(int[] cvToFind, IIntList fieldAccessList) {
        int first = 0; 
        int last = buffer.length - 1;
        binSearchFound = false;
        binSearchIdx = -1;
        
        while (first <= last) {
            int middle = (first + last) / 2;
            int[] cvMiddle = buffer[middle];
            int x = CVCmp.compare(cvToFind, cvMiddle, fieldAccessList);
            if (x == 0) { //equal
                binSearchFound = true;
                binSearchIdx = middle;
                return;
            } 
            if (x < 0) { //smaller
                last = middle - 1;
            } else { //larger
                binSearchIdx = middle;
                first = middle + 1;
            }
        }
    }

    public void close() throws IOException {
        if (cvFile != null)
            cvFile.close();
        if (cvDeltaFile != null)
            cvDeltaFile.close();
    }

}
