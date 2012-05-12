/**
 * 
 */
package v9t9.server.demo.events;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;

/**
 * @author ejs
 *
 */
public class SoundWriteRegisterEvent extends WriteRegister implements
		IDemoEvent {

	public SoundWriteRegisterEvent(int reg, int val) {
		super(reg, val);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.demo.events.WriteRegister#getRegisterAccess(v9t9.common.machine.IMachine)
	 */
	@Override
	protected IRegisterAccess getRegisterAccess(IMachine machine) {
		return machine.getSound();
	}

}
