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
public class VideoWriteDataEvent extends WriteDataBlock implements IDemoEvent {

	/**
	 * @param address
	 * @param data
	 */
	public VideoWriteDataEvent(int address, byte[] data) {
		super(address, data);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteDataBlock#getDomain()
	 */
	@Override
	protected IMemoryDomain getDomain(IMachine machine) {
		return machine.getMemory().getDomain(IMemoryDomain.NAME_VIDEO);
	}

}
