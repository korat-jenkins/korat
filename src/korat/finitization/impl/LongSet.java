package korat.finitization.impl;

import korat.finitization.ILongSet;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class LongSet extends PrimitiveTypeSet implements ILongSet {

    LongSet(long min, long diff, long max) {
        super(long.class);
        addRange(min, diff, max);
    }

    LongSet(long min, long max) {
        this(min, 1, max);
    }

    LongSet(long value) {
        this(value, 1, value);
    }

    public void addLong(long l) {
        primitives.add(l);
    }

    public void addRange(long min, long diff, long max) {
        if (min > max)
            return;

        long l = min;
        while (l < max) {
            primitives.add(l);
            l += diff;
        }
        primitives.add(max);
    }

    public void removeLong(long l) {
        primitives.remove(l);
    }

    public long getLong(int index) {
        return (Long) primitives.get(index);
    }

    public long[] getLongs() {
        long[] longs = new long[primitives.size()];
        for (int i = 0; i < primitives.size(); i++)
            longs[i] = getLong(i);

        return longs;
    }
}
