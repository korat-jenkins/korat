package korat.utils.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class BitInputOutputStreamTest extends TestCase {

    private BitOutputStream bos;
    private ByteArrayOutputStream baos;
    private BitInputStream bis;
    private ByteArrayInputStream bais;
    
    @Override
    protected void setUp() throws Exception {
        baos = new ByteArrayOutputStream();
        bos = new BitOutputStream(baos);
    }
    
    @Override
    protected void tearDown() throws Exception {
        if (bos != null)
            bos.close();
        if (bis != null)
            bis.close();
    }

    private void doTest(int[] ints, int[] bits, int[] expected) throws IOException {
        
        for (int i = 0; i < ints.length; i++) { 
            bos.writeBits(ints[i], bits[i]);
        }
        bos.flush();
        byte[] bs = baos.toByteArray();
        assertEquals(expected.length, bs.length);
        for (int i = 0; i < bs.length; i++) {
            int b = bs[i] & 255;
            assertEquals(expected[i], b);
        }
        
        bais = new ByteArrayInputStream(bs);
        bis = new BitInputStream(bais);
        for (int i = 0; i < ints.length; i++) { 
            int x = bis.readBitsAsInt(bits[i]);
            assertEquals(ints[i], x);
        }
    }
    
    public void test1() throws Exception {
        int[] ints = {5, 1, 13, 7, 57};
        int[] bits = {3, 5,  4, 4 , 8};
        int[] expected = {161, 215, 57};
        doTest(ints, bits, expected);
    }
    
    public void test2() throws Exception {
        int[] ints = {5, 1, 10, 356, 20, 100};
        int[] bits = {3, 2,  5,   9,  5,  32};
        int[] expected = {170, 172, 148, 0, 0, 0, 100};
        doTest(ints, bits, expected);
    }
    
    public void test3() throws Exception {
        int[] ints = {5, 170, 20};
        int[] bits = {3,  32,  5};
        int[] expected = {160, 0, 0, 21, 84};
        doTest(ints, bits, expected);
        bos.writeBits(5, 3);
        bos.writeInt(170);
        bos.writeBits(20, 5);
    }

}
