package skjsjhb.rhytick.opfw.je.dce;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import skjsjhb.rhytick.opfw.je.schedule.Loop;

import javax.annotation.Nullable;
import java.util.Hashtable;
import java.util.Map;

/**
 * JavaScript environment with optionally OPFW bindings based on GraalVM.
 * Used for main game scripts and charts.
 * <br/>
 * This class internally holds a {@link Loop} to manage concepts like 'tick' in Node.js and browsers.
 */
public class JavaScriptEnv {
    protected Map<String, Object> moduleMap = new Hashtable<>();
    /**
     * Internal GraalVM instance.
     */
    protected Context vm;
    protected Loop vmLoop = new Loop();

    /**
     * Create a new JavaScript evaluation environment.
     *
     * @apiNote Even native bindings is enabled, an interface (public constructors, methods, fields) still
     * needs to be annotated by {@link Expose} to become visible to the guest code.
     * @apiNote Native interfaces have a large amount of features provided by the client system. By
     * enabling these features, you trust the code which is going to run on this instance. Untrusted code
     * will have access to low-level APIs and will bring unnecessary risk to the client system.
     */
    public JavaScriptEnv() {
        var hab = HostAccess.newBuilder();
        hab.allowAccessAnnotatedBy(Expose.class);
        vm = Context.newBuilder("js")
                .allowHostAccess(hab.build())
                .allowValueSharing(false)
                .logHandler(System.out)
                .build();
    }

    /**
     * Get engine info as string.
     *
     * @return Readable info about the engine.
     */
    public String getEngineInfo() {
        return "JavaScriptEnv ("
                + vm.getEngine().getImplementationName() + ", "
                + vm.getEngine().getVersion() + ")";
    }

    /**
     * Gets the internal loop object.
     */
    protected Loop getLoop() {
        return vmLoop;
    }

    /**
     * Initialize basic built-in VM controlling APIs for guest script.
     */
    public void initVMAPI() {
        setGlobal("VM", new VMAPI(this));
    }

    /**
     * Push a script to be evaluated on the end of current loop.
     * <br/>
     * This method can be called from any thread, but only after the vm starts.
     *
     * @param src JavaScript source.
     * @apiNote <b>Warning: </b>Executing untrusted code will bring security issues.
     * See {@link JavaScriptEnv#JavaScriptEnv()} for details.
     */
    public void pushScript(String src) {
        if (vmLoop == null) {
            throw new IllegalStateException("pushing script before vm loop starts");
        }
        vmLoop.push(() -> vm.eval(Source.create("js", src)));
    }

    /**
     * Expose a public interface annotated with {@link Expose}.
     *
     * @param name Global identifier for the instance to bind.
     * @param ctx  Context object to be bound.
     * @apiNote This method might cause security issues. See {@link Expose} for details.
     * @see Expose
     */
    public void setGlobal(String name, Object ctx) {
        vm.getBindings("js").putMember(name, ctx);
    }

    /**
     * Sets a module with indexed key.
     * <br/>
     * Unlike globals, modules are not loaded or injected by default. Guest script uses {@code module()} to
     * get the module instance.
     *
     * @param name Module name.
     * @param ctx  Module implementation object.
     */
    public void setModule(String name, Object ctx) {
        moduleMap.put(name, ctx);
    }

    /**
     * Start the JS engine on current thread.
     * <br/>
     * This method blocks until the VM loop stops.
     */
    public void start() {
        vmLoop.start();
    }

    /**
     * DCE interface for guest script to schedule tasks.
     */
    public static class VMAPI {
        protected JavaScriptEnv env;

        public VMAPI(JavaScriptEnv e) {
            env = e;
        }

        /**
         * Request a function to be called on the next 'tick'.
         * <br/>
         * Although there is no such concept named 'tick' in our implementation, the 'next tick'
         * usually refers to the next call after the current execution call.
         * <br/>
         * TS signature:
         * <pre>requestLoop(f:()=>any):void</pre>
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
         * <br/>
         * TS signature:
         * <pre>require(name:string):any</pre>
         *
         * @param name Module name.
         */
        @Expose
        @SuppressWarnings("unused")
        @Nullable
        public Object require(String name) {
            return env.moduleMap.get(name);
        }

        /**
         * Stop the VM.
         * <br/>
         * This will stop the VM immediately, without waiting for resting tasks.
         * <br/>
         * TS signature:
         * <pre>stop():void</pre>
         */
        @Expose
        @SuppressWarnings("unused")
        public void stop() {
            env.getLoop().requestStop();
        }
    }
}
