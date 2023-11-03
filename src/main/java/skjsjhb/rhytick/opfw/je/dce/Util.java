package skjsjhb.rhytick.opfw.je.dce;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;

/**
 * Utilities implementing missing methods in script platform.
 */
@DCEModule(value = "util", statik = true)
public class Util {
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
     * Wraps a Java array.
     * <br/>
     * The param is claimed to be of type {@link Object}, to be compatible with GraalVM types. However, the
     * object must be of type {@code T[]} during its call and a dynamic cast will happen.
     */
    @Expose
    @SuppressWarnings("unused")
    public static ProxyArray toArray(Object array) {
        if (!array.getClass().isArray()) {
            throw new UnsupportedOperationException("not an array");
        }
        return new ProxyArray() {
            @Override
            public Object get(long index) {
                return Array.get(array, (int) index);
            }

            @Override
            public long getSize() {
                return Array.getLength(array);
            }

            @Override
            public void set(long index, Value value) {
                Array.set(array, (int) index, value.asHostObject());
            }
        };
    }
}
