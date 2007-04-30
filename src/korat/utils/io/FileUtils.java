package korat.utils.io;

import java.io.File;

public class FileUtils {

    /**
     * Deletes given folder with all of its content
     * 
     * @param dir
     * @param deleteFolderItself
     */
    public static void deleteFolder(File dir, boolean deleteFolderItself) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                deleteFolder(f, true);
            } else if (f.isFile()) {
                if (!f.delete()) {
                    System.err.println("Cannot delete file "
                            + f.getAbsolutePath());
                }
            }
        }
        if (deleteFolderItself) {
            if (!dir.delete()) {
                System.err.println("Cannot delete folder "
                        + dir.getAbsolutePath());
            }
        }
    }

    /**
     * appends special suffix (number of all files) to all files within given
     * directory
     * 
     * @param dir
     */
    public static void appendSuffix(File dir, int numOfPlaces) {
        File[] files = dir.listFiles();
        if (files == null)
            return;
        String suffix = Integer.toString(files.length);
        int n = numOfPlaces - suffix.length();
        for (int i = 0; i < n; i++) {
            suffix = "0" + suffix;
        }            
        for (File f : files) {
            if (!f.renameTo(new File(f.getAbsolutePath() + suffix))) {
                System.err.println("Cannot rename file!!!");
            }
        }
    }

}
