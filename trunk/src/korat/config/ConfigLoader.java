package korat.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;

import korat.gui.viz.VizListener;
import korat.loading.filter.ExcludingPackageFilter;
import korat.loading.filter.FilterManager;
import korat.testing.ITestCaseListener;
import korat.testing.impl.TestCradle;
import korat.utils.ReflectionUtils;
import korat.utils.SerializationListener;
import korat.utils.cv.WriteCVListener;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;

/**
 * Class that should be used only by <code>CodeManager</code> for 
 * command line options loading/parsing purposes. 
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class ConfigLoader {

    public static final char SEP = ',';
    
    public static final String SEP_STR = String.valueOf(SEP);
    
    /* =================================================================================
     *   KORAT OPTIONS
     *   
     *   IMPORTANT: each of these options (except for HELP) must have its corresponding 
     *   field in the ConfigManager class which is named exaclty as the longOpt option's 
     *   property (this is because the storeOptions() method relies on it to store all 
     *   parsed options to ConfigManager instance using reflection.
     * ================================================================================= */
    
    public static final MyOption ARGS = new MyOption( 
        "a", "args", "comma separated list of arguments to be given to finitization method", true, true, "params", null);
    
    public static final MyOption CLZ = new MyOption( 
        "c", "class", "name of test case class", true, true, "fullClassName", null);
    
    public static final MyOption CONFIG_FILE = new MyOption( 
        "G", "config", "name of the config file to be used", false, true, "fileName", null);

    public static final MyOption CV_DELTA = new MyOption( 
        "d", "cvDelta", "use delta file format", false, false, "", "false");
    
    public static final MyOption CV_END = new MyOption( 
        "e", "cvEnd", "set the end candidate vector to num-th vector from cvFile", false, true, "num", "-1");
    
    public static final MyOption CV_EXPECTED = new MyOption( 
        "x", "cvExpected", "expected number of total explored vectors", false, true, "num", "-1");
    
    public static final MyOption CV_FILE = new MyOption( 
        "f", "cvFile", "name of the file that contains candidate vectors", false, true, "fileName", "candidates.dat");
    
    public static final MyOption CV_FULL_FORMAT_RATIO = new MyOption( 
        "F", "cvFullFormatRatio", "the ratio of full format vectors (if using delta cv file format)", false, true, "num", "1000");
    
    public static final MyOption CV_START = new MyOption( 
        "A", "cvStart", "set the start candidate vector to num-th vector from cvFile", false, true, "num", "-1");    
    
    public static final MyOption CV_WRITE = new MyOption( 
        "w", "cvWrite", "write explored candidate vectors to disk", false, false, "", "false");
    
    public static final MyOption CV_WRITE_NUM = new MyOption( 
        "W", "cvWriteNum", "write only num equi-distant vectors to disk", false, true, "num", "-1");
    
    public static final MyOption DUMP_BYTECODES = new MyOption( 
        "B", "dumpBytecodes", "dump instrumented java bytecodes to disk", false, false, "", "false");
    
    public static final MyOption EXCLUDE_PACKAGES = new MyOption( 
        "E", "excludePackages", "comma separated list of packages to be excluded from instrumentation", false, true, "packages", null);
    
    public static final MyOption FINITIZATION = new MyOption( 
        "n", "finitization", "set the name of the finitization method. If ommited, default name fin<ClassName> would be used", false, true, "finMethodName", null);

    public static final MyOption HELP = new MyOption( 
        "?", "help", "print help message and exit", false, false, "", "false");
    
    public static final MyOption LISTENERS = new MyOption( 
        "l", "listeners", "comma separated list of full class names that implement ITestCaseListener interface", false, true, "listenerClasses", null);
    
    public static final MyOption MAX_STRUCT = new MyOption( 
        "M", "maxStructs", "stop execution after finding num test cases", false, true, "num", "-1");
    
    public static final MyOption PREDICATE = new MyOption( 
        "r", "predicate", "set the name of predicate method. If ommited, default name repOK would be used", false, true, "predMethodName", "repOK");
    
    public static final MyOption PRINT = new MyOption( 
        "z", "print", "print generated structures", false, false, "", "false");

    public static final MyOption PRINT_CAND_VECTS = new MyOption( 
        "D", "printCandVects", "print candidate vectors and access field lists during the search", false, false, "", "false");
    
    public static final MyOption PROGRESS = new MyOption( 
        "g", "progress", "print progress during search", false, true, "threshold", "-1");
    
    public static final MyOption SERIALIZE = new MyOption( 
        "s", "serialize", "serialize test cases to the given file", false, true, "fileName", null);
    
    public static final MyOption VISUALIZE = new MyOption( 
        "v", "visualize", "visualize valid test cases", false, false, "", "false");

    /**
     * This contains all defined options and will be initialized dynamically, through 
     * the java reflection mechanism.
     */
    private Options koratOptions;

    ConfigLoader() {
        koratOptions = new Options();
        initKoratOptions();
    }

    /**
     * initialize koratOptions with all declared options
     */
    private void initKoratOptions() {
        try {
            Class clz = getClass();
            for (Field f : ReflectionUtils.getAllFields(clz)) {
                if (Option.class.isAssignableFrom(f.getType())) {
                    koratOptions.addOption((Option)f.get(null));
                }
            }
        } catch (Exception e) {
            //should never get here
            throw new RuntimeException("WHY!!!", e);
        }
    }
    
    /* =================================================================================
     *   INTERFACE TO ConfigManager
     * ================================================================================= */
    
    void printUsage(Options options) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(System.out);
            printUsage(pw, options);
        } finally {
            if (pw != null) 
                pw.close();
        }
    }
    
    void printUsage(PrintWriter pw, Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.defaultWidth = 70;
        hf.printHelp(pw, hf.defaultWidth, "java korat.Korat", null, options,
                hf.defaultLeftPad, hf.defaultDescPad, null, true);
    }
    
    void parseCmdLine(String[] args) {
        parseArgs(args, false);
    }
    
    /* =================================================================================
     * PRIVATE STUFF FOR LOADING/PARSING/STORING OPTIONS
     * ================================================================================= */
    
    private CommandLine cmdLine;
    private CommandLine fromFileCmdLine;
    
    /**
     * <p>Parses options from the given string array. </p>
     *  
     * The algorithm for handling options is as follows:
     * <ol>
     * <li>if <code>HELP</code> options is found - prints usage and exists</li>
     * <li>if the given options are cmd line options (not from config file) and 
     * if the <code>CONFIG_FILE</code> option is found, then first loads options from the
     * file specified through the <code>CONFIG_FILE</code> option.</li>
     * <li>loads options from cmd line</li>
     * <li>stores options to <code>ConfigManger</code> instance</li>
     * <li>checks for required options</li>
     * <li>initializes stuff according to the previously parsed options</li>
     * </ol> 
     * 
     * @param args 
     *           options to parse
     * @param fromFile 
     *           whether the given options are from file or from cmd line.
     * @see #loadFromFile(String)
     * @see #processConfigFile(InputStream)
     * @see #storeOptions()
     * @see #checkRequiredOptions()
     * @see #initStuffFromOptions()          
     */
    private void parseArgs(String[] args, boolean fromFile) {
        try {
            
            CommandLine cl = new PosixParser().parse(koratOptions, args);
            if (fromFile) {
                fromFileCmdLine = cl;
            } else {
                cmdLine = cl;
            }
            if (cl.hasOption(HELP.getOpt())) {
                printUsage(koratOptions);
                System.exit(0);
            }
            if (!fromFile) {
                if (cl.hasOption(CONFIG_FILE.getOpt())) {
                    String confFileName = cl.getOptionValue(CONFIG_FILE.getOpt());
                    try {
                        args = loadFromFile(confFileName);
                        parseArgs(args, true);
                    } catch (IOException e) {
                        System.err.println("Cannot load file: " + confFileName);
                    }
                }
            }

            storeOptions();
            checkRequiredOptions();
            initStuffFromOptions();
            
        } catch (ParseException pe) {
            String msg = "";
            if (pe instanceof MissingOptionException)
                msg = "The following required options are missing: ";
            else if (pe instanceof UnrecognizedOptionException)
                msg = "The following options were not recognized: ";
            System.err.println(msg + pe.getMessage());
            System.err.println();
            System.exit(5);
        }
    }

    /**
     * <p>Loads options from the config file and returns them as a string array.</p>
     * 
     * <p>This method only searches for the file with the given name and then calls
     * <code>processConfigFile</code> which does the conversion from the file format
     * to string array</p>
     * 
     * @param fileName 
     *         name of the config file
     * @return 
     *         options from file represented as string array (just like 
     *         they were specified through the cmd line). 
     * @throws IOException 
     *         if an I/O exception occurs.
     * @see #processConfigFile(InputStream)        
     */
    private String[] loadFromFile(String fileName) throws IOException {
        File f = new File(fileName);
        InputStream is = null;
        if (f.exists()) {
            is = new FileInputStream(f);
        } else {
            is = ClassLoader.getSystemResourceAsStream(fileName);
            if (is == null)
                is = ClassLoader.getSystemResourceAsStream("/" + fileName);
        }

        if (is == null)
            throw new FileNotFoundException("Cannot find: " + fileName);

        try {

            return processConfigFile(is);
            
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("Cannot close properties file.");
                    e.printStackTrace(System.err);
                }
        }
        
    }

    /**
     * <p>Loads options from the config file and returns them as a string array.</p>
     * 
     * Format of the config file should be like the format of the cmd line with 
     * a couple of additional possibilities:
     * <ol>
     * <li>line breaks are allowed</li>
     * <li>all lines starting with "#" are ignored (considered as comments)</li>
     * </ol>
     * 
     * @param is 
     *         input stream of the config file
     * @return 
     *         options from file represented as string array (just like 
     *         they were specified through the cmd line).
     * @throws IOException if an I/O exception occurs.
     */
    private static String[] processConfigFile(InputStream is) throws IOException {
        LinkedList<String> argsList = new LinkedList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#"))
                    continue;
                Collections.addAll(argsList, line.split("\\s+"));
            }
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
            }
        }
        return argsList.toArray(new String[0]);
    }

    /**
     * Checks for the presence of the required options.
     * 
     * @throws MissingOptionException if a required options is not provided.
     */
    private void checkRequiredOptions() throws MissingOptionException {
        String missingOptions = "";
        ConfigManager config = ConfigManager.getInstance();
        if (config.className == null) {
            missingOptions += CLZ.getSwitches();
        }
        if (config.args == null) {
            if (missingOptions.length() > 0)
                missingOptions += ", ";
            missingOptions += ARGS.getSwitches();
        }
        if (missingOptions.length() > 0) {
            throw new MissingOptionException(missingOptions);
        }
    }

    /**
     * Initializes stuff from the previously parsed options. 
     */
    private void initStuffFromOptions() {
        TestCradle testCradle = TestCradle.getInstance();
        ConfigManager config = ConfigManager.getInstance();
        if (config.cvWrite) {
            testCradle.attachSpecialClient(new WriteCVListener());
        }
        if (config.excludePackages != null) {
            ExcludingPackageFilter excludeFilter = new ExcludingPackageFilter();
            for (String pckName : config.excludePackages) {
                excludeFilter.addPackage(pckName);
            }
            FilterManager.addFilter(excludeFilter);
        }
        if (config.finitization == null) {
            String className = config.className;
            config.finitization = "fin" + className.substring(className.lastIndexOf('.') + 1);
        }
        if (config.listeners != null) {
            for (String listenerClassName : config.listeners) {
                try {
                    Class clz = Class.forName(listenerClassName, true,
                            testCradle.getClassLoader());
                    testCradle.attachClient((ITestCaseListener) clz.newInstance());
                } catch (Exception e1) {
                    System.err.println("Cannot attach \"" + listenerClassName
                            + "\" listener.");
                    e1.printStackTrace();
                }
            }
        }
        if (config.print) {
            testCradle.attachClient(new ITestCaseListener() {
                public void notifyNewTestCase(Object testCase) {
                    System.out.println(testCase);
                }

                public void notifyTestFinished(long numOfExplored,
                        long numOfGenerated) {
                }
            });
        }
        if (config.serialize != null) {
            testCradle.attachClient(new SerializationListener(config.serialize));            
        }
        if (config.visualize) {
            testCradle.attachClient(new VizListener(true));
        }
    }

    /**
     * Stores the previously parsed options to <code>ConfigManager</code> instance.
     * Since it uses java reflection to automatically store all declared options, 
     * it relies on the "naming rule" of the MyOption options and option fields in
     * ConfigManager class: <strong>each of MyOptions options (except for HELP) must 
     * have its corresponding field in the ConfigManager class which is named exactly 
     * as the longOpt property of that option.</strong>
     */
    private void storeOptions() {
        ConfigManager config = ConfigManager.getInstance();
        for (Object optObj : koratOptions.getOptions()) {
            MyOption opt = (MyOption)optObj;
            if (opt == HELP) 
                continue;
            String optName = opt.getLongOpt();
            if ("class".equals(optName)) {   
                optName = "className"; //handling the single exception from the naming rule
            } 
            Field optField = null;
            try {
                optField = ReflectionUtils.getField(config.getClass(), optName);
                if (optField == null) {
                    throw new RuntimeException("Option with name \"" + optName + "\" was not found in the config manager class");
                }
            } catch (Throwable t) {
                throw new RuntimeException("Cannot get option \"" + optName + "\" from the config manager class", t);
            }
            Class fieldType = optField.getType();
            Object val = null;
            if (fieldType == boolean.class) {
                val = getFlagOption(opt);
            } else if (fieldType == int.class) {
                val = getIntOptionValue(opt);
            } else if (fieldType == long.class) {
                val = getLongOptionValue(opt);
            } else if (fieldType == String.class) {
                val = getOptionValue(opt);
            } else if (fieldType == String[].class) {
                val = getStringArrayOptionValue(opt);
            }
            try {
                optField.set(config, val);
            } catch (Exception e) {
                throw new RuntimeException("Cannot set the option \"" + optName + "\" in the config manager class", e);
            }
        }
//        config.className = getOptionValue(CLZ);
//        config.cvDelta = getFlagOption(CV_DELTA);
//        config.cvEnd = getLongOptionValue(CV_END);
//        config.cvExpected = getLongOptionValue(CV_EXPECTED);
//        config.cvFile = getOptionValue(CV_FILE);
//        config.cvFullFormatRatio = getIntOptionValue(CV_FULL_FORMAT_RATIO);
//        config.cvStart = getLongOptionValue(CV_START);
//        config.cvWrite = getFlagOption(CV_WRITE);
//        config.cvWriteNum = getIntOptionValue(CV_WRITE_NUM);
//        config.dumpBytecodes = getFlagOption(DUMP_BYTECODES);
//        config.excludePackages = getOptionValue(EXCLUDE_PACKAGES);
//        config.finName = getOptionValue(FIN_NAME);
//        config.listeners = getOptionValue(LISTENERS);
//        config.maxStructs = getLongOptionValue(MAX_STRUCT);
//        config.finArgs = getStringArrayOptionValue(ARGS);
//        config.predicateName = getOptionValue(PREDICATE_NAME);
//        config.print = getFlagOption(PRINT);
//        config.printCandVects = getFlagOption(PRINT_CAND_VECTS);
//        config.progress = getLongOptionValue(PROGRESS);
//        config.serialize = getOptionValue(SERIALIZE);
//        config.visualize = getFlagOption(VISUALIZE);
    }

    public void loadDefaults() {
        CommandLine cmdLineBak = cmdLine;
        CommandLine fromFileCmdLineBak = fromFileCmdLine;
        cmdLine = null;
        fromFileCmdLine = null;
        storeOptions();
        fromFileCmdLine = fromFileCmdLineBak;
        cmdLine = cmdLineBak;
    }
    
    private String[] getStringArrayOptionValue(MyOption opt) {
        String strArr = getOptionValue(opt);
        if (strArr == null)
            return null;
        return strArr.split(SEP_STR);
    }

    private String getOptionValue(MyOption opt) {
        String optVal = null;
        if (cmdLine != null) {
            optVal = cmdLine.getOptionValue(opt.getOpt());
        }
        if (optVal == null) {
            if (fromFileCmdLine != null)
                optVal = fromFileCmdLine.getOptionValue(opt.getOpt(), opt.argDefValue);
            else
                optVal = opt.argDefValue;
        }
        return optVal;
    }
    
    private boolean getFlagOption(MyOption opt) {
        if (cmdLine != null && cmdLine.hasOption(opt.getOpt()))
            return true;
        if (fromFileCmdLine != null && fromFileCmdLine.hasOption(opt.getOpt()))
            return true;        
        return false;
    }
    
    private int getIntOptionValue(MyOption opt) {
        int val = -1;
        String strVal = getOptionValue(opt);
        if (strVal == null)
            return val;
        try {
            val = Integer.parseInt(strVal);
        } catch (NumberFormatException e) {
            System.err.println("Option value for " + opt.getLongOpt() + " should be of type int.");
            e.printStackTrace();
            System.exit(2);
        }
        return val;
    }
    
    private long getLongOptionValue(MyOption opt) {
        long val = -1;
        String strVal = getOptionValue(opt);
        if (strVal == null)
            return val;
        try {
            val = Long.parseLong(strVal);
        } catch (NumberFormatException e) {
            System.err.println("Option value for " + opt.getLongOpt() + " should be of type long.");
            e.printStackTrace();
            System.exit(2);
        }
        return val;
    }

}