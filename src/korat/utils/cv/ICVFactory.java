package korat.utils.cv;

import java.io.IOException;

public interface ICVFactory {
    
    ICVWriter createCVWriter(String fileName, int numElemsPerCV, int maxElem) throws IOException;
    
    ICVReader createCVReader(String fileName) throws IOException;
    
    ICVFinder createCVFinder(String fileName) throws IOException;
    
}
