package korat.utils.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Simple serializer, using java.io serialization
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class SimpleDeserializer implements IDeserializer {

    ObjectInputStream deserializeStream;

    public SimpleDeserializer(String filename) {

        File f = new File(filename);
        if (!f.exists())
            throw new RuntimeException("Invalid filename <" + filename
                    + "> specified");

        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            deserializeStream = new ObjectInputStream(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Should never happen >>", e);
        } catch (EOFException e) {
            streamClosed = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private boolean streamClosed;

    public Object readObject() {
        Object ret = null;

        if (streamClosed)
            return null;

        try {
            ret = deserializeStream.readObject();

            if (ret == null) {
                deserializeStream.close();
                streamClosed = true;
            }
        } catch (EOFException e) {
            try {
                deserializeStream.close();
                streamClosed = true;
                return null;
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

    @Override
    public void finalize() {
        try {
            deserializeStream.close();
        } catch (IOException e) {
        }
    }

}
