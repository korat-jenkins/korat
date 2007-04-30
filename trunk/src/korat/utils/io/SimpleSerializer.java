package korat.utils.io;

import java.io.IOException;

/**
 * Simple serializer, using java.io serialization
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class SimpleSerializer implements ISerializer {

    String filename;

    private java.io.ObjectOutputStream serializeStream;

    private String testsName;

    public SimpleSerializer(String testsName) {
        this.testsName = testsName;
        initialize();
    }

    protected void initialize() {
        serializeStream = null;
        try {
            if (testsName == null || testsName.equals("")) {
                serializeStream = new java.io.ObjectOutputStream(
                        new java.io.BufferedOutputStream(System.out));
            } else {
                serializeStream = new java.io.ObjectOutputStream(
                        new java.io.BufferedOutputStream(
                                new java.io.FileOutputStream(testsName), 8192));
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(">> Cannot open serialization file.");
        }
    }

    public void serialize(Object toSerialize) {
        try {
            serializeStream.writeObject(toSerialize);
            serializeStream.flush();
            serializeStream.reset();
        } catch (java.io.IOException e) {
            throw new RuntimeException(">> Cannot serialize objects.", e);
        }
    }

    @Override
    public void finalize() {
        try {
            serializeStream.close();
        } catch (IOException e) {
        }
    }

}
