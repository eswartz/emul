/**
 * 
 */
package v9t9.common.machine;

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.cpu.IExecutor;
import v9t9.common.demos.IDemoHandler;
import v9t9.common.demos.IDemoManager;
import v9t9.common.events.IEventNotifier;
import v9t9.common.files.IFileHandler;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.hardware.ICruChip;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.modules.IModuleManager;

/**
 * @author ejs
 *
 */
public interface IMachine extends IBaseMachine {


	IExecutor getExecutor();

	void setExecutor(IExecutor executor);

	IKeyboardState getKeyboardState();

	ISoundChip getSound();
	ISpeechChip getSpeech();

	/**
	 * @return the moduleManager
	 */
	IModuleManager getModuleManager();

	IVdpChip getVdp();

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

	IFileHandler getFileHandler();
	
	IPathFileLocator getRomPathFileLocator();

	/**
	 * @return
	 */
	IEventNotifier getEventNotifier();

	IDemoHandler getDemoHandler();
	void setDemoHandler(IDemoHandler handler);

	IDemoManager getDemoManager();
	void setDemoManager(IDemoManager manager);

	IKeyboardMapping getKeyboardMapping();
	void setKeyboardMapping(IKeyboardMapping mapping);

	IKeyboardHandler getKeyboardHandler();
	void setKeyboardHandler(IKeyboardHandler keyboardHandler);
	
	
}