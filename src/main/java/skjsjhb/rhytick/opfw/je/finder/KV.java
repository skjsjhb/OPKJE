package skjsjhb.rhytick.opfw.je.finder;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Hashtable;
import java.util.Map;

/**
 * Key-value storage for guest script.
 */
public class KV {
    /**
     * The location of KV data file.
     */
    protected static String KV_FILE_LOCATION = "/osr/kv";
    /**
     * A map which stores key to binary stream of the stored object.
     */
    protected Map<String, String> kv = new Hashtable<>();

    /**
     * Get the object stored in KV.
     * <br/>
     * The value is loaded from the map, parsed and then returned as an object. If failed, it will return null.
     */
    @Nullable
    public Object get(String k) {
        if (!kv.containsKey(k)) {
            return null;
        }
        try {
            String src = kv.get(k);
            ByteArrayInputStream bi = new ByteArrayInputStream(src.getBytes());
            ObjectInputStream oi = new ObjectInputStream(bi);
            return oi.readObject();
        } catch (IOException e) {
            System.err.println("Could not read object: " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Corresponding class not found: " + e);
        }
        return null;
    }

    /**
     * Load kv file.
     */
    @SuppressWarnings("unchecked")
    public void load() {
        try {
            File f = new File(KV_FILE_LOCATION);
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            kv = (Map<String, String>) ois.readObject();
            ois.close();
            System.out.println("KV data loaded.");
        } catch (IOException e) {
            System.err.println("Could not load KV from file: " + e);
        } catch (ClassNotFoundException e) {
            System.err.println("Could not parse KV data: " + e);
        }
    }

    /**
     * Save kv file.
     */
    public void save() {
        try {
            String saveTarget = "/osr/kv";
            Finder.ensureDir(saveTarget);
            File f = new File(saveTarget);
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(kv);
            oos.close();
            System.out.println("KV data saved.");
        } catch (IOException e) {
            System.err.println("Could not save KV: " + e);
        }
    }

    /**
     * Set an object in this KV.
     */
    public void set(String k, Object v) {
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            ObjectOutputStream objOutput = new ObjectOutputStream(ba);
            objOutput.writeObject(v);
            kv.put(k, ba.toString());
        } catch (IOException e) {
            System.err.println("Could not write object: " + e);
        }
    }
}