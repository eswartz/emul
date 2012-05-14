/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;

/**
 * @author ejs
 *
 */
public class VideoWriteRegisterEvent extends WriteRegister implements
		IDemoEvent {

	public VideoWriteRegisterEvent(int addr) {
		super((addr & 0x7f00) >> 8, (addr & 0xff));
	}

	public VideoWriteRegisterEvent(int reg, int val) {
		super(reg, val);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteRegister#getRegisterAccess(v9t9.common.machine.IMachine)
	 */
	@Override
	protected IRegisterAccess getRegisterAccess(IMachine machine) {
		return machine.getVdp();
	}

	/**
	 * @return
	 */
	public int getAddr() {
		return (getReg() << 8) | 0x8000 | (getVal() & 0xff);
	}

}
