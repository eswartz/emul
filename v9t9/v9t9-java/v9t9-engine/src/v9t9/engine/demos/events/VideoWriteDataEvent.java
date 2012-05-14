/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author ejs
 *
 */
public class VideoWriteDataEvent extends WriteDataBlock implements IDemoEvent {

	public VideoWriteDataEvent(int address, byte[] data, int offs, int length) {
		super(address, data, offs, length);
	}

	public VideoWriteDataEvent(int address, byte[] data, int length) {
		super(address, data, 0, length);
	}
	
	public VideoWriteDataEvent(int address, byte[] data) {
		super(address, data, 0, data.length);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteDataBlock#getDomain()
	 */
	@Override
	protected IMemoryDomain getDomain(IMachine machine) {
		return machine.getMemory().getDomain(IMemoryDomain.NAME_VIDEO);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteDataBlock#execute(v9t9.common.machine.IMachine)
	 */
	@Override
	public void execute(IMachine machine) {
		IVdpChip vdp = machine.getVdp();
		for (int i = 0; i < length; i++) {
			vdp.writeAbsoluteVdpMemory(address + i, data[i + offs]);
		}
	}
}
