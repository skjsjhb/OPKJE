package skjsjhb.rhytick.opfw.je.cfg;

import skjsjhb.rhytick.opfw.je.finder.Finder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OPFW config provider class.
 */
public class Cfg {

    protected static final String BUNDLED_CFG_NAME = "default.cfg";
    protected static Map<String, String> values = new HashMap<>();

    /**
     * The overload of {@link #getBoolean(String, boolean)} with default value set to {@code false}.
     */
    public static boolean getBoolean(String k) {
        return getBoolean(k, false);
    }

    /**
     * Convert the result of {@link #getValue(String, String)} to {@code boolean}.
     */
    public static boolean getBoolean(String k, boolean dv) {
        var s = getValue(k, "");
        if (s.equals("+")) {
            return true;
        }
        if (s.equals("-")) {
            return false;
        }
        return dv;
    }

    /**
     * Convert the result of {@link #getValue(String, String)} to {@code double}.
     */
    public static double getDouble(String k, double dv) {
        var s = getValue(k, "");
        if (s.isEmpty()) {
            return dv;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ignored) {
            return dv;
        }
    }

    /**
     * The overload of {@link #getDouble(String, double)} with default value set to 0.
     */
    public static double getDouble(String k) {
        return getDouble(k, 0);
    }

    /**
     * The overload of {@link #getInt(String, int)} with default value set to 0.
     */
    public static int getInt(String k) {
        return getInt(k, 0);
    }

    /**
     * Convert the result of {@link #getValue(String, String)} to {@code int}.
     */
    public static int getInt(String k, int dv) {
        var s = getValue(k, "");
        if (s.isEmpty()) {
            return dv;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
            return dv;
        }
    }

    /**
     * Override of {@link #getValue(String, String)} with default value set to empty.
     */
    public static String getValue(String k) {
        return getValue(k, "");
    }

    /**
     * Gets the value using specified key. The default value is returned if the key is not set.
     *
     * @param k  Cfg key.
     * @param dv Default value.
     * @return Corresponding value, or {@code dv} if not exist.
     */
    public static String getValue(String k, String dv) {
        return Objects.requireNonNullElse(values.get(k), dv);
    }

    /**
     * Load cfg from specified source.
     *
     * @param sources Cfg source split as lines.
     */
    public static void load(String[] sources) {
        int counter = 0;
        for (var s : sources) {
            if (s.startsWith("#")) {
                continue;
            }
            if (s.startsWith("+")) {
                values.put(s.substring(1), "+");
            } else if (s.startsWith("-")) {
                values.put(s.substring(1), "-");
            } else {
                var kv = s.split(" ", 2);
                if (kv.length != 2) {
                    continue; // Invalid line
                }
                values.put(kv[0], kv[1]);
            }
            counter++;
        }
        System.out.println("Loaded " + counter + " cfg entries.");
    }

    /**
     * Load the default cfg file bundled in the jar.
     */
    public static void loadBundledCfg() throws IOException {
        System.out.println("Loading bundled cfg.");
        try (InputStream def = Thread.currentThread().getContextClassLoader().getResourceAsStream(BUNDLED_CFG_NAME)) {
            if (def != null) {
                String[] result = new BufferedReader(new InputStreamReader(def))
                        .lines().toArray(String[]::new);
                load(result);
            }
        }
    }

    /**
     * Load the user defined cfg file.
     * <br/>
     * User cfg locates at <pre>/osr/user.cfg</pre>
     */
    public static void loadUserCfg() {
        try {
            System.out.println("Loading user cfg.");
            File f = new File(Finder.resolve("/osr/user.cfg"));
            FileInputStream usr = new FileInputStream(f);
            String[] result = new BufferedReader(new InputStreamReader(usr))
                    .lines().toArray(String[]::new);
            load(result);
        } catch (FileNotFoundException ignored) {
            System.out.println("User cfg file does not exist. Skipped.");
        } catch (UncheckedIOException e) {
            System.err.println("Failed to load user cfg file: " + e);
        }
    }
}
