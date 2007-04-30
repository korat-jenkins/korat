package korat.finitization.impl;

import korat.finitization.IDoubleSet;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class DoubleSet extends PrimitiveTypeSet implements IDoubleSet {

    DoubleSet(double min, double diff, double max) {
        super(double.class);
        addRange(min, diff, max);
    }

    DoubleSet(double min, double max) {
        this(min, 1.0, max);
    }

    DoubleSet(double value) {
        this(value, 1.0, value);
    }

    public void addDouble(double d) {
        Double dd = new Double(d);
        primitives.add(dd);
    }

    public void addRange(double min, double diff, double max) {
        if (min > max)
            return;

        double d = min;
        int k = 0;
        while (d < max) {
            primitives.add(d);
            d = min + diff * (++k);
        }
        primitives.add(max);
    }

    public void removeDouble(double d) {
        primitives.remove(d);
    }

    public double getDouble(int index) {
        return (Double) primitives.get(index);
    }

    public double[] getDoubles() {
        double[] doubles = new double[primitives.size()];
        for (int i = 0; i < primitives.size(); i++)
            doubles[i] = getDouble(i);

        return doubles;
    }
}
