/**
 * 
 */
package v9t9.engine.demos.events;

import v9t9.common.demos.IDemoEvent;

/**
 * @author ejs
 *
 */
public class SoundWriteRegisterEvent extends WriteRegisterEvent implements
		IDemoEvent {

	public static final String ID = "SoundWriteRegister";

	public SoundWriteRegisterEvent(int reg, int val) {
		super(reg, val);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoEvent#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
	
}
