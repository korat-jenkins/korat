package korat.testing.impl;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import korat.config.ConfigManager;
import korat.finitization.IFinitization;
import korat.finitization.impl.Finitization;
import korat.finitization.impl.StateSpace;
import korat.loading.InstrumentingClassLoader;
import korat.testing.IKoratSearchStrategy;
import korat.testing.ITester;
import korat.utils.IIntList;
import korat.utils.cv.CVFactory;
import korat.utils.cv.ICVFactory;
import korat.utils.cv.ICVFinder;

/**
 * Given the Finitization, conducts tests for all regular candidates in domain
 * state space
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class TestCradle extends AbstractTestCaseGenerator implements ITester {

    private static TestCradle instance = new TestCradle();

    public static TestCradle getInstance() {
        return instance;
    }

    private ICVFactory cvFactory;
    
    protected ClassLoader classLoader;
    
    protected TestCradle() {
        classLoader = new InstrumentingClassLoader();
        Finitization.setClassLoader(classLoader);
        cvFactory = CVFactory.getCVFactory();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /*
     * -------------------------------------------------------------------------
     * Implementation of ITester interface.
     * -------------------------------------------------------------------------
     */

    private boolean traceStarted;

    public void startFieldTrace() {
        traceStarted = true;
        accessedFields.clear();
    }

    public void continueFieldTrace() {
        traceStarted = true;
    }

    public void stopFieldTrace() {
        traceStarted = false;
    }

    public void notifyFieldAccess(Object obj, String field) {
        if (!traceStarted)
            return;

        int accessedFieldIndex = stateSpace.getIndexInCandidateVector(obj,
                field);

        if (accessedFieldIndex != -1)
            accessedFields.add(accessedFieldIndex);
    }

    public void notifyFieldAccess(int accessedFieldIndex) {
        if (!traceStarted)
            return;

        if (accessedFieldIndex != -1)
            accessedFields.add(accessedFieldIndex);
    }

    /*
     * -------------------------------------------------------------------------
     * Info about korat execution that can be obtained after calling start
     * method.
     * -------------------------------------------------------------------------
     */

    protected long validCasesGenerated;

    protected long totalExplored;

    public long getValidCasesGenerated() {
        return validCasesGenerated;
    }

    public long getTotalExplored() {
        return totalExplored;
    }
    
    /*
     * -------------------------------------------------------------------------
     * Info that listeners may query in the middle of the search process.
     * -------------------------------------------------------------------------
     */
    
    public int[] getCandidateVector() {
        return stateSpaceExplorer.getCandidateVector().clone();
    }
    
    public boolean isPredicateOK() {
        return predicateOK;
    }
    
    public IIntList getAccessedFields() {
        return stateSpaceExplorer.getAccessedFields();
    }
    
    public StateSpace getStateSpace() {
        return stateSpace;
    }
    
    /*
     * -------------------------------------------------------------------------
     * Start method.
     * -------------------------------------------------------------------------
     */

    public void start(String className, String[] finArgs)
            throws KoratTestException {

        try {
        
            Class clazz = classLoader.loadClass(className);
            start(clazz, finArgs);
        
        } catch (ClassNotFoundException e) {
            throw new CannotFindClassUnderTest(className, e.getMessage(), e);
        }
        
    }

    /*
     * -------------------------------------------------------------------------
     * Internal stuff.
     * -------------------------------------------------------------------------
     */

    protected ConfigManager config = ConfigManager.getInstance();
    
    private String finName = null;

    protected StateSpace stateSpace;

    protected IIntList accessedFields;

    protected IKoratSearchStrategy stateSpaceExplorer;

    protected boolean predicateOK;

    private void start(Class clazz, String[] finArgs)
            throws KoratTestException {

        //COMPAT1.4
        //finName = getFinName(clazz.getSimpleName());
        finName = config.finitization;
        Method finitize = getFinMethod(clazz, finName, finArgs);
        IFinitization fin = invokeFinMethod(clazz, finitize, finArgs);
        startTestGeneration(fin);

    }

    private Method getFinMethod(Class cls, String finName, String[] finArgs)
            throws CannotFindFinitizationException {

        Method finitize = null;
        for (Method m : cls.getDeclaredMethods()) {
            if (finName.equals(m.getName())
                    && m.getParameterTypes().length == finArgs.length) {
                finitize = m;
                break;
            }
        }
        if (finitize == null) {
            throw new CannotFindFinitizationException(cls, finName);
        }
        return finitize;
        
    }

    private IFinitization invokeFinMethod(Class cls, Method finitize,
            String[] finArgs) throws CannotInvokeFinitizationException {

        int paramNumber = finArgs.length;
        Class[] finArgTypes = finitize.getParameterTypes();
        Object[] finArgValues = new Object[paramNumber];
        
        for (int i = 0; i < paramNumber; i++) {
            Class clazz = finArgTypes[i];
            String arg = finArgs[i].trim();
            Object val;

            if (clazz == boolean.class || clazz == Boolean.class) {
                val = Boolean.parseBoolean(arg);
            } else if (clazz == byte.class || clazz == Byte.class) {
                val = Byte.parseByte(arg);
            } else if (clazz == double.class || clazz == Double.class) {
                val = Double.parseDouble(arg);
            } else if (clazz == float.class || clazz == Float.class) {
                val = Float.parseFloat(arg);
            } else if (clazz == int.class || clazz == Integer.class) {
                val = Integer.parseInt(arg);
            } else if (clazz == long.class || clazz == Long.class) {
                val = Long.parseLong(arg);
            } else if (clazz == short.class || clazz == Short.class) {
                val = Short.parseShort(arg);
            } else if (clazz == String.class) {
                val = arg;
            } else
                throw new CannotInvokeFinitizationException(cls, finitize.getName(),
                        "Only parameters of primitive classes are allowed");

            finArgValues[i] = val;
        }

        try {
            return (IFinitization) finitize.invoke(null, (Object[]) finArgValues);
        } catch (Exception e) {
            throw new CannotInvokeFinitizationException(cls, finitize.getName(), e);
        }

    }

    protected void startTestGeneration(IFinitization fin)
            throws CannotInvokePredicateException, CannotFindPredicateException {

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

        if (config.printCandVects)
            System.out.println(stateSpace);

        while (!interrupted) {

            testCase = stateSpaceExplorer.nextTestCase();
            if (testCase == null)
                break;

            totalExplored++;

            predicateOK = checkPredicate(testCase, predicate);

            if (predicateOK)
                validCasesGenerated++;
            printStatus(stateSpaceExplorer, predicateOK);
            
            notifyClients(testCase);
            
            // invokes method, checks for correctness and notifies listeners
            if (predicateOK) {
                if (validCasesGenerated == config.maxStructs) {
                    interrupt();
                } else {
                    stateSpaceExplorer.reportCurrentAsValid();
                }
            }

        }

        if (dos != null) {
            try {
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        notifyTestFinished(totalExplored, validCasesGenerated);

        System.out.println("Total explored:" + totalExplored);
        System.out.println("New found:" + validCasesGenerated);

    }

    protected Method getPredicateMethod(Class<?> testClass, String predicateName)
            throws CannotFindPredicateException {
        try {
            return testClass.getMethod(predicateName, (Class[]) null);
        } catch (Exception e) {
            throw new CannotFindPredicateException(testClass, predicateName, e);
        }
    }

    protected boolean checkPredicate(Object testCase, Method predicate)
            throws CannotInvokePredicateException {
        startFieldTrace();
        try {
            return (Boolean) predicate.invoke(testCase, (Object[]) null);
        } catch (Exception e) {
            throw new CannotInvokePredicateException(testCase.getClass(),
                    predicate.getName(), e.getMessage(), e);
        } finally {
            stopFieldTrace();
        }
    }

    protected void initStartAndEndCVs(IKoratSearchStrategy ssExplorer) {
        long endCVNo = config.cvEnd;
        long startCVNo = config.cvStart;
        if (endCVNo == -1 && startCVNo == -1)
            return;
        
        ICVFinder cvFile = null;        
        try {
            
            cvFile = cvFactory.createCVFinder(config.cvFile);
            if (startCVNo != -1) {
                ssExplorer.setStartCandidateVector(cvFile.readCV(startCVNo));
            }
            if (endCVNo != -1) {
                ssExplorer.setEndCandidateVector(cvFile.readCV(endCVNo));
            }
                        
        } catch (Exception e) {
            throw new RuntimeException("Exception during accessing file with candidate vectors", e);
        } finally {
            try {
                if (cvFile != null)
                    cvFile.close();
            } catch (IOException e) {
            }
        }
    }
   
    private DataOutputStream dos = null;
    
    protected void printStatus(IKoratSearchStrategy sse, boolean predicateOK) {

        long progressThreshold = config.progress;
        if (progressThreshold > 0 && totalExplored % progressThreshold == 0) {
            
            ///////////////////////////////////////////////////
            if (dos == null) {
                try {
                    dos = new DataOutputStream(
                            new BufferedOutputStream(new FileOutputStream("acclist.dat")));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            
            IIntList accList = getAccessedFields();
            try {
                dos.writeLong(totalExplored - 1);
                int[] cv = sse.getCandidateVector();
                dos.writeInt(cv.length);
                for (int i = 0; i < cv.length; i++)
                    dos.writeInt(cv[i]);
                
                dos.writeInt(accList.numberOfElements());
                for (int i = 0; i < accList.numberOfElements(); i++) {
                    dos.writeInt(accList.get(i));
                }
                dos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            /////////////////////////////////////////////////////
            
            System.out.println("Total explored:" + totalExplored);
            System.out.println("New found:" + validCasesGenerated);
            
            int[] cv = sse.getCandidateVector();
            for (int i = 0; i < cv.length; i++)
                System.out.print(cv[i] + " ");
            
            System.out.print(" :: ");
            for (int i = 0; i < accList.numberOfElements(); i++) {
                System.out.print(accList.get(i) + " ");
            }
            System.out.println("\n");
            
        } else if (config.printCandVects) {
            printCV(sse, predicateOK);
        }
    }

    
    private void printCV(IKoratSearchStrategy sse, boolean predicateOK) {
        int[] cv = sse.getCandidateVector();
        for (int i = 0; i < cv.length; i++)
            System.out.print(cv[i] + " ");
        System.out.print(" :: ");

        IIntList fieldAccesses = sse.getAccessedFields();
        int[] acA = fieldAccesses.toArray();
        for (int i = 0; i < acA.length; i++)
            System.out.print(acA[i] + " ");

        if (predicateOK) {
            System.out.println("***");
        } else {
            System.out.println();
        }
    }

}
