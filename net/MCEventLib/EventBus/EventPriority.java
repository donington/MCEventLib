package net.MCEventLib.EventBus;

/** Priority of event listeners.<br>
 * Events will be returned sorted to this sequence:
 * <li><b>HIGHEST</b> (first to execute)
 * <li><b>HIGH</b>
 * <li><b>NORMAL</b> (default)
 * <li><b>LOW</b>
 * <li><b>LOWEST</b> (last to execute)
 */
public enum EventPriority {
  // These need to stay in order
  HIGHEST,
  HIGH,
  NORMAL,
  LOW,
  LOWEST
}
