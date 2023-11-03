package skjsjhb.rhytick.opfw.je.dce;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DCE access-grating annotation.
 * <br/>
 * By annotating an interface (public class, method, field) with this annotation, guest code running in
 * {@link ScriptEnv} will be able to use it.
 * <br/>
 *
 * @apiNote Out of security concerns, all accessible interfaces (public constructors, methods, fields) must also
 * be annotated with {@link Expose} to make them accessible in VMs with bindings enabled. Note that this <b>DOES NOT</b>
 * protect the exposed interfaces from being abused. Exposing interfaces with permissions more than necessary
 * will cause <b>SEVERE</b> security issues.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Expose {
}
