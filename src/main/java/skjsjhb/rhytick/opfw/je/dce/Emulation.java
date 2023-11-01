package skjsjhb.rhytick.opfw.je.dce;

import com.google.common.reflect.ClassPath;
import skjsjhb.rhytick.opfw.je.cfg.Cfg;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Refers to the Opticia main script.
 */
public class Emulation {
    protected JavaScriptEnv jsEnv;

    public Emulation() {
        System.out.println("Creating compatible layer for Opticia.");
        jsEnv = new JavaScriptEnv();
        System.out.println("Engine created: " + jsEnv.getEngineInfo());
    }

    /**
     * Register native interfaces using reflection.
     * <br/>
     * All public classes annotated with
     */
    protected void registerNatives() {
        try {
            var classInfos = ClassPath
                    .from(getClass().getClassLoader())
                    .getTopLevelClassesRecursive("skjsjhb.rhytick.opfw.je");

            for (var cls : classInfos) {
                var clazz = cls.load();
                if (!clazz.isAnnotationPresent(DCEModule.class)) {
                    continue; // Not the desired class
                }
                try {
                    var cons = clazz.getConstructor();
                    DCEModule m = clazz.getAnnotation(DCEModule.class);
                    String name = m.value();
                    Object inst = cons.newInstance();
                    if (m.asGlobal()) {
                        jsEnv.setGlobal(name, inst);
                    } else {
                        jsEnv.setModule(name, inst);
                    }
                    System.out.println("Emulation native interface registered: " + cls.getName());
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
     * Start the emulation thread.
     * <br/>
     * The natives are registered, then preload and main script entries are loaded. If all above wents fine, the VM
     * is started.
     * <br/>
     * The preload script is a 'glue', or say, compatibility provider to implement OPFW specification which are not
     * (or not easy to be) provided by OPKJE itself. It's loaded from the resources directory. The main script is the
     * Opticia executable and is stored at the data directory.
     * <br/>
     * This method blocks and return when requested or the VM stops.
     */
    public void start() {
        // Enable natives
        registerNatives();

        // Load necessary scripts
        System.out.println("Loading scripts.");
        String entry = Cfg.getValue("emulation.entry", "main");

        // Init VM
        jsEnv.initVMAPI();

        // Load scripts
        try {
            System.out.println("Loading bundled preload script.");
            jsEnv.loadBundledScript();
            System.out.println("Loading entry: " + entry);
            jsEnv.loadScriptAsync(entry);
        } catch (IOException e) {
            throw new EmulationVMException("failed to load vm scripts", e);
        }

        // Start the vm loop
        jsEnv.start();
    }

    public static class EmulationVMException extends RuntimeException {
        public EmulationVMException(String what) {
            super(what);
        }

        public EmulationVMException(String what, Throwable cause) {
            super(what, cause);
        }
    }
}
