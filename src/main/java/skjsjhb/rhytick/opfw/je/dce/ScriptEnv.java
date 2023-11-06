package skjsjhb.rhytick.opfw.je.dce;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import skjsjhb.rhytick.opfw.je.finder.Finder;
import skjsjhb.rhytick.opfw.je.launcher.Cfg;
import skjsjhb.rhytick.opfw.je.schedule.Loop;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

/**
 * JavaScript environment with optionally OPFW bindings based on GraalVM.
 * Used for main game scripts and charts.
 * <br/>
 * This class internally holds a {@link Loop} to manage concepts like 'tick' in Node.js and browsers.
 */
public class ScriptEnv {

    /**
     * Internal field for loading bundled script.
     */
    protected static final String BUNDLED_SCRIPT_NAME = "preload.js";

    /**
     * Static field identifier.
     */
    protected static final String STATIC_FIELD_ID = "static";

    /**
     * Internal method for naming different envs.
     */
    protected static int pid = 0;

    /**
     * Internal field to cache all registered modules.
     */
    protected static Map<String, ScriptModuleEntry> sharedModuleMap = new Hashtable<>();


    /**
     * The ID of this env.
     */
    protected int id;

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
    public ScriptEnv() {
        var hab = HostAccess.newBuilder();
        hab.allowAccessAnnotatedBy(Expose.class);
        vm = Context.newBuilder("js")
                .allowHostAccess(hab.build())
                .allowValueSharing(false)
                .out(System.out)
                .err(System.err)
                .build();
        id = pid++;
    }

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
        sharedModuleMap.put(name, new ScriptModuleEntry(ctx, statik));
    }

    /**
     * Get engine info as string.
     *
     * @return Readable info about the engine.
     */
    public String getEngineInfo() {
        return String.format("ScriptEnv #%d (%s, %s)", id,
                vm.getEngine().getImplementationName(), vm.getEngine().getVersion());
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
        setGlobal("VM", new VMAPI(this), false);
    }

    /**
     * Load a bundled script.
     * <br/>
     * Integrity check is skipped for bundled script, since integrity checking for the jar file is enabled by default.
     *
     * @throws IOException If the script stream cannot be read.
     */
    protected void loadBundledScript() throws IOException {
        try (InputStream ist = Thread.currentThread().getContextClassLoader().getResourceAsStream(BUNDLED_SCRIPT_NAME)) {
            if (ist != null) {
                byte[] source = ist.readAllBytes();
                if (source.length == 0) {
                    throw new IOException("empty script source");
                }
                pushScript(new String(source));
            } else {
                throw new IOException("null resource stream");
            }
        }
    }

    /**
     * Load a script from file and push it into the vm script queue.
     *
     * @param name Script name.
     * @throws IOException If the script file could not be read, or the integrity check failed.
     */
    protected void loadScriptAsync(String name) throws IOException {
        pushScript(readScriptSource(name));
    }

    /**
     * Load a script from file and execute it immediately, regardless of the loop status.
     * <br/>
     * This method is blocking and the script being loaded, unless absolutely necessary, should consider
     * {@link #loadScriptAsync(String)} instead.
     *
     * @param name Script name.
     * @throws IOException If the script file could not be read, or the integrity check failed.
     */
    protected void loadScriptImmediate(String name) throws IOException {
        vm.eval(Source.create("js", readScriptSource(name)));
    }

    /**
     * Convert and extract the static field of the specified object.
     *
     * @param o Interface object.
     * @return Converted value, or {@code null} if invalid.
     */
    @Nullable
    protected Value makeStatic(Object o) {
        Value val = vm.asValue(o);
        if (!val.getMemberKeys().contains(STATIC_FIELD_ID)) {
            System.err.println("Attempting to expose a non-static interface as static.");
            return null;
        }
        return val.getMember(STATIC_FIELD_ID);
    }

    /**
     * Push a script to be evaluated on the end of current loop. The script source is created immediately, but its
     * execution happens in the next loop.
     * <br/>
     * This method can be called from any thread, but only after the vm starts.
     *
     * @param src Script source.
     * @apiNote <b>Warning: </b>Executing untrusted code will bring security issues.
     * See {@link ScriptEnv#ScriptEnv()} for details.
     */
    public void pushScript(String src) {
        if (vmLoop == null) {
            throw new IllegalStateException("pushing script before vm loop starts");
        }
        Source s = Source.create("js", src);
        vmLoop.push(() -> vm.eval(s));
    }

    /**
     * Reads a script source from file.
     * <br/>
     * Scripts of specific name can be found at <pre>/opt/(name).js</pre>. The script file is read, and verified
     * together with <pre>(name).js.sig</pre>. The signature might be optional for other files, but for scripts,
     * they are necessary, unless flag <pre>emulation.no_script_verify</pre> is set, which is for dev purpose only.
     *
     * @param name Script virtual path.
     * @return The source code of the script.
     * @throws IOException If I/O errors occurred.
     */
    protected String readScriptSource(String name) throws IOException {
        boolean noVerify = Cfg.getBoolean("emulation.no_script_verify", false);
        if (noVerify) {
            System.out.println("Script integrity check has been disabled. The usage of this flag should be limited within" +
                    " development only.");
        }
        byte[] buf = Finder.readFileBytes(name, !noVerify);
        if (buf.length == 0) {
            throw new IOException("empty script source");
        }
        return new String(buf);
    }

    /**
     * Expose a public interface annotated with {@link Expose}.
     *
     * @param name   Global identifier for the instance to bind.
     * @param ctx    Context object to be bound.
     * @param statik Whether to destruct the object and extract the static field.
     * @apiNote This method might cause security issues. See {@link Expose} for details.
     * @see Expose
     */
    public void setGlobal(String name, Object ctx, boolean statik) {
        vm.getBindings("js").putMember(name, statik ? makeStatic(ctx) : ctx);
    }

    /**
     * Start the engine on current thread.
     * <br/>
     * This method blocks until the VM loop stops.
     */
    public void start() {
        System.out.printf("[ScriptEnv #%d Started]\n", id);
        vmLoop.start();
    }

    /**
     * Stop the loop and the engine.
     */
    public void stop() {
        System.out.printf("[ScriptEnv #%d Stopped]\n", id);
        vmLoop.requestStop();
        vm.close();
    }

    protected static class ScriptModuleEntry {
        Object instance; // Module unique instance

        boolean isStatic;

        ScriptModuleEntry(Object aInst, boolean aStatic) {
            instance = aInst;
            isStatic = aStatic;
        }
    }

    /**
     * DCE interface for guest script to schedule tasks.
     * This interface is special and is registered internally, without using auto registration.
     */
    public static class VMAPI {
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
                env.loadScriptImmediate(name);
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
            ScriptModuleEntry sme = sharedModuleMap.get(name);
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
         * <br/>
         * This will stop the VM immediately, without waiting for resting tasks.
         */
        @Expose
        @SuppressWarnings("unused")
        public void stop() {
            env.stop();
        }
    }
}
