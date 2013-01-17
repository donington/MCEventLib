package net.MCEventLib.asm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.MCEventLib.EventBus.Event;
import net.MCEventLib.EventBus.ListenerList;
import net.MCEventLib.annotation.EventBus;
import net.MCEventLib.annotation.EventParent;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;
import static org.objectweb.asm.ClassWriter.*;


import cpw.mods.fml.relauncher.IClassTransformer;
import donington.BukkitEventPort.BukkitEventPort;


public class ASMEventTransformer implements IClassTransformer
{

	public ASMEventTransformer() {}


	@Override
	public byte[] transform(String name, byte[] bytes) {
		// TODO: fix these locations to something within MCEventLib & make it extendible
		if ( !name.startsWith("net.event.") || !name.endsWith("Event") )
			return bytes;

		ClassReader cr = new ClassReader(bytes);
		ClassNode eventNode = new ClassNode();
		cr.accept(eventNode, 0);

		try {
			if ( buildEvent(eventNode) ) {
				ClassWriter cw = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
				eventNode.accept(cw);
				return cw.toByteArray();
			}
			return bytes;
		} catch (Exception e)	{	e.printStackTrace(); }

		return bytes;
	}


	private Class getEventSuper(ClassNode eventNode) throws ClassNotFoundException {
		Class eventSuper = this.getClass().getClassLoader().loadClass(eventNode.superName.replace('/', '.'));

		// ensure this class is derived from Event
		if ( !Event.class.isAssignableFrom(eventSuper) )
			return null;

		// if not a potential eventBus, scan for <init>;  abort if found
//		if ( !eventSuper.equals(Event.class) )
//		for ( MethodNode method : (List<MethodNode>)eventNode.methods ) {
//			if ( method.name.equals("<init>") && method.desc.equals(Type.getMethodDescriptor(VOID_TYPE)) ) { 
//				BukkitEventPort.debug("%s: detected <init>;  aborting", eventSuper.getSimpleName());
//				return null;
//			}
//		}

		return eventSuper;
	}


	private boolean buildEvent(ClassNode eventNode) throws Exception {

		/* ******************** *
		 *   validate_event()   *
		 * ******************** */
		boolean isEventBus = false;
		Class eventSuper = getEventSuper(eventNode);
		Class eventBusRoot = null;					// @EventBus (require: first inheritance from Event)
		Class eventParent = null;   				// @EventParent (declare a parent event (passes listeners, fallback to @EventBus)

		// configure direct descendants of Event class (new @EventBus hierarchy)
		if ( eventSuper.equals(Event.class) ) {
			isEventBus = true;
			// eventBusRoot = eventSuper;
			// eventParent := null

			// configure descendants from an @EventBus hierarchy
		} else {  // TODO: scan seems to be skipping a parent by accident...
			Class ancestor = eventSuper;

			// test for eventParent before entering loop
			if ( ancestor.isAnnotationPresent(EventParent.class) ) {
				eventParent = ancestor;
			}

			// iterate superclass hierarchy (ancestors) to find @EventParent and @EventBus
			while ( !ancestor.equals(Event.class) ) {
				// validate superclass hierarchy as we scan
				if ( !Event.class.isAssignableFrom(ancestor) ) {
					BukkitEventPort.debug("%s: error: Event chain corrupted by member: '%s'", eventNode.name, ancestor.getSimpleName());
					return false;
				}

				eventBusRoot = ancestor;
				ancestor = ancestor.getSuperclass();

				if ( ( eventParent == null ) && ( ancestor.isAnnotationPresent(EventParent.class) ) ) {
					eventParent = ancestor;
				}
			}
		}
		// isEventBus: true if classNode *is* an EventBus;  false if normal event
		// eventBusRoot(!isEventBus) := first superclass descendant from Event || null
		// eventParent: parent event class || null if no parent was detected.

		if ( !isEventBus ) {
			// require eventBusRoot to have @EventBus annotation
			if ( !eventBusRoot.isAnnotationPresent(EventBus.class) ) {
				BukkitEventPort.debug("%s: missing annotation '@EventBus'", eventBusRoot.getSimpleName());
				return false;
			}

			// default eventParent to eventBusRoot if ( !@EventParent )
			if ( eventParent == null )
				eventParent = eventBusRoot;
		}


		Type tSuper = Type.getType(eventSuper);
		Type tList = Type.getType(ListenerList.class);

		BukkitEventPort.debug("buildEvent(): event  := '%s'", eventNode.name.replace('/', '.'));
		BukkitEventPort.debug("buildEvent(): super  := '%s'", eventSuper.getCanonicalName());
		if ( !isEventBus ) {
			BukkitEventPort.debug("buildEvent(): bus    := '%s'", eventBusRoot.getCanonicalName());
			BukkitEventPort.debug("buildEvent(): parent := '%s'", eventParent.getCanonicalName());
		} else
			BukkitEventPort.debug("buildEvent(): event is an @EventBus root");

		FieldNode listenerList = new FieldNode(ACC_PROTECTED | ACC_STATIC, "listenerList", tList.getDescriptor(), null, null);
		eventNode.fields.add(listenerList);

		LabelNode doneLabel = new LabelNode();

		MethodNode method = new MethodNode(ASM4, ACC_PUBLIC, "<init>", getMethodDescriptor(Type.VOID_TYPE), null, null);
		InsnList asm = method.instructions;

		/** begin super() invocation */
		asm.add(new VarInsnNode(ALOAD, 0));
		asm.add(new MethodInsnNode(INVOKESPECIAL, tSuper.getInternalName(), "<init>", getMethodDescriptor(Type.VOID_TYPE)));
		/** end super() invocation */

		/** begin listenerList initialization */
		// if ( listenerList != null ) goto doneLabel
		asm.add(new FieldInsnNode(GETSTATIC, eventNode.name, listenerList.name, listenerList.desc));
		asm.add(new JumpInsnNode(IFNONNULL, doneLabel));

		// listenerList = new ListenerList(?)
		asm.add(new TypeInsnNode(NEW, tList.getInternalName()));
		asm.add(new InsnNode(DUP));

		if ( !isEventBus && ( eventParent != null ) ) {
		  // listenerList = new ListenerList(parent.listenerList);
			Type tParent = Type.getType(eventParent);
			asm.add(new FieldInsnNode(GETSTATIC, tParent.getInternalName(), listenerList.name, listenerList.desc));
			asm.add(new MethodInsnNode(INVOKESPECIAL, tList.getInternalName(), "<init>", getMethodDescriptor(Type.VOID_TYPE, tList)));
		} else {
		  // listenerList = new ListenerList();
			asm.add(new MethodInsnNode(INVOKESPECIAL, tList.getInternalName(), "<init>", getMethodDescriptor(Type.VOID_TYPE)));
		}
		asm.add(new FieldInsnNode(PUTSTATIC, eventNode.name, listenerList.name, listenerList.desc));
		/** end listenerList initialization */

		asm.add(doneLabel);

		/** begin print debugging info */
		asm.add(new LdcInsnNode("%s: ListenerList[%x] is ready"));
		asm.add(new InsnNode(ICONST_2));
		asm.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
		asm.add(new InsnNode(DUP));
		asm.add(new InsnNode(ICONST_0));
		asm.add(new LdcInsnNode(eventNode.name.substring(eventNode.name.lastIndexOf("/")+1)));
		asm.add(new InsnNode(AASTORE));
		asm.add(new InsnNode(DUP));
		asm.add(new InsnNode(ICONST_1));
		asm.add(new FieldInsnNode(GETSTATIC, eventNode.name, listenerList.name, listenerList.desc));
		asm.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I"));
		asm.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
		asm.add(new InsnNode(AASTORE));
		asm.add(new MethodInsnNode(INVOKESTATIC, "donington/BukkitEventPort/BukkitEventPort", "debug",  "(Ljava/lang/String;[Ljava/lang/Object;)V"));
		/** end print debugging info */

		/** method finished */
		method.instructions.add(new InsnNode(RETURN));
		/** method finished */

		eventNode.methods.add(method);
		BukkitEventPort.debug("%s: injected <init>", eventNode.name);
		return true;
	}

}
