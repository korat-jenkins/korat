package korat;

import java.util.Arrays;

import korat.config.ConfigLoader;
import korat.config.ConfigManager;
import korat.testing.impl.CannotFindClassUnderTest;
import korat.testing.impl.CannotFindFinitizationException;
import korat.testing.impl.CannotFindPredicateException;
import korat.testing.impl.CannotInvokeFinitizationException;
import korat.testing.impl.CannotInvokePredicateException;
import korat.testing.impl.KoratTestException;
import korat.testing.impl.TestCradle;

/**
 * Korat Main Class
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class Korat {

    /**
     * Loader of Korat Application
     * 
     * @param args -
     *            program arguments are listed below. <p/>
     * 
     * Arguments: <table cellspacing="3" cellpadding="3">
     * <tr>
     * <td style="white-space:nowrap;"><code>--args &lt;arg-list&gt;</code></td>
     * <td>mandatory</td>
     * <td>comma separated list of finitization parameters, ordered as in
     * corresponding finitization method.</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--class &lt;fullClassName&gt;</code></td>
     * <td>mandatory</td>
     * <td>name of test case class</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--config &lt;fileName&gt;</code></td>
     * <td>optional</td>
     * <td>name of the config file to be used</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--cvDelta</code></td>
     * <td>optional</td>
     * <td>use delta file format when storing candidate vectors to disk</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--cvEnd &lt;num&gt;</code></td>
     * <td>optional</td>
     * <td>set the end candidate vector to &lt;num&gt;-th vector from cvFile</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--cvExpected &lt;num&gt;</code></td>
     * <td>optional</td>
     * <td>expected number of total explored vectors</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--cvFile &lt;filename&gt;</code></td>
     * <td>optional</td>
     * <td>name of the candidate-vectors file</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--cvFullFormatRatio &lt;num&gt;</code></td>
     * <td>optional</td>
     * <td>the ratio of full format vectors (if delta file format is used)</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--cvStart &lt;num&gt;</code></td>
     * <td>optional</td>
     * <td>set the start candidate vector to &lt;num&gt;-th vector from cvFile</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--cvWrite</code></td>
     * <td>optional</td>
     * <td>write all explored candidate vectors to file</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--cvWriteNum &lt;num&gt;</code></td>
     * <td>optional</td>
     * <td>write only &lt;num&gt; equi-distant vectors to disk</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--excludePackages &lt;packages&gt;</code></td>
     * <td>optional</td>
     * <td>comma separated list of packages to be excluded from instrumentation</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--finitization &lt;finMethodName&gt;</code></td>
     * <td>optional</td>
     * <td>set the name of finitization method. If ommited, default name
     * fin&lt;ClassName&gt; is used.</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--help</code></td>
     * <td>optional</td>
     * <td>print help message.</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--listeners &lt;listenerClasses&gt;</code></td>
     * <td>optional</td>
     * <td>comma separated list of full class names that implement
     * <code>ITestCaseListener</code> interface.</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--maxStructs</code> &lt;num&gt;</td>
     * <td>optional</td>
     * <td>stop execution after finding &lt;num&gt; invariant-passing
     * structures</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--predicate &lt;predMethodName&gt;</code></td>
     * <td>optional</td>
     * <td>set the name of predicate method. If ommited, default name "repOK"
     * will be used</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--print</code></td>
     * <td>optional</td>
     * <td>print the generated structure to the console</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--printCandVects</code></td>
     * <td>optional</td>
     * <td>print candidate vector and accessed field list during the search.</td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--progress &lt;threshold&gt;</code></td>
     * <td>optional</td>
     * <td>print status of the search after exploration of &lt;threshold&gt;
     * candidates </td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--serialize &lt;filename&gt;</code></td>
     * <td>optional</td>
     * <td>seralize the invariant-passing test cases to the specified file. If
     * filename contains absolute path, use quotes. </td>
     * </tr>
     * 
     * <tr>
     * <td style="white-space:nowrap;"><code>--visualize</code> </td>
     * <td>optional</td>
     * <td>visualize the generated data structures</td>
     * </tr>
     * 
     * </table>
     * 
     * <i>Example command line :: </i> <br/> java korat.Korat --class
     * korat.examples.binarytree.BinaryTree --args 3,3,3
     * 
     */
    public static void main(String[] args) {

        TestCradle testCradle = TestCradle.getInstance();
        ConfigManager config = ConfigManager.getInstance();
        config.parseCmdLine(args);

        System.out.print("\nStart of Korat Execution for " + config.className
                + " (" + config.predicate + ", ");
        System.out.println(Arrays.toString(config.args) + ")\n");

        try {

            long t1 = System.currentTimeMillis();
            testCradle.start(config.className, config.args);
            long t2 = System.currentTimeMillis();

            long dt1 = t2 - t1;
            System.out.println("\nEnd of Korat Execution");
            System.out.println("Overall time: " + dt1 / 1000.0 + " s.");

        } catch (CannotFindClassUnderTest e) {
            
            System.err.println("!!! Korat cannot find class under test:");
            System.err.println("        <class_name> = " + e.getFullClassName());
            System.err.println("    Use -"
                    + ConfigLoader.CLZ.getSwitches()
                    + " switch to provide full class name of the class under test.");
            
        } catch (CannotFindFinitizationException e) {
            
            System.err.println("!!! Korat cannot find finitization method for the class under test:");
            System.err.println("        <class> = " + e.getCls().getName());
            System.err.println("        <finitization> = " + e.getMethodName());
            System.err.println("    Use -"
                    + ConfigLoader.FINITIZATION.getSwitches()
                    + " switch to provide custom finitization method name.");
            
        } catch (CannotFindPredicateException e) {
            
            System.err.println("!!! Korat cannot find predicate method for the class under test:");
            System.err.println("        <class> = " + e.getCls().getName());
            System.err.println("        <predicate> = " + e.getMethodName());
            System.err.println("    Use -"
                    + ConfigLoader.PREDICATE.getSwitches()
                    + " switch to provide custom predicate method name.");
            
        } catch (CannotInvokeFinitizationException e) {
            
            System.err.println("!!! Korat cannot invoke finitization method:");
            System.err.println("        <class> = " + e.getCls().getName());
            System.err.println("        <finitization> = " + e.getMethodName());
            System.err.println();
            System.err.println("    Stack trace:");
            e.printStackTrace(System.err);
            
        } catch (CannotInvokePredicateException e) {
            
            System.err.println("!!! Korat cannot invoke predicate method:");
            System.err.println("      <class> = " + e.getCls().getName());
            System.err.println("      <predicate> = " + e.getMethodName());
            System.err.println();
            System.err.println("    Stack trace:");
            e.printStackTrace(System.err);
            
        } catch (KoratTestException e) {
            
            System.err.println("!!! A korat exception occured:");
            System.err.println();
            System.err.println("    Stack trace:");
            e.printStackTrace(System.err);
            
        }

    }

}
