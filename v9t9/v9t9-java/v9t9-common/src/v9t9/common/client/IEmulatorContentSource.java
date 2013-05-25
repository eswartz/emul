/**
 * 
 */
package v9t9.common.client;

import v9t9.common.machine.IMachine;


/**
 * This interface represents sources of content usable by the emulator,
 * either through a file, dragged-in content, etc., intended for
 * use in the client in interactive usage.
 * @author ejs
 *
 */
public interface IEmulatorContentSource {
	IEmulatorContentSource[] EMPTY = new IEmulatorContentSource[0];

	/**
	 * Get the machine
	 */
	IMachine getMachine();
	
	/**
	 * Get the content object
	 */
	Object getContent();

	/**
	 * @return
	 */
	String getLabel();
}
