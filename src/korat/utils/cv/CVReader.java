package korat.utils.cv;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import korat.utils.io.BitInputStream;


/**
 * Utility for reading candidate vector files written according to CVWriter's format. 
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CVReader implements ICVReader {
    
    private BitInputStream bis;
    private long numCVs;
    private int numElemsPerCV;
    private int numBitsPerElem;
    private long numCVsRead;
    private boolean predicateOK;
    
    protected CVReader(String fileName) throws IOException {
          this(new BufferedInputStream(new FileInputStream(fileName)));
    }
    
    protected CVReader(InputStream in) throws IOException {
        bis = new BitInputStream(in);
        readHeader();
    }

    private void readHeader() throws IOException {
        numCVs = bis.readLong();
        numElemsPerCV = bis.readInt();
        numBitsPerElem = bis.readInt();
        numCVsRead = 0;
    }
    
    public int[] readCV() throws IOException {
        int[] cv = new int[numElemsPerCV];
        for (int i = 0; i < cv.length; i++) {
            cv[i] = bis.readBitsAsInt(numBitsPerElem);
            if (cv[i] == -1)
                return null;
        }
        int predicateOKBit = bis.readBitsAsInt(1);
        if (predicateOKBit == -1) {
            return null;
        } else if (predicateOKBit == 0) {
            predicateOK = false;
        } else {
            predicateOK = true;
        }
        numCVsRead++;
        return cv;
    }
    
    public void close() throws IOException {
        if (bis != null)
            bis.close();
    }
    
    public boolean hasNext() {
        return numCVsRead < numCVs;
    }

    public long getNumCVs() {
        return numCVs;
    }

    public int getNumElemsPerCV() {
        return numElemsPerCV;
    }

    public long getNumCVsRead() {
        return numCVsRead;
    }
    
    public long getNumCVsLeftToRead() {
        return numCVs - numCVsRead;
    }

    public boolean isPredicateOK() {
        return predicateOK;
    }
    
    public static void main(String[] args) {
        
        String fileName = "candidates.dat";
        boolean quiet = false;
        boolean ignoreHeaderLength = false;
        long cvStart = -1;
        long cvEnd = Long.MAX_VALUE;
        
        boolean useDelta = false;
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-cvEnd".equals(arg)) {
                cvEnd = Long.parseLong(args[++i]);
            } else if ("-cvDelta".equals(arg)) {
                useDelta = true;
            } else if ("-cvFile".equals(arg)) {
                fileName = args[++i];
            } else if ("-cvStart".equals(arg)) {
                cvStart = Long.parseLong(args[++i]);
            } else if ("-i".equals(arg)) {
                ignoreHeaderLength = true;
            } else if ("-q".equals(arg)) {
                quiet = true;
            } else if ("-help".equals(arg) || "-h".equals(arg)) {
                System.out.println("Arguments: \n"
                        + " -cvEnd <long>       optional  default=Long.MAX_VALUE  last vector to print\n\n"
                        + " -cvFile <fileName>  optional  default=candidates.dat  file that contains\n"
                        + " -cvStart <long>     optional  default=-1              first vector to print\n\n"
                        + "                                                       candidate vectors.\n\n"
                        + " -i                  optional  default=false           ignore length written\n"
                        + "                                                       header, read to the end\n"
                        + "                                                       of file\n\n"
                        + " -help               optional  default=false           print this message.\n\n"
                        + " -q                  optional  default=false           quiet, don't print \n"
                        + "                                                       candidate vectors.\n\n");
                System.exit(0);
            }
        }
        
        int generated = 0;
        int explored = 0;    
        
        ICVReader cvReader = null;
        try {
            if (useDelta)
                cvReader = new CVFactoryDelta().createCVReader(fileName);
            else
                cvReader = new CVFactory().createCVReader(fileName);
            
            long n = cvReader.getNumCVs();
            int elemsPerCV = cvReader.getNumElemsPerCV();
            //int bitsPerElem = cvReader.numBitsPerElem;
            System.out.println("Number of candidate vectors: " + n);
            System.out.println("Number elements per candidate vector: " + elemsPerCV);
            //System.out.println("Number of bits per elem: " + bitsPerElem);
        
            if (ignoreHeaderLength) {
                n = Long.MAX_VALUE;
            }
            int[] curr = null;
            for (long i = 0; i < n; i++) {
                explored++;
                curr = cvReader.readCV();
                if (curr == null) {
                    break;
                }
                if (!quiet) {
                    if (i >= cvStart && i <= cvEnd) {
                        System.out.println();
                        System.out.print(i + ")  ");
                        System.out.println(Arrays.toString(curr));
                        System.out.println(cvReader.isPredicateOK());
                    }
                }
                if (cvReader.isPredicateOK()) {
                    generated++;
                }
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (cvReader != null)
                try {
                    cvReader.close();
                } catch (IOException e) {
                }
        }
        
        System.out.println("Number of explored structures: " + explored);
        System.out.println("Number of generated structures: " + generated);
        
    }
    
}
