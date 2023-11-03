package skjsjhb.rhytick.opfw.je.dce;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Emulation#registerNatives()} will scan all classes annotated with this symbol and register them automatically
 * for the intepreter.
 * <br/>
 * The class annotated must be public and contains at least one public constructor without any parameters. Otherwise
 * the registration will fail.
 *
 * @apiNote This annotation must be used with extra care. Annotating it to arbitrary classes bring enormous security
 * risks. Although both the launcher and the guest script are checked, it is still possible for malicious code to access
 * the class path and inject packages which are not meant to be loaded. Limit the classes with this annotation will, at
 * least, take the risk down to acceptable level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DCEModule {
    /**
     * If set to {@code true}, the annotated class is registered using {@link ScriptEnv#setGlobal(String, Object)},
     * rather than as a module.
     * <br/>
     * To avoid possible global variable pollution, this should be used with limitations and care.
     */
    boolean asGlobal() default false;

    /**
     * If set to {@code true}, the <b>class</b> of the annotated class is registered, rather than an instance.
     * <br/>
     * The word {@code statik} is not a typo.
     */
    boolean statik() default false;

    /**
     * The module name, or the global name if {@link #asGlobal()} is {@code true}.
     */
    String value();
}
