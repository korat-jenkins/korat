package korat.finitization.impl;

import korat.finitization.IFloatSet;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class FloatSet extends PrimitiveTypeSet implements IFloatSet {

    FloatSet(float min, float diff, float max) {
        super(float.class);
        addRange(min, diff, max);
    }

    FloatSet(float min, float max) {
        this(min, 1.0f, max);
    }

    FloatSet(float value) {
        this(value, 1.0f, value);
    }

    public void addFloat(float f) {
        Float ff = new Float(f);
        primitives.add(ff);
    }

    public void addRange(float min, float diff, float max) {
        if (min > max)
            return;

        float f = min;
        int k = 0;
        while (f < max) {
            primitives.add(f);
            f = min + diff * (++k);
        }
        primitives.add(max);

    }

    public void removeFloat(float f) {
        primitives.remove(f);
    }

    public float getFloat(int index) {
        return (Float) primitives.get(index);
    }

    public float[] getFloats() {
        float[] floats = new float[primitives.size()];
        for (int i = 0; i < primitives.size(); i++)
            floats[i] = getFloat(i);

        return floats;
    }
}
