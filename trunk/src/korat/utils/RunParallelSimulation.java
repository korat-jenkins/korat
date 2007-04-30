package korat.utils;

import java.util.Random;
import java.util.TreeSet;

import korat.testing.impl.TestCradleII;

public class RunParallelSimulation {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Enter number of seeds to generate");
            System.exit(-1);
        }
        int seeds = Integer.parseInt(args[0]);
        int totalNum = Integer.parseInt(args[1]);
        
        Random random = new Random(123);
        TreeSet<Integer> seedsSet = new TreeSet<Integer>();
        while (seedsSet.size() < seeds) {
            seedsSet.add(random.nextInt(10*seeds));
        }
        String cmdLine = "--class korat.examples.dag.DAG --cvDelta --args 7";
        String msg = "";
        for (int seed : seedsSet) {
            TestCradleII.seed = seed;
            korat.Korat.main(cmdLine.split(" "));
            msg += "Seed = " + seed + ", max_distance = " + 100.0 * TestCradleII.max / totalNum + "%\n";
        }
        
        System.out.println("\n------\n" + msg);
        
    }
    
}
