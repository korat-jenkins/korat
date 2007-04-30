package korat.config;

/**
 * Single point for obtaining korat options
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class ConfigManager {

    private static ConfigManager instance;

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
            instance.configLoader.loadDefaults();
        }
        return instance;
    }

    private ConfigLoader configLoader;

    private ConfigManager() {
        configLoader = new ConfigLoader();
    }

    /**
     * Parses the cmd line options (actually, delegates the job to 
     * <code>configLoader</code>). 
     * 
     * Korat sets its options using 3 level hierarchy (with ascending priorities):
     * <ol>
     * <li>all non-required options have their default values</li>
     * <li>options can be specified through the config file</li>
     * <li>options can be specified through the cmd line</li>
     * </ol>
     *
     * This means that it first searches cmd line, then config file (if provided)
     * and finally if the option is not specified neither in cmd line or config
     * file, the default value will be used (if the options is not required, 
     * otherwise, an exception will be thrown and execution will stop).
     * 
     * Format of the config file should be like the format of the cmd line with 
     * a couple of additional possibilities:
     * <ol>
     * <li>line breaks are allowed</li>
     * <li>all lines starting with "#" are ignored (considered as comments)</li>
     * </ol>
     * 
     * @param args cmd line arguments
     * @see ConfigLoader#parseCmdLine(String[])
     */
    public void parseCmdLine(String[] args) {
        configLoader.parseCmdLine(args);
    }

    /* ======================================================================== 
     *   ALL KORAT OPTIONS
     * ======================================================================== */

    /**
     * Array of arguments to be passed to the finitization method. 
     */
    public String[] args;
    
    /**
     * Full class name of the class under test.
     */
    public String className;
    
    /**
     * Name of the config file (if used, null otherwise).
     */
    public String config;

    /**
     * Whether or not to use delta file format for storing candidate vectors.
     */
    public boolean cvDelta;

    /**
     * Index in the cvFile of the ending candidate vector which the search
     * algorithm should search to.
     */
    public long cvEnd;

    /**
     * Expected number of total explored vectors. This piece of information can
     * help in optimal equi-distancing when <code>cvWriteNum</code> is set to 
     * <code>true</code>.
     */
    public long cvExpected;

    /**
     * Name of the file that contains candidate vectors.
     */
    public String cvFile;

    /**
     * Ratio of full format vectors (makes sense only if delta file
     * format is used, i.e. <code>cvDelta</code> is set to <code>true</code>).
     */
    public int cvFullFormatRatio;

    /**
     * Index in the cvFile of the starting candidate vector which the search
     * algorithm should start searching from.
     */
    public long cvStart;

    /**
     * Whether or not to write explored candidate vectors to disk.
     */
    public boolean cvWrite;

    /**
     * Number of equi-distant vectors to be written to disk (makes sense only if
     * <code>cvFrite</code> is set to <code>true</code>.
     */
    public int cvWriteNum;

    /**
     * Whether or not to dump instrumented bytecodes.
     */
    public boolean dumpBytecodes;
    
    /**
     * Comma separated list of packages to be excluded from instrumentation.
     */
    public String[] excludePackages;

    /**
     * Name of the finitization method. If not set, defaults to "fin<ClassName>".
     */
    public String finitization;

    /**
     * Comma separated list of <code>ITestCaseListener</code> listeners to be attached
     * to <code>testCradle</code> instance.
     */
    public String[] listeners;

    /**
     * Max number of test cases to be generated. The search algorithm stops when 
     * <code>maxStructs</code> number of test cases is generated.
     */
    public long maxStructs;

    /**
     * Name of the predicate method to be used. If not set, defaults to "repOK".
     */
    public String predicate;

    /**
     * Whether or not to print generated test cases to standard output
     * (<code>toString</code> method would be called on each of the 
     * valid test cases generated).
     */
    public boolean print;

    /**
     * Whether or not to print candidate vector and access field list during 
     * the search process. Should be used for debugging purposes. 
     */
    public boolean printCandVects;

    /**
     * Print progress during search on every <code>progress</code>-th generated 
     * test case. 
     */
    public long progress;

    /**
     * Serialize valid test cases to file with given name.
     */
    public String serialize;

    /**
     * Whether or not to visualize valid test cases. To use this options, "dot" 
     * program from "GraphViz" package (http://www.graphviz.org) must be installed
     * and added to system's PATH environment variable. 
     */
    public boolean visualize;

}
