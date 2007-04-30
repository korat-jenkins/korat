package korat.utils.io;

import java.io.IOException;

/**
 * Interface for writing bits (e.g. to OutputStream, RandomFileAccess, etc.)
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public interface IBitWriter {

    /**
     * Writes <code>len</code> bits of the given value
     * <code>b<code> starting from bit offset 0.
     * 
     * @param b bits to write
     * @param len number of bits from value <code>b</code> to write
     * @throws IOException if an I/O error occurs.
     */
    void writeBits(long b, int len) throws IOException;

    /**
     * Writes <code>len</code> bits of the given value
     * <code>b<code> starting from bit offset <code>off</code>.
     * 
     * @param b bits to write
     * @param off number of bits from the given value <code>b</code> to skip 
     * @param len number of bits from the given value <code>b</code> to write 
     *            starting from <code>off</code>-th bit 
     * @throws IOException if an I/O error occurs.
     */
    void writeBits(long b, int off, int len) throws IOException;

    /**
     * Write given int in 32 bits
     * 
     * @param x int value to write
     * @throws IOException if an I/O error occurs.
     */
    void writeInt(int x) throws IOException;

    /**
     * Write given long in 64 bits
     * 
     * @param x long value to write
     * @throws IOException if an I/O error occurs.
     */
    void writeLong(long x) throws IOException;

    /**
     * Write given float in 32 bits
     * 
     * @param x float value to write
     * @throws IOException
     *             if an I/O error occurs.
     */
    void writeFloat(float x) throws IOException;

    /**
     * Write given double in 64 bits
     * 
     * @param x double value to write
     * @throws IOException
     *             if an I/O error occurs.
     */
    void writeDouble(double x) throws IOException;

    /**
     * Flush internal buffers to underlying layer 
     * 
     * @throws IOException
     *             if an I/O error occurs.
     */
    void flush() throws IOException;

}