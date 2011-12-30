/**
 * 
 */
package v9t9.engine.modules;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import ejs.base.utils.StorageException;
import ejs.base.utils.StreamXMLStorage;
import ejs.base.utils.XMLUtils;

/**
 * @author ejs
 *
 */
public class ModuleLoader {

	public static List<IModule> loadModuleList(IMachine machine, InputStream is, URI databaseURI) throws NotifyException {
		
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setInputStream(is);
		List<IModule> modules = new ArrayList<IModule>();
		try {
			storage.load("modules");
		} catch (StorageException e) {
			if (e.getCause() instanceof StorageException)
				throw new NotifyException(null, "Error loading module database", e.getCause());
			throw new NotifyException(null, "Error parsing module database", e);
		}
		for (Element moduleElement : XMLUtils.getChildElementsNamed(storage.getDocumentElement(), "module")) {
			String name = moduleElement.getAttribute("name");
			Module module = new Module(databaseURI, name);
			
			Element[] entries;
			
			// image?
			entries = XMLUtils.getChildElementsNamed(moduleElement, "image");
			for (Element el : entries) {
				String image = el.getTextContent().trim();
				module.setImagePath(image);
			}

			// memory entries
			List<MemoryEntryInfo> memoryEntries = machine.getMemoryEntryFactory().loadEntriesFrom(
					name, moduleElement);
			module.setMemoryEntryInfos(memoryEntries);
			
			modules.add(module);
		}
		return modules;
	}
	

}
