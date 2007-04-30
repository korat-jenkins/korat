package korat.finitization.impl;

import korat.finitization.IIntSet;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */

// TODO: maybe better NumericTypeSet
public class IntSet extends PrimitiveTypeSet implements IIntSet {

    IntSet(int min, int diff, int max) {
        super(int.class);
        addRange(min, diff, max);
    }

    IntSet(int min, int max) {
        this(min, 1, max);
    }

    IntSet(int value) {
        this(value, 1, value);
    }

    public void addInt(int i) {
        primitives.add(i);
    }

    public void removeInt(int i) {
        primitives.remove(new Integer(i));
    }

    public void addRange(int min, int diff, int max) {
        int i = min;
        while (i < max) {
            primitives.add(i);
            i += diff;
        }
        primitives.add(max);
    }

    public int getInt(int index) {
        return (Integer) primitives.get(index);
    }

    public int[] getInts() {
        int[] ints = new int[primitives.size()];
        for (int i = 0; i < primitives.size(); i++)
            ints[i] = getInt(i);

        return ints;
    }

    public int getMin() {
        if (primitives.size() == 0)
            throw new RuntimeException("Size of int set is zero");

        int min = Integer.MAX_VALUE;
        for (int i = 0; i < primitives.size(); i++) {
            int elem = getInt(i);
            if (elem < min)
                min = elem;
        }

        return min;
    }

    public int getMax() {
        if (primitives.size() == 0)
            throw new RuntimeException("Size of int set is zero");

        int max = Integer.MIN_VALUE;
        for (int i = 0; i < primitives.size(); i++) {
            int elem = getInt(i);
            if (elem > max)
                max = elem;
        }

        return max;
    }

}
