// ported from forge for BukkitEventPort internal use
package net.MCEventLib.EventBus;

import java.util.ArrayList;

import donington.BukkitEventPort.BukkitEventPort;


public class ListenerList {

	private static ArrayList<ListenerList> allListeners = new ArrayList<ListenerList>();

	private final short busID;
	private ListenerListInstance list;


	public ListenerList(short id) {
		this(id, null);
	}


/*
	public ListenerList(short id, Class eventCurrent, Class eventParent) {
		busID = id;
		allListeners.add(this);

		if      ( eventCurrent == null )
			BukkitEventPort.debug("new ListenerList(%i, null, %s)", busID, eventParent.getSimpleName());
		else if ( eventParent == null )
			BukkitEventPort.debug("new ListenerList(%i, %s, null)", busID, eventCurrent.getSimpleName());
		else
			BukkitEventPort.debug("new ListenerList(%i, current, parent)", busID, eventCurrent.getSimpleName(), eventParent.getSimpleName());
	}
 */


	public ListenerList(short id, ListenerList parentList) {
		busID = id;
		allListeners.add(this);

		if ( parentList == null ) {
			BukkitEventPort.debug("new ListenerList(%i, null)", busID);
			list = new ListenerListInstance();
		} else {
			BukkitEventPort.debug("new ListenerList(%i, %x)", busID, parentList.hashCode());
			list = new ListenerListInstance(parentList.list);
		}
	}


	public void register(int id, EventPriority priority, EventListener listener) {
		if ( id != busID ) return;

		list.register(priority, listener);
	}


	public void unregister(int id, EventListener listener) {
		if ( id != busID ) return;

		list.unregister(listener);
	}


	public static void unregisterListener(int id, EventListener listener) {
		for (ListenerList list : allListeners)
			list.unregister(id, listener);
	}


	public EventListener[] getListeners() {
		return list.getListeners();
	}


	private class ListenerListInstance {
		private boolean rebuild = true;
		private EventListener[] listeners;
		private ArrayList<ArrayList<EventListener>> priorities;
		private ListenerListInstance parent;


		private ListenerListInstance() {
			int count = EventPriority.values().length;
			priorities = new ArrayList<ArrayList<EventListener>>(count);

			for (int x = 0; x < count; x++)
				priorities.add(new ArrayList<EventListener>());
		}


		private ListenerListInstance(ListenerListInstance parent)	{
			this();
			this.parent = parent;
		}


		/*
		public void dispose()
		{
			for (ArrayList<EventListener> listeners : priorities)
			{
				listeners.clear();
			}
			priorities.clear();
			parent = null;
			listeners = null;
		}
		 */


		/**
		 * Returns a ArrayList containing all listeners for this event, 
		 * and all parent events for the specified priority.
		 * 
		 * The list is returned with the listeners for the children events first.
		 * 
		 * @param priority The Priority to get
		 * @return ArrayList containing listeners
		 */
		 public ArrayList<EventListener> getListeners(EventPriority priority) {
			 ArrayList<EventListener> ret = new ArrayList<EventListener>(priorities.get(priority.ordinal()));

			 if (parent != null)
				 ret.addAll(parent.getListeners(priority));

			 return ret;
		 }


		 /**
		  * Returns a full list of all listeners for all priority levels.
		  * Including all parent listeners.
		  * 
		  * List is returned in proper priority order.
		  * 
		  * Automatically rebuilds the internal Array cache if its information is out of date.
		  * 
		  * @return Array containing listeners
		  */
		 public EventListener[] getListeners() {
			 if (shouldRebuild()) buildCache();
			 return listeners;
		 }


		 protected boolean shouldRebuild() {
			 return rebuild || (parent != null && parent.shouldRebuild());
		 }


		 /**
		  * Rebuild the local Array of listeners, returns early if there is no work to do.
		  */
		 private void buildCache() {        
			 if(parent != null && parent.shouldRebuild())
				 parent.buildCache();

			 ArrayList<EventListener> ret = new ArrayList<EventListener>();
			 for (EventPriority value : EventPriority.values())
				 ret.addAll(getListeners(value));

			 listeners = ret.toArray(new EventListener[ret.size()]);
			 rebuild = false;
		 }


		 public void register(EventPriority priority, EventListener listener) {
			 priorities.get(priority.ordinal()).add(listener);
			 rebuild = true;
		 }


		 public void unregister(EventListener listener) {
			 for(ArrayList<EventListener> list : priorities)
				 list.remove(listener);
		 }

	}
}
