/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;

public abstract class WriteRegister implements IDemoEvent {
	private int reg;
	private int val;
	
	public WriteRegister(int reg, int val) {
		this.reg = reg;
		this.val = val;
	}
	public int getReg() {
		return reg;
	}
	public int getVal() {
		return val;
	}
	
	protected abstract IRegisterAccess getRegisterAccess(IMachine machine);
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#execute(v9t9.common.machine.IMachine)
	 */
	@Override
	public void execute(IMachine machine) {
		getRegisterAccess(machine).setRegister(reg, val);
	}
}