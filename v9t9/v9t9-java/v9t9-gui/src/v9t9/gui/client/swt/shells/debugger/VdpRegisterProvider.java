/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class VdpRegisterProvider extends BaseRegisterProvider {
	/**
	 * @param machine
	 */
	public VdpRegisterProvider(IMachine machine) {
		super(machine, machine.getVdp());
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegisterProvider#getNumDigits()
	 */
	@Override
	public int getNumDigits() {
		return 3;
	}

}
