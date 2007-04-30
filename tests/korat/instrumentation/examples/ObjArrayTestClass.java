package korat.instrumentation.examples;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 */
public class ObjArrayTestClass {

    private String[] ss = new String[2];

    public ObjArrayTestClass() {

        ss[0] = "korat";
        ss[1] = "rules";

    }

    public int getLength() {
        return ss.length;
    }

    public int getNumOfLetters() {

        int n = 0;

        for (int i = 0; i < ss.length; i++)
            n += ss[i].length();

        return n;

    }

    public String reverse(String s) {

        int n = s.length();
        ss = new String[n];
        for (int i = 0; i < n; i++)
            ss[i] = s.charAt(i) + "";

        String ret = "";

        for (int i = n - 1; i >= 0; i--)
            ret += ss[i];

        return ret;

    }
}
