package skjsjhb.rhytick.opfw.je.dce;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import skjsjhb.rhytick.opfw.je.schedule.Loop;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

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
        vm = createContext();
        id = pid++;
    }

    /**
     * Internal method for creating a context.
     */
    protected static Context createContext() {
        var hab = HostAccess.newBuilder();
        hab.allowAccessAnnotatedBy(Expose.class);
        return Context.newBuilder("js")
                .allowHostAccess(hab.build())
                .allowValueSharing(false)
                .out(System.out)
                .err(System.err)
                .build();
    }

    /**
     * Forward of {@link Context#eval(Source)} without return value.
     */
    public void eval(String src) {
        vm.eval("js", src);
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
     * Get the ID of this env.
     */
    public int getID() {
        return id;
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
     * This method blocks until the VM loop stops. After the VM exits, it's automatically closed.
     */
    public void start() {
        vm.enter();
        System.out.printf("[ScriptEnv #%d Started]\n", id);
        vmLoop.start();
        System.out.printf("[ScriptEnv #%d Stopped]\n", id);
        vm.leave();
        vm.close();
    }

    /**
     * Stop the loop and the engine.
     */
    public void stop() {
        vmLoop.requestStop();
    }
}
