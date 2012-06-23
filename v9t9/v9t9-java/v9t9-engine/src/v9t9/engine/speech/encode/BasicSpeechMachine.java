/**
 * 
 */
package v9t9.engine.speech.encode;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.engine.machine.MachineBase;

/**
 * @author ejs
 *
 */
public class BasicSpeechMachine extends MachineBase implements IMachine {

	/**
	 * @param settings
	 * @param machineModel
	 */
	public BasicSpeechMachine(ISettingsHandler settings,
			IMachineModel machineModel) {
		super(settings, machineModel);
	}

}
