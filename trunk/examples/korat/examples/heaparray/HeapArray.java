package korat.examples.heaparray;

import korat.finitization.IArraySet;
import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.impl.FinitizationFactory;

public class HeapArray {

    private int size;

    private int array[];

    public String toString() {
        String s = "";
        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                s = s + array[i] + " ";
            }
            s += ", size = " + size + ", array.length = " + array.length;
        }
        return s;
    }


    public boolean repOK() {

        if (array == null)
            return false;

        if (size < 0 || size > array.length)
            return false;

        for (int i = 0; i < size; i++) {
            int elem_i = array[i];
            if (elem_i == -1)
                return false;

            if (i > 0) {
                int elem_parent = array[(i - 1) / 2];
                if (elem_i > elem_parent)
                    return false;
            }
        }

        for (int i = size; i < array.length; i++)
            if (array[i] != -1)
                return false;

        return true;

    }

    public static IFinitization finHeapArray(int n) {
        return finHeapArray(n, n, n);
    }
    
    public static IFinitization finHeapArray(int maxSize, int maxArrayLength,
            int maxArrayValue) {

        IFinitization f = FinitizationFactory.create(HeapArray.class);

        IIntSet sizes = f.createIntSet(0, 1, maxSize);

        IIntSet arrayLength = f.createIntSet(0, 1, maxArrayLength);
        IIntSet arrayValues = f.createIntSet(-1, 1, maxArrayValue);
        IArraySet arrays = f.createArraySet(int[].class, arrayLength, arrayValues, 1);

        f.set("size", sizes);
        f.set("array", arrays);

        return f;
    }

}
