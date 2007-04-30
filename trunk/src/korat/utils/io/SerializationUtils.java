package korat.utils.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class SerializationUtils {

    public static Object cloneBySerialization(Object obj) throws IOException,
            ClassNotFoundException {

        Object clone = null;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream ostream = null;
        ObjectInputStream oistream = null;

        try {

            baos = new ByteArrayOutputStream(8192);
            ostream = new ObjectOutputStream(baos);
            ostream.writeObject(obj);
            ostream.flush();
            ostream.reset();

            oistream = new ObjectInputStream(new ByteArrayInputStream(
                    baos.toByteArray()));
            clone = oistream.readObject();

        } finally {

            try {

                if (baos != null)
                    baos.close();
                if (ostream != null)
                    ostream.close();
                if (oistream != null)
                    oistream.close();

            } catch (IOException e) {
            }

        }

        return clone;

    }

    public static Object cloneBySerializationNoExceptions(Object obj) {

        Object retObj = null;

        try {

            retObj = cloneBySerialization(obj);

        } catch (Exception e) {
        }

        return retObj;

    }

}
