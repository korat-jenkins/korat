package korat.utils.cv;

import java.io.IOException;

/**
 * Interface for writing candidate vectors to a file.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public interface ICVWriter {

    /**
     * Writes given candidate vector and its predicateOK flag to file.
     * 
     * @param cv candidate vector to be written to file
     * @param predicateOK if the given candidate vector passes predicate check or not
     * @throws IOException if an I/O error occurs
     */
    void writeCV(int[] cv, boolean predicateOK) throws IOException;

    /**
     * Closes underlying files, streams, etc.
     * 
     * @throws IOException if an I/O error occurs
     */
    void close() throws IOException;

}