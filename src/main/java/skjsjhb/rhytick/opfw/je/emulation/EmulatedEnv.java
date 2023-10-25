package skjsjhb.rhytick.opfw.je.emulation;

import org.graalvm.polyglot.Value;
import skjsjhb.rhytick.opfw.je.dce.Expose;
import skjsjhb.rhytick.opfw.je.dce.JavaScriptEnv;
import skjsjhb.rhytick.opfw.je.dce.NativeBridge;
import skjsjhb.rhytick.opfw.je.finder.Finder;
import skjsjhb.rhytick.opfw.je.schedule.MainLoop;

/**
 * Emulated OPFW runtime with all necessary components.
 */
public class EmulatedEnv {
    protected Finder finder = new Finder();
    protected JavaScriptEnv jsEnv = new JavaScriptEnv(true);
    /**
     * Loop for game logic processing, rendering, etc.
     */
    protected MainLoop mainLoop = new MainLoop();
    /**
     * Loop for JS evaluation.
     */
    protected MainLoop jsLoop = new MainLoop();
    protected NativeBridge jsBridge = new NativeBridge();

    public EmulatedEnv() {
        jsBridge.bindNativeIndex(jsEnv);
        jsBridge.bindInterface("emulation", EmulatedAPI.class, new EmulatedAPI());
    }

    /**
     * Interfaces for interacting with the emulated environment.
     */
    public class EmulatedAPI {
        /**
         * Request a function to be executed on the main loop of JS.
         * <br/>
         * The guest function will be checked and executed on the main loop using
         * {@link skjsjhb.rhytick.opfw.je.schedule.Scheduler}.
         *
         * @param f    Guest function.
         * @param stat Control how the function is executed.
         *             0 - Run forever.
         *             1 - Run once.
         *             Other - Run until.
         */
        @Expose
        public void requestLoop(Value f, int stat) {
            if (!f.canExecute()) {
                return; // Invalid acceess
            }
            if (stat == 0) {
                jsLoop.runOnMainLoopForever(f::execute);
            } else if (stat == 1) {
                jsLoop.runOnMainLoopOnce(f::execute);
            } else {
                jsLoop.runOnMainLoopUntil(() -> {
                    Value ret = f.execute();
                    return ret.asBoolean();
                });
            }
        }

        /**
         * Stop the JS loop.
         */
        @Expose
        public void stopLoop() {
            jsLoop.requestStop();
        }
    }

    /**
     * Start the emulated env. This method blocks until the main loop ends.
     */
    public void start() {
        // TODO start emul
    }
}
