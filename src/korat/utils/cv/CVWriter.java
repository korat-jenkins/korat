package korat.utils.cv;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import korat.utils.io.BitOutputStream;


/**
 * Utility for writing candidate vectors in a binary file.
 * <p/>
 * 
 * Format of the file that this utility produces is:
 *   0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                   TOTAL NUMBER OF CANDIDADE VECTORS IN A FILE (high word)                     |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                   TOTAL NUMBER OF CANDIDADE VECTORS IN A FILE (low word)                      |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                          NUMBER OF ELEMENTS PER CANDIDATE VECTOR                              |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                        NUMBER OF BITS PER CANDIDATE VECTOR ELEMENT                            |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * | CVs[0,0] (variable size)    | CVs[0,1] (variable size) |.... | CVs[0,m] (variable size)    |ok| 
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * | CVs[1,0] (variable size)    | CVs[1,1] (variable size) |.... | CVs[1,m] (variable size)    |ok| 
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                                               ....                                            |  
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * | CVs[n,0] (variable size)    | CVs[n,1] (variable size) |.... | CVs[n,m] (variable size)    |ok| 
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * 
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CVWriter implements ICVWriter {

    private int numElemsPerCV;
    private int numBitsPerElem; 
    private String fileName;
    private long numCVs = 0;
    private BitOutputStream bos = null;
    
    protected CVWriter(String fileName, int numElemsPerCV, int maxElem) throws IOException {
        this.fileName = fileName;
        this.numElemsPerCV = numElemsPerCV;
        this.numBitsPerElem = (int) Math.ceil(Math.log(maxElem + 1) / Math.log(2));
        bos = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
        writeHeader();
    }

    private void writeHeader() throws IOException {
        bos.writeLong(-1); //placeholder for number of candidate vectors
        bos.writeInt(numElemsPerCV);
        bos.writeInt(numBitsPerElem);
        
    }
    
    public void writeCV(int[] cv, boolean predicateOK) throws IOException {
        if (cv.length != numElemsPerCV)
            throw new RuntimeException("Wrong number of elements in candidate vector.");
        for (int elem : cv) {
            bos.writeBits(elem, numBitsPerElem);
        }
        if (predicateOK) {
            bos.writeBits(1, 1);
        } else {
            bos.writeBits(0, 1);
        }
        numCVs++;
    }
    
    public void close() throws IOException {
        if (bos != null)
            bos.close();
        
        // write num of CVs at the beginning of the file.
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(fileName, "rw");
            f.writeLong(numCVs);
        } finally {
            if (f != null) 
                f.close();
        }
    }
    
}
