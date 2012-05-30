/**
 * 
 */
package v9t9.common.demos;

import v9t9.common.machine.IMachine;

/**
 * This is the base interface implemented by an actor in the emulator
 * that wants to participate in a demo.
 * @author ejs
 *
 */
public interface IDemoActor {
	/**
	 * Get the identifier for the {@link IDemoEvent#getIdentifier()}
	 * created and consumed by this actor.
	 * @return
	 */
	String getEventIdentifier();
	
	/**
	 * Set up the actor for this machine
	 * @param machine
	 */
	void setup(IMachine machine);
}
