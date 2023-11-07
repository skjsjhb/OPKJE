package skjsjhb.rhytick.opfw.je.dce;

import java.io.IOException;

/**
 * Refers to the Opticia main script.
 * <br/>
 * The Emulation contains information for monitoring a running script env and syncing states.
 */
public class Emulation {
    protected ScriptEnv jsEnv;

    public Emulation() {
        jsEnv = new ScriptEnv();
        System.out.println("Engine created: " + jsEnv.getEngineInfo());
    }

    /**
     * Get the underlying script env.
     */
    public ScriptEnv getEnv() {
        return jsEnv;
    }

    /**
     * Prepare for the run of the code.
     */
    public void prepareRun() {
        jsEnv.initVMAPI();
        try {
            jsEnv.loadBundledScript();
        } catch (IOException e) {
            System.err.println("Could not load preload script: " + e);
            System.err.println("This will likely to cause subsequent errors.");
        }
    }

    /**
     * Start the emulation process on this thread with specified source.
     * <br/>
     * This method blocks and return when requested or the VM stops.
     */
    public void start(String src) {
        jsEnv.pushScript(src);
        jsEnv.start();
    }

    /**
     * Start the emulation process on a new thread with specified source.
     * <br/>
     * This method returns immediately.
     */
    public void startAsync(String src) {
        new Thread(() -> {
            start(src);
        }).start();
    }
}
