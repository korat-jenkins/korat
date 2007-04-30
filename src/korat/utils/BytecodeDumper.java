package korat.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class BytecodeDumper {

    private static BytecodeDumper instance;

    public static BytecodeDumper getInstance() {
        if (instance == null) {
            instance = new BytecodeDumper();
        }
        return instance;
    }

    protected Logger logger;

    private String rootDir = "bytecodes";

    private String classesDir = "classes";

    public String getRootDir() {
        return rootDir;
    }

    public String getClassesDir() {
        return classesDir;
    }

    public BytecodeDumper() {
        logger = Logger.getLogger(getClass().getName());
        logger.setLevel(Level.OFF);
        cleanRootDir();
    }

    public void dump(byte[] bytes, String fileName, boolean splitIntoDirectories)
            throws IOException {
        if (splitIntoDirectories) {
            String[] dirs = fileName.split("\\.");
            fileName = "";
            for (String s : dirs) {
                fileName = fileName + File.separator + s;
            }
        }

        String classFileName = getRootDir() + File.separator + getClassesDir()
                + fileName + ".class";

        OutputStream os = null;
        try {
            makeDirs(classFileName);
            os = new FileOutputStream(classFileName);
            os.write(bytes);
            logger.info("Bytecodes dumped to file: " + classFileName);
        } finally {
            if (os != null)
                os.close();
        }
    }

    private void cleanRootDir() {
        File f = new File(rootDir);
        clean(f);
    }

    private void clean(File f) {
        if (f == null)
            return;

        if (f.isFile()) {
            boolean b = f.delete();
            if (b)
                logger.info("Deleted file: " + f.getAbsolutePath());
            else
                logger.warning("Cannot delete file: " + f.getAbsolutePath());
            return;
        }

        if (f.isDirectory()) {
            for (String path : f.list()) {
                File child = new File(f.getAbsolutePath() + File.separator
                        + path);
                clean(child);
            }
            boolean b = f.delete();
            if (b)
                logger.info("Deleted directory: " + f.getAbsolutePath());
            else
                logger.warning("Cannot delete directory: "
                        + f.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        getInstance().cleanRootDir();
    }

    private void makeDirs(String fileName) {
        File f = new File(fileName.substring(0,
                fileName.lastIndexOf(File.separatorChar)));
        f.mkdirs();
    }

    public void dumpAndEatExceptions(byte[] bytes, String fileName,
            boolean splitIntoDirectories) {
        try {
            dump(bytes, fileName, splitIntoDirectories);
        } catch (IOException e) {
            logger.warning("Exception ate: " + e.getMessage());
        }
    }

}
