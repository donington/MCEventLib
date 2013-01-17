package net.MCEventLib.EventBus;

import java.util.ArrayList;



/** this isn't fully implemented yet
 * @author donington
 */
public abstract class EventBusRegistry {
	private static ArrayList<Boolean> buslist = new ArrayList<Boolean>();
	private static final short buslimit = 256;  // probably way more than necessary


	public static Short getNewEventBus() {
		if ( buslist.size() > buslimit ) return(null);
		short id = (short) buslist.size();
		buslist.add(true);
		return id;
	}

}
