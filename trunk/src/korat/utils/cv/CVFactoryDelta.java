package korat.utils.cv;

import java.io.IOException;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CVFactoryDelta implements ICVFactory {

    private static CVFactoryDelta instance = new CVFactoryDelta();

    public static CVFactoryDelta getInstance() {
        return instance;
    }
    
    public ICVFinder createCVFinder(String fileName) throws IOException {
        return new CVFinderDelta(fileName);
    }

    public ICVReader createCVReader(String fileName) throws IOException {
        return new CVReaderDelta(fileName);
    }

    public ICVWriter createCVWriter(String fileName, int numElemsPerCV,
            int maxElem) throws IOException {
        return new CVWriterDelta(fileName, numElemsPerCV, maxElem);
    }

}
