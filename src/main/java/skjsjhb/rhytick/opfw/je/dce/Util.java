package skjsjhb.rhytick.opfw.je.dce;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;

/**
 * Utilities implementing missing methods in script platform.
 */
@GuestModule(value = "util", statik = true)
@SuppressWarnings("unused")
public final class Util {
    /**
     * Decode {@code byte[]} to {@link String}.
     * <br/>
     * The content is assumed to be of charset {@code UTF-8}.
     */
    @Expose
    @SuppressWarnings("unused")
    public static String decodeString(byte[] b) {
        return new String(b, StandardCharsets.UTF_8); // Scripts require UTF-8
    }

    /**
     * Encode a {@link String} to {@code byte[]}.
     * <br/>
     * The string is assumed to be of charset {@code UTF-8}.
     */
    @Expose
    @SuppressWarnings("unused")
    public static byte[] encodeString(String a) {
        return a.getBytes(StandardCharsets.UTF_8); // Generate bytes
    }

    /**
     * Sleep current thread.
     * <br/>
     * This method entirely blocks current thread, preventing any tasks in event queue from being processed.
     * Unless for testing or certain purpose, this method should not be called.
     */
    @Expose
    @SuppressWarnings("unused")
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {

        }
    }

    /**
     * Wraps a Java array.
     * <br/>
     * The param is claimed to be of type {@link Object}, to be compatible with GraalVM types. However, the
     * object must be of type {@code T[]} during its call and a dynamic cast will happen.
     * <br/>
     * The param {@code array} should never be shared since {@code T[]} is not thread-safe. Multiple generated
     * {@link ProxyArray} have access to the array simultaneously regardless of it's caller. This will bring
     * undefined behavior.
     */
    @Expose
    @SuppressWarnings("unused")
    public static ProxyArray toArray(Object array) {
        if (!array.getClass().isArray()) {
            // Create no-op array
            return new ProxyArray() {
                @Override
                public Object get(long index) {
                    return null;
                }

                @Override
                public long getSize() {
                    return 0;
                }

                @Override
                public void set(long index, Value value) {
                }
            };
        }
        // Impl an array
        return new ProxyArray() {
            @Override
            public Object get(long index) {
                if (index >= Array.getLength(array)) {
                    return null; // No value is returned
                }
                return Array.get(array, (int) index);
            }

            @Override
            public long getSize() {
                return Array.getLength(array);
            }

            @Override
            public void set(long index, Value value) {
                if (index >= Array.getLength(array)) {
                    return; // Fail silently
                }
                Array.set(array, (int) index, value.asHostObject());
            }
        };
    }
}
