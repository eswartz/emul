/**
 * 
 */
package v9t9.engine.machine;

import v9t9.common.settings.IBaseMachine;
import v9t9.engine.client.IClient;
import v9t9.engine.cpu.Executor;
import v9t9.engine.dsr.IDsrManager;
import v9t9.engine.hardware.ICruAccess;
import v9t9.engine.hardware.SoundChip;
import v9t9.engine.hardware.VdpChip;
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

	SoundChip getSound();

	/**
	 * @return the moduleManager
	 */
	ModuleManager getModuleManager();

	VdpChip getVdp();

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