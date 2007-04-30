package korat.utils;

import junit.framework.TestCase;

class B {
    private int i1;

    public int i2;

    public void dummy() {
        i1++;
    }
}

class D extends B {
    protected int i3;

    public int i4;

    public void dummy() {
        i3++;
    }
}

class DD extends D {
    int i5;

    public int i6;

    public void dummy() {
        i5++;
    }
}

public class ReflectionUtilsTests extends TestCase {

    private DD obj;

    protected void setUp() throws Exception {
        super.setUp();
        obj = new DD();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'korat.utils.ReflectionUtils.getFieldWithAccess(Object,
     * String)'
     */
    public void testGetFieldWithAccess() {
        assertTrue(ReflectionUtils.getFieldWithAccess(obj, "i1").getName().equals(
                "i1"));
        assertTrue(ReflectionUtils.getFieldWithAccess(obj, "i2").getName().equals(
                "i2"));
        assertTrue(ReflectionUtils.getFieldWithAccess(obj, "i3").getName().equals(
                "i3"));
        assertTrue(ReflectionUtils.getFieldWithAccess(obj, "i4").getName().equals(
                "i4"));
        assertTrue(ReflectionUtils.getFieldWithAccess(obj, "i5").getName().equals(
                "i5"));
        assertTrue(ReflectionUtils.getFieldWithAccess(obj, "i6").getName().equals(
                "i6"));
        try {
            ReflectionUtils.getFieldWithAccess(obj, "i7");
            fail();
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        junit.swingui.TestRunner.run(ReflectionUtilsTests.class);
    }

}
