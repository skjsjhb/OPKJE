package skjsjhb.rhytick.opfw.je.cfg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OPFW config provider class.
 */
public class Cfg {
    protected static Map<String, String> values = new HashMap<>();

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
     * The overload of {@link #getInt(String, int)} with default value set to 0.
     */
    public static int getInt(String k) {
        return getInt(k, 0);
    }

    /**
     * The overload of {@link #getDouble(String, double)} with default value set to 0.
     */
    public static double getDouble(String k) {
        return getDouble(k, 0);
    }

    /**
     * The overload of {@link #getBoolean(String, boolean)} with default value set to {@code false}.
     */
    public static boolean getBoolean(String k) {
        return getBoolean(k, false);
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
    public static void loadBundledCfg() {
        System.out.println("Loading bundled cfg...");
        try (InputStream def = Thread.currentThread().getContextClassLoader().getResourceAsStream("default.cfg")) {
            if (def != null) {
                String[] result = new BufferedReader(new InputStreamReader(def))
                        .lines().toArray(String[]::new);
                load(result);
            }
        } catch (IOException e) {
            System.out.println("Failed to load bundled cfg file: " + e);
        }
    }
}
