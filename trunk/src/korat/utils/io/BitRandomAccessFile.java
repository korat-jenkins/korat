package korat.utils.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Utility for reading bits from the random access file. 
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class BitRandomAccessFile implements IBitReader {

    //TODO: implement writing also
    
    private static final long[] MASKS = { 0, 1, 3, 7, 15, 31, 63, 127, 255 };
    
    private RandomAccessFile file;

    private int inBuff;

    private int inBuffSize = 0; // @invariant: buffSize < 8
    
    public BitRandomAccessFile(String fileName) throws FileNotFoundException {
        file = new RandomAccessFile(fileName, "r");
    }
    
    /**
     * Reads the given number of bits and interprets them as long
     * 
     * @param numOfBits number of bits to read. Has to be in the range from 1 to 64.
     * @return value of the read bits interpreted as long. 
     * @throws IOException if an I/O error occurs.
     */
    public long readBitsAsLong(int numOfBits) throws IOException {
        if (numOfBits <= 0 || numOfBits > 64)
            return -2;

        long ret = inBuff;
        int bitsAvailable = inBuffSize;

        while (bitsAvailable < numOfBits) {
            int b = file.read();
            if (b == -1)
                return -1;
            if (bitsAvailable + 8 <= 64) {
                ret <<= 8;
                ret |= b;
            } else {
                int x = numOfBits - bitsAvailable;
                ret <<= x;
                inBuffSize = 8 - x;
                ret |= b >>> (inBuffSize);
                inBuff = b & (int) MASKS[inBuffSize];
            }
            bitsAvailable += 8;
        }

        if (bitsAvailable <= 64) {
            inBuffSize = bitsAvailable - numOfBits;
            inBuff = (int) (ret & MASKS[inBuffSize]);
            ret >>>= inBuffSize;
        }
        return ret;
    }
    
    /**
     * Positions file pointer to the given location. 
     * Location is given in a number of <strong>bits</strong>.
     * 
     * @param numBits
     * @throws IOException
     */
    public void seek(long numBits) throws IOException {
        inBuffSize = 0;
        inBuff = 0;
        long offset = numBits / 8;
        file.seek(offset);
        int mod = (int)(numBits % 8);
        readBitsAsInt(mod);
    }
    
    /**
     * Reads the given number of bits and interprets them as int.
     * 
     * @param numOfBits number of bits to read. Has to be in the range from 1 to 31.
     * @return value of the read bits interpreted as int.
     * @throws IOException if an I/O error occurs.
     */
    public int readBitsAsInt(int numOfBits) throws IOException {
        if (numOfBits <= 0 || numOfBits > 32)
            return -2;

        return (int) readBitsAsLong(numOfBits);
    }

    public int readInt() throws IOException {
        return readBitsAsInt(32);
    }

    public long readLong() throws IOException {
        return readBitsAsLong(64);
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Get file pointer <strong>in bits</strong>
     * @return file pointer meassured in bits
     * @throws IOException
     */
    public long getFilePointer() throws IOException {
        long pointer = file.getFilePointer();
        long pointerInBits = pointer * 8;
        return pointerInBits - inBuffSize;
    }

    public void close() throws IOException {
        if (file != null)
            file.close();
    }
    
}
