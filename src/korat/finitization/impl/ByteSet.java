package korat.finitization.impl;

import korat.finitization.IByteSet;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class ByteSet extends PrimitiveTypeSet implements IByteSet {

    ByteSet(byte min, byte diff, byte max) {
        super(byte.class);
        addRange(min, diff, max);
    }

    ByteSet(byte min, byte max) {
        this(min, (byte) 1, max);
    }

    ByteSet(byte value) {
        this(value, (byte) 1, value);
    }

    public void addByte(byte b) {
        primitives.add(b);
    }

    public void addRange(byte min, byte diff, byte max) {
        if (min > max)
            return;

        byte b = min;
        while (b < max) {
            primitives.add(new Byte(b));
            b += diff;
        }
        primitives.add(new Byte(max));
    }

    public void removeByte(byte b) {
        Byte bb = new Byte(b);
        primitives.remove(bb);
    }

    public byte getByte(int index) {
        return ((Byte) primitives.get(index)).byteValue();
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[primitives.size()];
        for (int i = 0; i < primitives.size(); i++)
            bytes[i] = getByte(i);

        return bytes;
    }
}
