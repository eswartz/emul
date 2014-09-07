/**
 * 
 */
package v9t9.machine.common.tests;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.machine.IMachineModel;
import v9t9.engine.machine.MachineBase;

/**
 * @author ejs
 *
 */
public class BasicMachineTest extends MachineBase {
	public BasicMachineTest(ISettingsHandler settings,
			IMachineModel machineModel) {
		super(settings, machineModel);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.machine.MachineBase#createFileExecutionHandler()
	 */
	@Override
	protected IFileExecutionHandler createFileExecutionHandler() {
		return null;
	}

}
