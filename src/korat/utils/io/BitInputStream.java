package korat.utils.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility for reading bits from underlying <code>InputStream</code>.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class BitInputStream extends FilterInputStream implements IBitReader {

    private static final long[] MASKS = { 0, 1, 3, 7, 15, 31, 63, 127, 255 };

    private int buff;

    private int buffSize = 0; // @invariant: buffSize < 8

    /**
     * @param in
     *            As per the general contract of the
     *            <code>FilterInputStream</code>.
     */
    public BitInputStream(InputStream in) {
        super(in);
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitReader#readBitsAsLong(int)
     */
    public long readBitsAsLong(int numOfBits) throws IOException {
        if (numOfBits <= 0 || numOfBits > 64)
            return -2;

        long ret = buff;
        int bitsAvailable = buffSize;

        while (bitsAvailable < numOfBits) {
            int b = read();
            if (b == -1)
                return -1;
            if (bitsAvailable + 8 <= 64) {
                ret <<= 8;
                ret |= b;
            } else {
                int x = numOfBits - bitsAvailable;
                ret <<= x;
                buffSize = 8 - x;
                ret |= b >>> (buffSize);
                buff = b & (int) MASKS[buffSize];
            }
            bitsAvailable += 8;
        }

        if (bitsAvailable <= 64) {
            buffSize = bitsAvailable - numOfBits;
            buff = (int) (ret & MASKS[buffSize]);
            ret >>>= buffSize;
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitReader#readBitsAsInt(int)
     */
    public int readBitsAsInt(int numOfBits) throws IOException {
        if (numOfBits <= 0 || numOfBits > 32)
            return -2;

        return (int) readBitsAsLong(numOfBits);
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitReader#readInt()
     */
    public int readInt() throws IOException {
        return readBitsAsInt(32);
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitReader#readLong()
     */
    public long readLong() throws IOException {
        return readBitsAsLong(64);
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitReader#readFloat()
     */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitReader#readDouble()
     */
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
}
