package skjsjhb.rhytick.opfw.je.dce;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation proxy for JS APIs.
 */
public final class NativeBridge {
    /**
     * Indexed interface -> interface provider map.
     */
    private final Map<Class<?>, Object> interfaceProvider = new HashMap<>();

    /**
     * Indexed name -> interface map.
     */
    private final Map<String, Class<?>> interfaceNames = new HashMap<>();

    public class NativeProvider {
        /**
         * Native interface resolution method.
         *
         * @param name Interface name.
         * @return Registered interface, or {@code null} if not defined.
         */
        @Expose
        public Object require(String name) {
            var api = interfaceNames.get(name);
            if (api != null) {
                var obj = interfaceProvider.get(api);
                if (obj != null) {
                    System.out.println("JS native interface '" + name + "' applied.");
                    return obj;
                }
            }
            System.out.println("Could not find native interface named '" + name + "'");
            return null;
        }
    }

    /**
     * Bind a root op object for the specified env named {@code OPFW}.
     * <br/>
     * The root function is capable for loading native interfaces specified in {@link #interfaceNames}. Note
     * that if the specified {@code env} does not have native bindings enabled, this method will not work.
     *
     * @param env JS interpreter env.
     */
    public void bindNativeIndex(JavaScriptEnv env) {
        env.setEnv("OPFW", new NativeProvider());
    }

    /**
     * Bind a interface for later lookups.
     *
     * @param name     Interface name.
     * @param api      Interface class.
     * @param provider Provider instance implementing the interface.
     * @apiNote In theory, the {@code provider} does not necessarily derived from {@code api}. However, for
     * simplicity we assume (and thus require) that {@code provider} is a subclass of {@code api}.
     */
    public void bindInterface(String name, Class<?> api, Object provider) {
        interfaceNames.put(name, api);
        interfaceProvider.put(api, provider);
        System.out.println("Registered interface '" + name + "' -> " + api.getName());
    }
}
