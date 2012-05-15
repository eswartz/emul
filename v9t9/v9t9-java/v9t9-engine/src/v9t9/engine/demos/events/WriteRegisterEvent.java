/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demo.IDemoEvent;

public abstract class WriteRegisterEvent implements IDemoEvent {
	private int reg;
	private int val;
	
	public WriteRegisterEvent(int reg, int val) {
		this.reg = reg;
		this.val = val;
	}
	public int getReg() {
		return reg;
	}
	public int getVal() {
		return val;
	}
}