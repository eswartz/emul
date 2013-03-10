/**
 * 
 */
package v9t9.machine.ti99.machine.fileExecutors;

import v9t9.common.events.NotifyException;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
public class ExtBasicLoadAndRunFileExecutor implements IFileExecutor {

	private IModule module;
	private String devicePath;

	public ExtBasicLoadAndRunFileExecutor(IModule module, String devicePath) {
		this.module = module;
		this.devicePath = devicePath;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Load and run " + devicePath + " with " + module.getName();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Load module " + module.getName() + ", load the program " + devicePath + " and run it.\n\n"
				+"This would be done by hand with:\n\n"
				+"OLD " + devicePath +"\n"
				+"RUN"; 
				
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#run(v9t9.common.machine.IMachine)
	 */
	@Override
	public void run(IMachine machine) throws NotifyException {
		machine.getModuleManager().switchModule(module);
		machine.reset();
		machine.getKeyboardHandler().pasteText(" 2"+	// space for title, 2 for extended basic
				"\uFFFC\uFFFC"+
				"\nOLD " + devicePath + "\n"+
				"\uFFFC\uFFFC\uFFFC\uFFFC\uFFFC"+
				"RUN\n");
	}

}
