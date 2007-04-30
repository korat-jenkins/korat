package korat.utils.cv;

import java.io.IOException;

import korat.config.ConfigManager;


public class CVSim {
    public static void main(String[] args) {
        WriteCVListenerSim cvLis = new WriteCVListenerSim();
        String fileName = "cv-sim.dat";
        ConfigManager.getInstance().cvFile = fileName;
        ConfigManager.getInstance().cvWriteNum = Integer.parseInt(args[0]);
        long n = Long.parseLong(args[1]);
        int[] cv = new int[10];
        for (long i = 0; i < n; i++) {
            cvLis.writeCV(cv);
            incCV(cv);
        }
        cvLis.notifyTestFinished(-1, -1);
        
        printIndexes(fileName);
        
    }

    private static void printIndexes(String fileName) {
        try {
            
            ICVReader reader = CVFactory.getCVFactory().createCVReader(fileName);
            for (int i = 0; i < reader.getNumCVs(); i++) {
                int[] cv = reader.readCV();
                System.out.print(convToDec(cv) + " ");
            }
            System.out.println();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long convToDec(int[] cv) {
        long num = 0;
        for (int i = 0; i < cv.length; i++) {
            num = num * 10 + cv[i];
        }
        return num;
    }

    private static void incCV(int[] cv) {
        int cf = 1, i = cv.length - 1;
        while (cf == 1) {
            int x = cv[i] + 1;
            cv[i] = x % 10;
            cf = x / 10;
            i--;
        }
    }
}
