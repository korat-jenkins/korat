package korat.utils.cv;


public class Cmp extends AlgSimulator {
    /***
         * 
         *  args[0] - number candidates to store
         *  args[1] - maximal number of candidates (obtained through exploration)
         *  args[2] - if in Expected mode, number of expected candidates   
         */
        public static void main(String[] args) {
            
            if (args.length < 3) {
                System.out.println("Wrong parameter list");
                System.exit(1);
            }
            
            int size = Integer.parseInt(args[0]);        // number of store candidates
            long maxNum = Long.parseLong(args[1]);     // max number of candidates
            long expected = Long.parseLong(args[2]);
            
            long [] buffN = null;
            long [] buffE = null;
            

            buffN = checkWriteNumMode (size, maxNum);
            buffE = checkExpectedMode (size, maxNum, expected);

            System.out.println("Old algorithm from the paper");
            checkDistance (buffN);
            System.out.println("'Expected' algorithm (optimal)");
            checkDistance (buffE);
               
            
        }

    private static void checkDistance(long[] buff) {
      
        long diff = buff[0];    //!
        System.out.println(" index   cv[i-1]   cv[i]   distance("+ diff +")  distance%");
        for (int i = 1; i < buff.length; i++) {
            long d = buff[i]-buff[i-1];
            if (d != diff){
                int reldist = (int)(((double)(d-diff))/diff * 100);
                if (reldist < 0) reldist = -reldist;
                System.out.println(" " + i + "    " + buff[i-1] + "    " + buff[i] + "    " + d + "    " + reldist);
            }
        }

    }
   
        
        


}
