package korat.utils.cv;

import java.io.IOException;

/**
 * Utility class. Compares outputs of two files with candidate vectors.
 * Checks whether the vectors in the same positions are equal.
 * 
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 *
 */
public class CVComparer {

    
    /**
     * 
     * 
     * @param args
     */
    public static void main(String[] args) {
        

        String fileA = args[0];
        String fileB = args[1];
        
        ICVReader readerA = null, readerB = null;
        try {
             readerA = CVFactory.getCVFactory().createCVReader(fileA);
        } catch (IOException e) {
            System.out.println("Cannot read file " + fileA + ".");
            System.exit(1);
        }
        try {
            readerB = CVFactory.getCVFactory().createCVReader(fileB);
        } catch (IOException e) {
            System.out.println("Cannot read file " + fileB + ".");
            System.exit(1);
        }
        
        long cvsA = readerA.getNumCVs();
        long cvsB = readerB.getNumCVs();
        long factor = 1;
        
        /*
         * readerA always has more candidates to read
         */
        if (cvsB > cvsA){
            long t = cvsA;
            cvsA = cvsB;
            cvsB = t;
            
            String tempS = fileA;
            fileA = fileB;
            fileB = tempS;
            
            ICVReader tempR = readerA;
            readerA = readerB;
            readerB = tempR;
        }
        
        factor = cvsA / cvsB;
        
        System.out.println("Files:  A=" + fileA + " & B=" + fileB + "  (" + factor + ")");
               
        try {

            for (long i = 0; i < cvsB && i < 100; i++) {
                 
                int [] cB = readerB.readCV();
                int [] cA = null;
                
                for (int j = 0; j < factor; j++) {
                    cA = readerA.readCV();
                }
                
                assert (cB.length == cA.length);
                
                boolean [] different = new boolean[cA.length];
                boolean equal = true;
                for (int j = 0; j < cA.length; j++)
                    if (cB[j] != cA[j]) {
                        equal = false;
                        different[j] = true;
                    }

                if (!equal) {
                    System.out.print("\nVector --> [" + i + "]");
                    
                    int res = compare(cA, cB);
                    if (res == 0)
                        System.out.println(" A = B");
                    else if (res < 0)
                        System.out.println(" A < B");
                    else
                        System.out.println(" A > B");
                    
                    println(cA);
                    println(cB);
                    println(different);
                } else
                    System.out.print(".");
  
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        
    }

    private static int compare(int[] ca, int[] cb) {

        for (int i = 0; i < ca.length; i++)
            if (ca[i] < cb[i])
                return -1;
            else if (ca[i] > cb[i])
                return 1;
        return 0;
        
    }

    private static void println(int[] array) {
        for (int i = 0 ; i < array.length; i++)
            System.out.print(array[i] + " ");
        System.out.println();
    }
    
    private static void println(boolean[] array) {
        for (int i = 0 ; i < array.length; i++)
            if (array[i])
                System.out.print("* ");
            else 
                System.out.print("  ");
        System.out.println();
    }
    
}
