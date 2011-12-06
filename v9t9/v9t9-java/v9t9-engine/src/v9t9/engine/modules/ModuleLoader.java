/**
 * 
 */
package v9t9.engine.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import v9t9.base.utils.FileXMLStorage;
import v9t9.base.utils.StorageException;
import v9t9.base.utils.XMLUtils;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.NotifyException;
import v9t9.common.files.DataFiles;
import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
public class ModuleLoader {

	/**
	 * @return
	 */
	public static List<IModule> loadModuleList(ISettingsHandler settings, String name) throws NotifyException {
		
		
		File file;
		
		file = DataFiles.resolveFileAtPath(settings.getInstanceSettings().getConfigDirectory(), name);
		if (file == null) {
			file = DataFiles.resolveFile(settings, name);
			if (file == null)
				throw new NotifyException(null, "Cannot locate module list " + name);
		}
		
		List<IModule> modules = new ArrayList<IModule>();
		FileXMLStorage storage = new FileXMLStorage(file);
		try {
			storage.load("modules");
		} catch (StorageException e) {
			if (e.getCause() instanceof StorageException)
				throw new NotifyException(null, "Error loading module list " + name, e.getCause());
			throw new NotifyException(null, "Error parsing module list " + name, e);
		}
		for (Element el : XMLUtils.getChildElementsNamed(storage.getDocumentElement(), "module")) {
			Module module = new Module(
					el.getAttribute("name"));
			module.loadFrom(el);
			modules.add(module);
		}
		return modules;
	}
}
