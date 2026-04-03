package util;

import java.io.*;

/**
 * Handles all file I/O operations using Java Object Serialization.
 * Demonstrates FILE HANDLING — a core Java feature.
 *
 * Data is stored in the /data directory as binary .dat files.
 * Each file holds a serialized HashMap that can be loaded back into memory.
 */
public class FileHandler {

    private static final String DATA_DIR = "data" + File.separator;

    // Static block: runs once when the class is first loaded.
    // Ensures the /data directory always exists before any read/write.
    static {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Serializes any Java object to a file.
     * @param obj      The object to save (usually a HashMap)
     * @param fileName Target file name inside the /data directory
     */
    public static void saveObject(Object obj, String fileName) throws IOException {
        String path = DATA_DIR + fileName;
        // try-with-resources automatically closes the stream — good practice
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {
            oos.writeObject(obj);
        }
    }

    /**
     * Deserializes an object back from a file.
     * @param fileName Source file name inside the /data directory
     * @return The deserialized object, or null if the file doesn't exist
     */
    public static Object loadObject(String fileName) throws IOException, ClassNotFoundException {
        String path = DATA_DIR + fileName;
        File file = new File(path);

        if (!file.exists()) {
            return null; // First run — no data yet
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path)))) {
            return ois.readObject();
        }
    }

    /** Checks whether a particular data file exists. */
    public static boolean fileExists(String fileName) {
        return new File(DATA_DIR + fileName).exists();
    }
}
