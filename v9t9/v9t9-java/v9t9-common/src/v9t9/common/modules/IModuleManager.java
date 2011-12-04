/**
 * 
 */
package v9t9.common.modules;

import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyException;

/**
 * @author ejs
 *
 */
public interface IModuleManager {

	void loadModules(String[] files, IEventNotifier notifier);

	IModule[] getModules();

	void switchModule(IModule module) throws NotifyException;

	void unloadAllModules();

	void loadModule(IModule module) throws NotifyException;

	void unloadModule(IModule loaded);

	void switchModule(String name) throws NotifyException;

	IModule findModuleByName(String string, boolean exact);

	/**
	 * @return
	 */
	IModule[] getLoadedModules();

}