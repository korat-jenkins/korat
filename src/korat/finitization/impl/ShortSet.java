package korat.finitization.impl;

import korat.finitization.IShortSet;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class ShortSet extends PrimitiveTypeSet implements IShortSet {

    ShortSet(short min, short diff, short max) {
        super(short.class);
        addRange(min, diff, max);
    }

    ShortSet(short min, short max) {
        this(min, (short) 1, max);
    }

    ShortSet(short value) {
        this(value, (short) 1, value);
    }

    public void addShort(short s) {
        Short ss = new Short(s);
        primitives.add(ss);
    }

    public void addRange(short min, short diff, short max) {
        if (min > max)
            return;

        short s = min;
        while (s < max) {
            primitives.add(s);
            s += diff;
        }
        primitives.add(new Short(max));
    }

    public void removeShort(short s) {
        primitives.remove(s);
    }

    public short getShort(int index) {
        return (Short) primitives.get(index);
    }

    public short[] getShorts() {
        short[] shorts = new short[primitives.size()];
        for (int i = 0; i < primitives.size(); i++)
            shorts[i] = getShort(i);

        return shorts;
    }

}
