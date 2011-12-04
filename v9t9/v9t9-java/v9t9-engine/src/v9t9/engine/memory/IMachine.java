/**
 * 
 */
package v9t9.engine.memory;

import v9t9.common.client.IClient;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IBaseMachine;
import v9t9.engine.cpu.IExecutor;
import v9t9.engine.dsr.IDsrManager;
import v9t9.engine.hardware.ICruChip;
import v9t9.engine.hardware.ISoundChip;
import v9t9.engine.hardware.ISpeechChip;
import v9t9.engine.hardware.IVdpChip;
import v9t9.engine.machine.ModuleManager;

/**
 * @author ejs
 *
 */
public interface IMachine extends IBaseMachine {

	IClient getClient();

	void setClient(IClient client);

	IExecutor getExecutor();

	void setExecutor(IExecutor executor);

	IKeyboardState getKeyboardState();

	ISoundChip getSound();
	ISpeechChip getSpeech();

	/**
	 * @return the moduleManager
	 */
	ModuleManager getModuleManager();

	IVdpChip getVdp();

	IDsrManager getDsrManager();

	/**
	 * @return
	 */
	IMachineModel getModel();

	/** Called when keyboardState changes */
	void keyStateChanged();

	/**
	 * @return
	 */
	ICruChip getCru();
	void setCru(ICruChip cru);

}