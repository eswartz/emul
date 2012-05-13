/**
 * 
 */
package v9t9.server.demo.events;

import java.util.Iterator;

import ejs.base.utils.Tuple;
import v9t9.common.demo.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;

/**
 * Write data in successive addresses
 * @author ejs
 *
 */
public abstract class WriteDataBlock implements IDemoEvent {

	protected int address;
	protected byte[] data;
	protected final int offs;
	protected final int length;
	
	public WriteDataBlock(int address, byte[] data, int offs, int length) {
		this.address = address;
		this.data = data;
		this.offs = offs;
		this.length = length;
	}
	public int getAddress() {
		return address;
	}
	public byte[] getData() {
		return data;
	}
	public int getLength() {
		return length;
	}
	public int getOffset() {
		return offs;
	}
	protected abstract IMemoryDomain getDomain(IMachine machine);
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#execute(v9t9.common.machine.IMachine)
	 */
	@Override
	public void execute(IMachine machine) {
		for (int i = 0; i < length; i++) {
			getDomain(machine).writeByte(address + i, data[i + offs]);
		}
	}
	
	public Iterator<Tuple> rleIterator() {
		// TODO Auto-generated method stub
		return null;
	}
}