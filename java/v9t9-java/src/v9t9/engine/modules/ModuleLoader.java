/**
 * 
 */
package v9t9.engine.modules;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.ejs.coffee.core.utils.FileXMLStorage;
import org.ejs.coffee.core.utils.XMLUtils;
import org.w3c.dom.Element;

import v9t9.engine.files.DataFiles;

/**
 * @author ejs
 *
 */
public class ModuleLoader {

	/**
	 * @return
	 */
	public static List<IModule> loadModuleList() {
		
		
		return loadModuleList(DataFiles.resolveFile("modules.xml"));
	}


	/**
	 * 
	 */
	public static List<IModule> loadModuleList(File file) {
		List<IModule> modules = new ArrayList<IModule>();
		FileXMLStorage storage = new FileXMLStorage(file);
		try {
			storage.load("modules");
		} catch (CoreException e) {
			e.printStackTrace();
			return Collections.emptyList();
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
