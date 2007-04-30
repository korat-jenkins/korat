package korat.utils;

/**
 * General purpose class for holding a pair of data
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 * @param <E1>
 * @param <E2>
 */
public class Pair<E1 extends Object, E2 extends Object> {
    
    private E1 data1;
    
    private E2 data2;

    public Pair() {
    }
    
    public Pair(E1 data1, E2 data2) {
        this.data1 = data1;
        this.data2 = data2;
    }

    public E1 getData1() {
        return data1;
    }

    public void setData1(E1 data1) {
        this.data1 = data1;
    }

    public E2 getData2() {
        return data2;
    }

    public void setData2(E2 data2) {
        this.data2 = data2;
    }
    
}
