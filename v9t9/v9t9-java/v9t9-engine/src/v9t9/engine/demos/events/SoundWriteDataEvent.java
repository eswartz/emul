/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author ejs
 *
 */
public class SoundWriteDataEvent extends WriteDataToAddr implements IDemoEvent {

	public SoundWriteDataEvent(int address, byte[] data) {
		super(address, data, data.length);
	}

	public SoundWriteDataEvent(int addr, byte[] data,
			int length) {
		super(addr, data, length);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteDataToAddr#getDomain(v9t9.common.machine.IMachine)
	 */
	@Override
	protected IMemoryDomain getDomain(IMachine machine) {
		return machine.getConsole();
	}

}
