package korat.utils.cv;

import java.io.IOException;

import korat.config.ConfigManager;

/**
 * Concrete factory for dealing with candidate vector related stuff.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CVFactory implements ICVFactory {

    private static CVFactory factory = new CVFactory();
    
    /**
     * Use this method to get currently configured factory
     * 
     * @return currently configured factory
     */
    public static ICVFactory getCVFactory() {
        if (ConfigManager.getInstance().cvDelta) {
            return CVFactoryDelta.getInstance();
        } else {
            return CVFactory.getInstance();
        }
    }
    
    public static CVFactory getInstance() {
        return factory;
    }
    
    protected CVFactory() {
        
    }
    
    public ICVFinder createCVFinder(String fileName) throws IOException {
        return new CVFinder(fileName);
    }

    public ICVReader createCVReader(String fileName) throws IOException {
        return new CVReader(fileName);
    }

    public ICVWriter createCVWriter(String fileName, int numElemsPerCV,
            int maxElem) throws IOException {
        return new CVWriter(fileName, numElemsPerCV, maxElem);
    }

}
