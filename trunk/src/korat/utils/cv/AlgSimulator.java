package korat.utils.cv;

/**
 * 
 * Simulates algorithm that saves equidistant vectors  
 * 
 *
 */
public class AlgSimulator {
    
    /***
     * 
     *  args[0] - number candidates to store
     *  args[1] - maximal number of candidates (obtained through exploration)
     *  args[2] - if in Expected mode, number of expected candidates   
     */
    public static void main(String[] args) {
        
        if (args.length < 2) {
            System.out.println("Wrong parameter list");
            System.exit(1);
        }
        
        int size = Integer.parseInt(args[0]);        // number of store candidates
        long maxNum = Long.parseLong(args[1]);     // max number of candidates
        
        long [] buff = null;
        
        if (args.length == 2) 
            buff = checkWriteNumMode (size, maxNum);
        
        else if (args.length == 3) {
            long expected = Long.parseLong(args[2]);
            buff = checkExpectedMode (size, maxNum, expected);
            
        } else
            System.exit(1);
           
        print(buff, size);

                 
    }
    
    public static void print(long[] buff, int expectedSize) {

        System.out.print("(");
        for (int i = 0; i < buff.length; i++)
            if (i != buff.length - 1) {
                System.out.print(buff[i] + " ");
                if( i == expectedSize - 1)
                    System.out.print("*** ");
                
            }else 
                System.out.println(buff[i] + ") ");

    }
    
    public static long [] checkWriteNumMode(int size, long candidates) {
        long buff[] = new long[2*size];    //candidate list
        
        int distance = 1;   // distance between cv in list
        
        int cnt = 0; // distance between last and current cv 
        int index = 0; // buffer position where it was written to
        
        for (long cv = 0; cv < candidates; cv++) {
    
            if (cnt == distance) {
                buff[index] = cv;
                cnt = 0;
                index++;
                if (index == buff.length) {
                    int n = buff.length / 2;
                    for (int i = 0; i < n; i++) {
                        buff[i] = buff[ 2*i + 1];
                    }
                    distance *= 2;
                    index = n;
                }
            }
            cnt++;
                       
        }
        
        
        int n = buff.length / 2;
        long ret[] = new long[n];
        int retIndex = 0;
        if (index > n) {
            index--;
        }
        int k = index - n;
        ret[0] = buff[0];
        for (int i = 1; i <= k; i++) {
            ret[++retIndex] = buff[2*i];
        }
        for (int i = 2 * k + 1; i < index; i++) {
            ret[++retIndex] = buff[i];
        }
        
        
        
        
        return ret;
    }
    
    public static long [] checkExpectedMode(int size, long candidates, long expected) {
        
        long len = expected / (size + 1);
        long mod = expected % (size + 1);
        int cnt = 0;
        int index = 0;

        long [] buff = new long[size];
        
        for (long cv = 0; cv <candidates; cv++) {
            long  x = len;
            if (mod > 0) {
                x++;
            }
            if (cnt == x) {
                if (index < buff.length) {
                    buff[index] = cv;
                    index++;
                } else {
                    System.out.println("WARNING: there are more vectors than expected");
                }
                cnt = 0;
                mod--;
            }
            cnt++;
        }
        
        return buff;
        
    }
    
    

}
