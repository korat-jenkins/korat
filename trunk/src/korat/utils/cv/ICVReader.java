package korat.utils.cv;

import java.io.IOException;

/**
 * Interface for sequential reading of candidate vectors from the 
 * candidate-vectors-file.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public interface ICVReader {

    /**
     * Reads next candidate vector from file.
     * 
     * @return next candidate vector from file
     * @throws IOException if an I/O error occurs
     */
    int[] readCV() throws IOException;

    /**
     * Call this method immediately after calling readCV to find out if the read
     * vector passes predicate check or not
     * 
     * @return is predicate ok or not for the last read candidate vector
     */
    boolean isPredicateOK();
    
    /**
     * Closes underlying files, streams, etc.
     * 
     * @throws IOException if an I/O error occurs
     */
    void close() throws IOException;

    /**
     * Returns are there more vectors to be read.
     * 
     * @return are there more vector to be read
     */
    boolean hasNext();

    /**
     * Returns number of candidate vectors in the file
     * 
     * @return number of candidate vectors in the file
     */
    long getNumCVs();

    /**
     * Returns number of elements per candidate vector
     * 
     * @return number of elements per candidate vector
     */
    int getNumElemsPerCV();

    /**
     * Returns how many vectors have been read already.
     * 
     * @return how many vectors have been read already
     */
    long getNumCVsRead();

}