/**
 * 
 */
package v9t9.engine.machine;

import v9t9.common.settings.IBaseMachine;
import v9t9.engine.client.IClient;
import v9t9.engine.cpu.Executor;
import v9t9.engine.dsr.IDsrManager;
import v9t9.engine.hardware.ICruAccess;
import v9t9.engine.hardware.ISoundChip;
import v9t9.engine.hardware.ISpeechChip;
import v9t9.engine.hardware.IVdpChip;
import v9t9.engine.keyboard.KeyboardState;

/**
 * @author ejs
 *
 */
public interface IMachine extends IBaseMachine {

	IClient getClient();

	void setClient(IClient client);

	Executor getExecutor();

	void setExecutor(Executor executor);

	KeyboardState getKeyboardState();

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
	MachineModel getModel();

	/** Called when keyboardState changes */
	void keyStateChanged();

	/**
	 * @return
	 */
	ICruAccess getCruAccess();
	void setCruAccess(ICruAccess cruAccess);

}