package korat.instrumentation.examples;

/**
 * Simple class for testing int arrays instrumentation.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class IntArrayTestClass {

    int[] ii;

    int[] jj = new int[30];

    public IntArrayTestClass() {
        ii = new int[10];
    }

    public int sum(int x) {

        ii = new int[x + 1];

        for (int i = 0; i < ii.length; i++) {
            ii[i] = i;
        }

        int s = 0;
        for (int i = 0; i < ii.length; i++) {
            s += ii[i];
        }

        return s;

    }

    public int f() {

        ii = new int[5];
        jj = new int[10];

        int i;
        for (i = 0; i < ii.length; i++) {
            ii[i] = i * i;
            jj[i] = i + ii[i];
        }

        for (; i < jj.length; i++)
            jj[i] = i;

        ii = jj;

        int s = ii.length;
        for (int k = 0; k < ii.length; k++)
            s += ii[k];

        return s;

    }

}
