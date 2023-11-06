package skjsjhb.rhytick.opfw.je.dce;

import skjsjhb.rhytick.opfw.je.launcher.Cfg;

import java.io.IOException;

/**
 * Refers to the Opticia main script.
 */
public class Emulation {
    protected ScriptEnv jsEnv;

    public Emulation() {
        System.out.println("Creating compatible layer for Opticia.");
        jsEnv = new ScriptEnv();
        System.out.println("Engine created: " + jsEnv.getEngineInfo());
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
        Modular.autoRegister();

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
