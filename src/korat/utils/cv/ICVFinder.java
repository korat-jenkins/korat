package korat.utils.cv;

import java.io.IOException;

/**
 * Interface for searching for candidate vectors in the given 
 * candidate-vectors-file.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public interface ICVFinder {

    /**
     * Gets number of candidate vectors in a file
     * 
     * @return number of candidate vectors
     */
    long getNumCVs();

    /**
     * Gets number of elements per candidate vector
     * 
     * @return number of elements per candidate vector
     */
    int getNumElemsPerCV();

    /**
     * Reads idx-th candidate vector from file
     * 
     * @param idx index of candidate vector in the file
     * @return candidate vector on the idx-th position in the file
     * @throws IOException if an I/O error occurs
     */
    int[] readCV(long idx) throws IOException;

    /**
     * Call this method immediately after calling readCV to find out if the read
     * vector passes predicate check or not
     * 
     * @return is predicate ok or not for the last read candidate vector
     */
    boolean isPredicateOK();

    /**
     * Finds the index for the given candidate vector.
     * 
     * @param cv candidate vector to search for
     * @return index of the given candidate vector in the file if vector exists
     *         of -1 otherwise
     * @throws IOException if an I/O error occurs
     */
    long find(int[] cv) throws IOException;

    /**
     * Finds indexes for the given candidate vectors
     * @param cvs candidate vectors to search for
     * @return indices of the given candidate vectors in the file
     * @throws IOException if an I/O error occurs
     */
    public long[] find(int[][] cvs) throws IOException;
    
    /**
     * Closes underlying files, streams, etc.
     * 
     * @throws IOException
     */
    void close() throws IOException;

    

}