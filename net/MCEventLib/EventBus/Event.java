package net.MCEventLib.EventBus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import net.MCEventLib.annotation.Cancellable;
import net.MCEventLib.annotation.HasResult;


import donington.BukkitEventPort.ASMEventHandler;
import donington.BukkitEventPort.BukkitEventPort;


public abstract class Event {
	// events without a proper parent event will fall back to this
	protected static ListenerList listenerList = null;

	private final short busID;

	private final boolean isCancellable;
	private final boolean hasResult;  // cancellable events always have a result
	private final boolean ignoreCancel;

	private boolean cancelled = false;
	private Result result = Result.DEFAULT;


	/*
	private Annotation getAnnotationByClass(Class clazz, Class annotazion) {
		Annotation annotation = null;

		if ( clazz.isAnnotationPresent(annotazion) ) try {
			annotation = clazz.getAnnotation(annotazion);
		} catch ( Exception e ) {};  // silently catch error

		if ( annotation == null ) {
			BukkitEventPort.debug("%s: error: annotation not found", clazz.getSimpleName());
			return null;
		}

		return(annotation);
	}


	private Short getAnnotationShort(Annotation annotation, String field) {
		Class annotazion = annotation.getClass();
		Short value = null;

		try {
			value = (Short) annotazion.getField(field).getShort(annotation);
		} catch (Exception e) {
			BukkitEventPort.debug("%s: error: annotation does not contain field '%s'", annotazion.getSimpleName(), field);
		}

		return value;
	}
   */


	public Event(short id) {
		Class clazz = this.getClass();

		busID = id;
		isCancellable = clazz.isAnnotationPresent(Cancellable.class);
		hasResult = clazz.isAnnotationPresent(HasResult.class) || this.isCancellable;
		ignoreCancel = false;
		BukkitEventPort.debug("%s: event initialized with busID=%i", clazz.getSimpleName(), busID);
	}


	/**
	 * Determines if the event can be cancelled.
	 * @return <b>True</b> if event can be cancelled<br><b>False</b> if event cannot be cancelled
	 */
	public boolean isCancellable() {
		return this.isCancellable;
	}


	/**
	 * Get the current cancelled state of the event.
	 * @return <b>True</b> if event is cancelled<br><b>False</b> if event is not cancelled
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}


	/**
	 * Set the state of the event.
	 * @return <b>True</b> if the cancelled state was set<br><b>False</b> if the event cannot be cancelled
	 */
	public boolean setCancelled(boolean state) {
		if (!this.isCancellable()) {
			StackTraceElement[] stack = new Throwable().getStackTrace();
			BukkitEventPort.warning("%s: event cannot be cancelled", stack[0].getMethodName());
			return false;
		}
		cancelled = state;
		return true;
	}


	/**
	 * Determines if the event has a result.
	 * @return <b>True</b> if event has a result<br><b>False</b> if event does not have a result
	 */
	public boolean hasResult()
	{
		return this.hasResult;
	}


	/**
	 * Get the current result for the event.
	 * @return <b>Result.ALLOW</b> event is allowed<br>
	 * <b>Result.DENY</b> event is denied<br>
	 * <b>Result.DEFAULT</b> event performs default action
	 */
	public Result getResult() {
		return this.result;
	}


	/**
	 * Set the result for the event.
	 * @return <b>True</b> if the result was set<br><b>False</b> if the result cannot be set
	 */
	public boolean setResult(Result state) {
		if (!this.hasResult()) {
			StackTraceElement[] stack = new Throwable().getStackTrace();
			BukkitEventPort.warning("%s: event has no result", stack[0].getMethodName());
			return false;
		}
		this.result = state == null ? Result.DEFAULT : state;
		return true;
	}


	public void register(ASMEventHandler listener) {
		this.listenerList.register(getBusID(), listener.getPriority(), listener);
	}


	/**
	 * Retrieve all the listeners for this event.
	 * @return EventListener[]
	 */
	public EventListener[] getListeners() {
		return this.listenerList.getListeners();
	}


	public short getBusID() {
		return busID;
	}

}
