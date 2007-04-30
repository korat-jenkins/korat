package korat.utils.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility for writing bits to underlying <code>OutputStream</code>
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class BitOutputStream extends FilterOutputStream implements IBitWriter {

    private static final long[] MASKS = { 0, 1, 3, 7, 15, 31, 63, 127, 255 };

    private int buff;

    private int buffSize = 0; // @invariant: buffSize < 8

    /**
     * @param out
     *            as per the general contract of the
     *            <code>FilterOutputStream</code>
     */
    public BitOutputStream(OutputStream out) {
        super(out);
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitWriter#writeBits(long, int)
     */
    public void writeBits(long b, int len) throws IOException {
        writeBits(b, 0, len);
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitWriter#writeBits(long, int, int)
     */
    public void writeBits(long b, int off, int len) throws IOException {
        if (len <= 0 || len > 64)
            return;

        b >>= off;
        while (len + buffSize >= 8) {
            int x = 8 - buffSize;
            buff <<= x;
            long bb = b >> (len - x);
            buff = buff | (int) (MASKS[x] & bb);
            write(buff);
            len -= x;
            buffSize = 0;
        }

        if (len > 0) { // also (len + numOfBits < 8) holds
            buff <<= len;
            buff = buff | (int) (MASKS[len] & b);
            buffSize += len;
        }

    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitWriter#writeInt(int)
     */
    public void writeInt(int x) throws IOException {
        writeBits(x, 32);
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitWriter#writeLong(long)
     */
    public void writeLong(long x) throws IOException {
        writeBits(x, 64);
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitWriter#writeFloat(float)
     */
    public void writeFloat(float x) throws IOException {
        writeInt(Float.floatToIntBits(x));
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitWriter#writeDouble(double)
     */
    public void writeDouble(double x) throws IOException {
        writeLong(Double.doubleToLongBits(x));
    }

    /* (non-Javadoc)
     * @see korat.utils.io.IBitWriter#flush()
     */
    @Override
    public void flush() throws IOException {
        if (buffSize > 0) {
            int x = 8 - buffSize;
            buff <<= x;
            write(buff);
            buffSize = 0;
        }
        super.flush();
    }

}
