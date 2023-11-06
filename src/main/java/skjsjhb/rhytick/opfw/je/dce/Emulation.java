package skjsjhb.rhytick.opfw.je.dce;

import java.io.IOException;

/**
 * Refers to the Opticia main script.
 */
public class Emulation {
    protected ScriptEnv jsEnv;

    public Emulation() {
        jsEnv = new ScriptEnv();
        System.out.println("Engine created: " + jsEnv.getEngineInfo());
    }

    /**
     * Get the ID of the underlying env id.
     */
    public int getEnvID() {
        return jsEnv.getID();
    }


    /**
     * Prepare for the run of the code.
     */
    public void prepareRun() {
        try {
            jsEnv.initVMAPI();
            jsEnv.loadBundledScript();
        } catch (IOException e) {
            throwVMException(e);
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

    protected void throwVMException(Throwable e) {
        throw new EmulationVMException("failed to load vm scripts", e);
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
