package korat.testing.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.TreeSet;

import korat.config.ConfigManager;
import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.impl.Finitization;
import korat.utils.cv.CVFactory;
import korat.utils.cv.CVFinderDelta;
import korat.utils.cv.CVFinderDelta.FinderResult;

public class TestCradleII extends TestCradle {

    // TODO: get the actual size parameter from the command line
    private int SIZE = 7;
    
    private int processors = 512;
    static public int currP = 0;
    
    private int MAX_N = 100;
    private int MIN_N = 3;
    private int N;
    private int currN;
    
    public static int seed;
    
    
    //private ICVWriter cvWriter;
    private CVFinderDelta cvFinder;
    
    @Override
    protected void startTestGeneration(IFinitization fin) throws CannotInvokePredicateException, CannotFindPredicateException {
        
        Random random = new Random(seed);
        TreeSet<Long> set = new TreeSet<Long>();
        currP = 0;
        
        while (currP < processors) {
            currN = 0;
            N = random.nextInt((processors - currP) + MAX_N) + MIN_N;
            stateSpaceExplorer = new StateSpaceExplorer(fin);
            stateSpace = ((Finitization)fin).getStateSpace();
            initStartAndEndCVs(stateSpaceExplorer);
            
            totalExplored = 0;
            validCasesGenerated = 0;
    
            Object testCase = null;
            Class testCaseClass = fin.getFinClass();
    
            Method predicate = getPredicateMethod(testCaseClass, config.predicate);
    
            /* ---- Search Loop ---- */
    
            accessedFields = stateSpaceExplorer.getAccessedFields();
    
            boolean changed = true;
            while (!interrupted) {
                currN++;
                if (currN == N) {
                    int num = accessedFields.numberOfElements();
                    int nn = Math.min(3 + (int)Math.abs(500 / (processors - currP + 1) * random.nextGaussian()), num - SIZE - 3);
                    changed = false;
                    for (int mm = nn; mm < num - 1; mm++) {
                        for (int i = 0; i < mm; i++) {
                            int lastAccessedField = accessedFields.removeLast();
                            if (lastAccessedField > SIZE) {
                                stateSpaceExplorer.getCandidateVector()[lastAccessedField] = 0;
                                if (stateSpace.getFieldDomain(lastAccessedField) instanceof IIntSet) {
                                //if ((lastAccessedField - SIZE - 2) % 8 == 0) {
                                    for (int off = -2; off < 5; off++) {
                                        stateSpaceExplorer.getCandidateVector()[lastAccessedField + off] = 0;
                                    }
                                }
                                changed = true;
                            }
                        }
                        if (changed) break;
                    }
                    if (!changed) break;
                }
                
                //!!REMEMBER TO TURN OFF PARITAL CANDIDATE BUILDING IN CandidateBuilder CLASS !!!
                testCase = stateSpaceExplorer.nextTestCase();
                if (testCase == null)
                    break;
    
                totalExplored++;
    
                predicateOK = checkPredicate(testCase, predicate);
    
                if (currN == N) {
                    //ICVWriter cvWriter = getCVWriter();
                    try {
                        FinderResult fr = cvFinder.find(getCandidateVector(), getAccessedFields());
                        if (fr.found) {
                            set.add(fr.exactIdx);
                        } else {
                            System.out.println(fr.found + ", " + fr.fromIdx + ", " + fr.toIdx);
                            System.out.println(java.util.Arrays.toString(getCandidateVector()));
                            System.out.println(java.util.Arrays.toString(getAccessedFields().toArray()));
                        }
                        //cvWriter.writeCV(getCandidateVector(), false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        
                    }
                    currP++;
                    // System.out.println("currP " + currP);
                    currN = 0;
                    N = random.nextInt(MAX_N) + MIN_N;
                }
    
                if (currP == processors) {
                    break;
                }
                
                printStatus(stateSpaceExplorer, predicateOK);                
                notifyClients(testCase);
                
                // invokes method, checks for correctness and notifies listeners
                if (predicateOK) {
                    validCasesGenerated++;
                    if (validCasesGenerated == config.maxStructs) {
                        interrupt();
                    } else {
                        stateSpaceExplorer.reportCurrentAsValid();
                    }
                }
    
            }
        }
                
        notifyTestFinished(totalExplored, validCasesGenerated);

        System.out.println("Total explored:" + totalExplored);
        System.out.println("New found:" + validCasesGenerated);
        
        processSet(set);
        
    }

    
    public static long max;
    
    private void processSet(TreeSet<Long> set) {
        long prev = 0;
        max = 0;
        for (long l : set) {
            long diff = l - prev;
            if (diff > max)
                max = diff;
            prev = l;
        }
        long diff = 20128126-prev;
        if (diff > max)
            max = diff;
        System.out.println(max);
    }


    public TestCradleII() {
        try {
            ConfigManager.getInstance().cvDelta = true;
            cvFinder = (CVFinderDelta)CVFactory.getCVFactory().createCVFinder("d:\\cv7delta10000\\c7_delta_10000.dat");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


//    private ICVWriter getCVWriter() {
//        IStateSpace stateSpace = TestCradle.getInstance().getStateSpace();
//        int numElems = stateSpace.getTotalNumberOfFields();
//        int max = 0;
//        for (int i = 0; i < numElems; i++) {
//            int m = stateSpace.getFieldDomain(i).getNumberOfElements();
//            if (m > max) {
//                max = m;
//            }
//        }
//        
//        ICVWriter cvWriter = null;
//        try {
//            cvWriter = CVFactory.getCVFactory().createCVWriter("rand-cand.dat", numElems, max);
//        } catch (Exception e) {
//            cvWriter = null;
//            System.err.println("WARNING: Cannot init WriteCVListener!");
//        }
//        return cvWriter;
//    }

    
    
}
