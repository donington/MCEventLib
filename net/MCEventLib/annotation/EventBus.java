package net.MCEventLib.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention(value = RUNTIME)
@Target(value = TYPE)
public @interface EventBus {}
