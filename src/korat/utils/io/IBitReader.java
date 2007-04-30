package korat.utils.io;

import java.io.IOException;

/**
 * Interface for reading bits (e.g. from InputStream, RandomFileAccess etc.)
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public interface IBitReader {

    /**
     * Reads the given number of bits and interprets them as long
     * 
     * @param numOfBits number of bits to read. Has to be in the range from 1 to 64.
     * @return value of the read bits interpreted as long. 
     * @throws IOException if an I/O error occurs.
     */
    long readBitsAsLong(int numOfBits) throws IOException;

    /**
     * Reads the given number of bits and interprets them as int.
     * 
     * @param numOfBits number of bits to read. Has to be in the range from 1 to 31.
     * @return value of the read bits interpreted as int.
     * @throws IOException if an I/O error occurs.
     */
    int readBitsAsInt(int numOfBits) throws IOException;

    /**
     * Reads 32 bits and interprets them as int.
     * 
     * @return value of 32 read bits interpreted as int.
     * @throws IOException if an I/O error occurs.
     */
    int readInt() throws IOException;

    /**
     * Reads 64 bits and interprets them as long.
     * 
     * @return value of 64 read bits interpreted as long.
     * @throws IOException if an I/O error occurs.
     */
    long readLong() throws IOException;

    /**
     * Reads 32 bits and interprets them as float.
     * 
     * @return value of 32 read bits interpreted as float.
     * @throws IOException if an I/O error occurs.
     */
    float readFloat() throws IOException;

    /**
     * Reads 64 bits and interprets them as double.
     * 
     * @return value of 64 read bits interpreted as double.
     * @throws IOException if an I/O error occurs.
     */
    double readDouble() throws IOException;

}