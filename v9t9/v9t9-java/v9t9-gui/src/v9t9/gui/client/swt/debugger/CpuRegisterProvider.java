/**
 * 
 */
package v9t9.gui.client.swt.debugger;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuRegisterProvider extends BaseRegisterProvider {
	/**
	 * @param machine
	 */
	public CpuRegisterProvider(IMachine machine) {
		super(machine, machine.getCpu().getState());
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegisterProvider#getNumDigits()
	 */
	@Override
	public int getNumDigits() {
		return 4;
	}

}
