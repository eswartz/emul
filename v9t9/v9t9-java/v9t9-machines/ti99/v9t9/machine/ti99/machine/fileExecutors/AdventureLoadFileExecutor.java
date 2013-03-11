/**
 * 
 */
package v9t9.machine.ti99.machine.fileExecutors;

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
public class AdventureLoadFileExecutor implements IFileExecutor {

	private IModule module;
	private String devicePath;
	private String name;

	public AdventureLoadFileExecutor(IModule module, String devicePath, String name) {
		this.module = module;
		this.devicePath = devicePath;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getLabel()
	 */
	@Override
	public String getLabel() {
		if (name == null)
			return "Play file " + devicePath + " with " + module.getName();
		else
			return "Play " + name + " (" + devicePath + ") with " + module.getName();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Load module " + module.getName() + 
				" then load the adventure file " + devicePath + "." +
				(name != null ? "\nThis is the game " + name + "." : "");
				
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#run(v9t9.common.machine.IMachine)
	 */
	@Override
	public void run(IMachine machine) throws NotifyException {
		machine.getModuleManager().switchModule(module);
		machine.reset();
		machine.getKeyboardHandler().pasteText(" 2"+	// space for title, 2 for Adventure
				IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO +
				"\n" +
				devicePath + "\n");
	}

}
