package net.MCEventLib.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.MCEventLib.EventBus.EventPriority;



// implementors of this event bus should create their own event subscribe method
@Retention(value = RUNTIME)
@Target(value = METHOD)
public @interface EventSubscribe {
	public EventPriority priority() default EventPriority.NORMAL;
	public boolean ignoreCancelled() default false;
}
