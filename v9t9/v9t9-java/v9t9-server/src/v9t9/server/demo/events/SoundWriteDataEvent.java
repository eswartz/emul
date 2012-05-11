/**
 * 
 */
package v9t9.server.demo.events;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author ejs
 *
 */
public class SoundWriteDataEvent extends WriteDataToAddr implements IDemoEvent {

	public SoundWriteDataEvent(int address, byte[] data) {
		super(address, data);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteDataToAddr#getDomain(v9t9.common.machine.IMachine)
	 */
	@Override
	protected IMemoryDomain getDomain(IMachine machine) {
		return machine.getConsole();
	}

}
