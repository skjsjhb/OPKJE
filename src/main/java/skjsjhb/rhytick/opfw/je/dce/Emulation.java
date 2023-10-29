package skjsjhb.rhytick.opfw.je.dce;

import skjsjhb.rhytick.opfw.je.cfg.Cfg;
import skjsjhb.rhytick.opfw.je.finder.Finder;

import java.io.IOException;

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
     * Load a script installed with Opticia and push it into the vm script queue.
     * <br/>
     * Scripts of specific name can be found at <pre>/opt/(name).js</pre>. The script file is read, and verified
     * together with <pre>(name).js.sig</pre>. The signature might be optional for other files, but for scripts,
     * they are necessary, unless flag <pre>emulation.no_script_verify</pre> is set, which is for dev purpose only.
     *
     * @param name Script name.
     * @throws IOException If the script file could not be read, or the integrity check failed.
     */
    protected void loadScript(String name) throws IOException {
        boolean noVerify = Cfg.getBoolean("emulation.no_script_verify", false);
        if (noVerify) {
            System.out.println("Script integrity check has been disabled. The usage of this flag should be limited within" +
                    " development only.");
        }
        byte[] buf = Finder.readFileBytes("/opt/" + name + ".js", !noVerify);
        if (buf.length == 0) {
            throw new IOException("empty script source");
        }
        jsEnv.pushScript(new String(buf));
    }

    /**
     * Start the emulation thread.
     * <br/>
     * This method blocks and return when requested or the VM stops.
     */
    public void start() {
        // Load necessary scripts
        System.out.println("Loading scripts.");
        String entry = Cfg.getValue("emulation.entry", "main");
        String preload = Cfg.getValue("emulation.preload", "preload");
        try {
            System.out.println("Loading preload: " + preload);
            loadScript(preload);
            System.out.println("Loading entry: " + entry);
            loadScript(entry);
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
