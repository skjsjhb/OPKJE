package skjsjhb.rhytick.opfw.je.dce;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

/**
 * JavaScript environment with optionally OPFW bindings based on GraalVM.
 * Used for main game scripts and charts.
 */
public class JavaScriptEnv {
    /**
     * Internal GraalVM instance.
     */
    protected Context vm;

    /**
     * Create a new JavaScript evaluation environment.
     *
     * @param allowInterfaces Enable native interfaces bindings.
     * @apiNote Even native bindings is enabled, an interface (public constructors, methods, fields) still
     * needs to be annotated by {@link Expose} to become visible to the guest code.
     * @apiNote Native interfaces have a large amount of features provided by the client system. By
     * enabling these features, you trust the code which is going to run on this instance. Untrusted code
     * will have access to low-level APIs and will bring unnecessary risk to the client system.
     */
    public JavaScriptEnv(boolean allowInterfaces) {
        var hab = HostAccess.newBuilder();
        if (allowInterfaces) {
            hab.allowAccessAnnotatedBy(Expose.class);
        }
        vm = Context.newBuilder("js")
                .allowHostAccess(hab.build())
                .allowValueSharing(false)
                .logHandler(System.out)
                .build();
    }

    /**
     * Expose a public interface annotated with {@link Expose}.
     *
     * @param name Global identifier for the instance to bind.
     * @param ctx  Context object to be bound.
     * @apiNote This method might cause security issues. See {@link Expose} for details.
     * @see Expose
     */
    public void setEnv(String name, Object ctx) {
        vm.getBindings("js").putMember(name, ctx);
    }

    /**
     * Proxy method of {@link Context#eval(Source)} with return value ignored.
     *
     * @param src JavaScript source.
     * @return Evaluation result.
     * @apiNote <b>Warning: </b>Executing untrusted code will bring security issues.
     * See {@link JavaScriptEnv#JavaScriptEnv(boolean)} for details.
     * @see JavaScriptEnv#JavaScriptEnv(boolean)
     */
    public Value eval(String src) {
        return vm.eval(Source.create("js", src));
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

}
