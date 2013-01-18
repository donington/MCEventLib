package net.MCEventLib.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.MCEventLib.EventBus.EventPriority;


/** <b>Usage:</b>
 * <pre>
 * @Retention(value = RUNTIME)
 * @Target(value = METHOD)
 * @EventSubscribe
 * public @interface MyEventTypeSubscribe {
 *   public EventPriority priority() default EventPriority.NORMAL;
 *   public boolean ignoreCancelled() default false;
 * }
 * </pre>
 */
@Retention(value = RUNTIME)
@Target(value = ANNOTATION_TYPE)
public @interface EventSubscribe {
	EventPriority priority() default EventPriority.NORMAL;
	boolean ignoreCancel() default false;
}
