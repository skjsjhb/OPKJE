package skjsjhb.rhytick.opfw.je.finder;

import skjsjhb.rhytick.opfw.je.dce.DCEModule;
import skjsjhb.rhytick.opfw.je.dce.Expose;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Hashtable;
import java.util.Map;

/**
 * Key-value storage for guest script.
 */
@DCEModule(value = "kv", statik = true)
public class KV {
    /**
     * The location of KV data file.
     */
    protected static String KV_FILE_LOCATION = "/osr/kv";

    /**
     * A map which stores key to binary stream of the stored object.
     */
    protected static Map<String, String> kv = new Hashtable<>();

    /**
     * Get the object stored in KV.
     * <br/>
     * The value is loaded from the map, parsed and then returned as an object. If failed, it will return null.
     */
    @Nullable
    @Expose
    @SuppressWarnings("unused")
    public static String get(String k) {
        return kv.get(k);
    }

    /**
     * Load kv file.
     */
    @SuppressWarnings("unchecked")
    public static void load() {
        try {
            File f = new File(Finder.resolve(KV_FILE_LOCATION));
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            kv = (Map<String, String>) ois.readObject();
            ois.close();
            System.out.printf("KV data loaded. (%d entries)\n", kv.size());
        } catch (FileNotFoundException ignored) {
            // This is not considered an error
            System.out.println("KV file does not exist. Skipped loading.");
        } catch (IOException e) {
            System.err.println("Could not load KV from file: " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Could not parse KV data: " + e);
        }
    }

    /**
     * Save kv file.
     */
    public static void save() {
        try {
            Finder.ensureDir(KV_FILE_LOCATION);
            File f = new File(Finder.resolve(KV_FILE_LOCATION));
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(kv);
            oos.close();
            System.out.printf("KV data saved. (%d entries)\n", kv.size());
        } catch (IOException e) {
            System.err.println("Could not save KV: " + e);
        }
    }

    /**
     * Set an object in this KV.
     */
    @Expose
    @SuppressWarnings("unused")
    public static void set(String k, String v) {
        kv.put(k, v);
    }
}
