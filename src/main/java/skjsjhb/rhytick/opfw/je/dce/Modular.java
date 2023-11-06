package skjsjhb.rhytick.opfw.je.dce;

import com.google.common.reflect.ClassPath;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Module manager for guest scripts.
 */
public final class Modular {

    /**
     * Internal field to cache all registered modules.
     */
    private static final Map<String, ModuleEntry> MODULE_MAP = new Hashtable<>();

    /**
     * Registers a module with indexed key.
     * <br/>
     * Unlike globals, modules are not loaded or injected by default. Guest script uses {@code VM.require} to
     * get the module instance. Modules are statically registered and can be shared between contexts.
     * <br/>
     * When registering with {@link GuestModule}, modules must be thread-safe. Otherwise exceptions will happen
     * when being accessed from multiple threads.
     *
     * @param name Module name.
     * @param ctx  Module implementation object.
     */
    public static void addModule(String name, Object ctx, boolean statik) {
        MODULE_MAP.put(name, new ModuleEntry(ctx, statik));
    }

    /**
     * Register native interfaces using reflection.
     * <br/>
     * All public classes annotated with {@link GuestModule} will be registered as a module / global in this emulation
     * session.
     * <br/>
     * Corresponding type definitions are in {@code src/main/types/OPKJE.d.ts}.
     */
    public static void autoRegister() {
        System.out.println("Enabling native bindings.");
        try {
            var classInfos = ClassPath
                    .from(Thread.currentThread().getContextClassLoader())
                    .getTopLevelClassesRecursive("skjsjhb.rhytick.opfw.je");

            for (var cls : classInfos) {
                var clazz = cls.load();
                if (!clazz.isAnnotationPresent(GuestModule.class)) {
                    continue; // Not the desired class
                }
                try {
                    var cons = clazz.getConstructor();
                    GuestModule m = clazz.getAnnotation(GuestModule.class);
                    String name = m.value();
                    Object inst;
                    if (m.statik()) {
                        inst = clazz; // Register a static one
                    } else {
                        inst = cons.newInstance();
                    }
                    addModule(name, inst, m.statik());
                    System.out.println("Binding: " + cls.getName() + (m.statik() ? " [STATIC]" : " [INSTANCE]"));
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    System.err.println("Could not instantiate " + cls.getName() + ": " + e);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not enumerate classes: " + e);
        }
    }

    /**
     * Get specified module.
     *
     * @param name Module name.
     * @return A {@link ModuleEntry} containing the registration info, or `null` if not found.
     */
    @Nullable
    public static ModuleEntry getModule(String name) {
        return MODULE_MAP.get(name);
    }

    public static class ModuleEntry {
        Object instance; // Module unique instance

        boolean isStatic;

        ModuleEntry(Object aInst, boolean aStatic) {
            instance = aInst;
            isStatic = aStatic;
        }
    }
}
