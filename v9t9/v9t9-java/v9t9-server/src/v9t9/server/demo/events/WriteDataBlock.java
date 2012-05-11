/**
 * 
 */
package v9t9.server.demo.events;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;

/**
 * Write data in successive addresses
 * @author ejs
 *
 */
public abstract class WriteDataBlock implements IDemoEvent {
	private int address;
	private byte[] data;
	
	public WriteDataBlock(int address, byte[] data) {
		this.address = address;
		this.data = data;
	}
	public int getAddress() {
		return address;
	}
	public byte[] getData() {
		return data;
	}
	
	protected abstract IMemoryDomain getDomain(IMachine machine);
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#execute(v9t9.common.machine.IMachine)
	 */
	@Override
	public void execute(IMachine machine) {
		for (int i = 0; i < data.length; i++) {
			getDomain(machine).writeByte(address + i, data[i]);
		}
	}
}