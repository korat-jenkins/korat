package korat.utils.cv;

import java.io.IOException;

import korat.utils.io.BitRandomAccessFile;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CVFinder implements ICVFinder {
    
    long numCVs;
    
    private int numElemsPerCV;
    
    private int numBitsPerElem;
    
    private long bodyStart;
    
    private int cvSizeInBits;

    private boolean predicateOK;

    String cvFileName;
    
    private BitRandomAccessFile cvFile;
    
    protected CVFinder(String cvFileName) throws IOException {
        this.cvFileName = cvFileName;
        cvFile = new BitRandomAccessFile(cvFileName);
        readHeader();
        bodyStart = cvFile.getFilePointer();
        cvSizeInBits = numElemsPerCV * numBitsPerElem;
    }

    private void readHeader() throws IOException {
        numCVs = cvFile.readLong();
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
        int[] cv = new int[numElemsPerCV];
        
        // calc position in file
        long pos = bodyStart;
        int dataSizeInBits = cvSizeInBits + 1;
        long bitsOff = dataSizeInBits * idx;
        pos += bitsOff;
        cvFile.seek(pos);
        
        // now it is positioned just before the candidate vector
        for (int i = 0; i < cv.length; i++)
            cv[i] = cvFile.readBitsAsInt(numBitsPerElem);
        
        int b = cvFile.readBitsAsInt(1);
        if (b == 0) {
            predicateOK = false;
        }else { 
            predicateOK = true;
        }
        
        return cv;
    }
    
    public long find(int[] cv) throws IOException {
        int[][] cvs = new int[1][];
        cvs[0] = cv;
        return find(cvs)[0];
    }
    
    public long[] find(int[][] cvs) throws IOException {
        long[] result = new long[cvs.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = -1;
        }
        CVReader reader = new CVReader(cvFileName);
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
    
    public void close() throws IOException {
        if (cvFile != null)
            cvFile.close();
    }

    public static void main(String[] args) {
        try {
            
            CVFinder f = new CVFinder("candidates.dat");
            System.out.println("Number of candidate vectors: " + f.numCVs);
            System.out.println("Number elements per candidate vector: " + f.numElemsPerCV);
            System.out.println("Number of bits per elem: " + f.numBitsPerElem);
            int[] cv = f.readCV(200900);
            System.out.println(f.find(cv));
            cv = f.readCV(160123);
            System.out.println(f.find(cv));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
