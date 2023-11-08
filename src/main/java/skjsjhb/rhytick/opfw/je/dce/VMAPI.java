package skjsjhb.rhytick.opfw.je.dce;

import org.graalvm.polyglot.Value;
import skjsjhb.rhytick.opfw.je.finder.Finder;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * DCE interface for guest script to schedule tasks.
 * This interface is special and is registered internally, without using auto registration.
 * Each emulation context owns its dedicated VMAPI implementation, not shared with other VMs. This dedicated API
 * fully controls the current VM.
 */
public class VMAPI {
    protected ScriptEnv env;

    public VMAPI(ScriptEnv e) {
        env = e;
    }

    /**
     * Gets engine info.
     *
     * @return Engine info returned from {@link ScriptEnv#getEngineInfo()}.
     */
    @Expose
    @SuppressWarnings("unused")
    public String getVMInfo() {
        return env.getEngineInfo();
    }

    /**
     * Request a library to be loaded from file and executued in the environment immediately.
     * <br/>
     * If the verification flag ({@code emulation.no_script_verify}) is not set, then the target script will
     * be verified for security reasons. The guest script cannot skip it.
     *
     * @param name Path to the library file. The file is read using {@link Finder},
     *             so make sure to get paths right.
     */
    @Expose
    @SuppressWarnings("unused")
    public void library(String name) {
        try {
            System.out.println("Guest requesting library: " + name);
            env.eval(Codeload.readScriptSource(name));
        } catch (IOException e) {
            System.err.printf("Failed to load library '%s': %s\n", name, e);
        }
    }

    /**
     * Request a function to be called on the next 'tick'.
     * <br/>
     * Although there is no such concept named 'tick' in our implementation, the 'next tick'
     * usually refers to the next loop of evaluation after the current execution call.
     */
    @Expose
    @SuppressWarnings("unused")
    public void requestLoop(Value f) {
        if (f.canExecute()) {
            env.getLoop().push(f::execute);
        }
    }

    /**
     * Require a defined module.
     *
     * @param name Module name.
     */
    @Expose
    @SuppressWarnings("unused")
    @Nullable
    public Object require(String name) {
        var sme = Modular.getModule(name);
        if (sme == null) {
            return null;
        }
        if (sme.isStatic) {
            return env.makeStatic(sme.instance);
        }
        return sme.instance;
    }

    /**
     * Stop the VM.
     */
    @Expose
    @SuppressWarnings("unused")
    public void stop() {
        env.getLoop().requestStop();
    }
}
