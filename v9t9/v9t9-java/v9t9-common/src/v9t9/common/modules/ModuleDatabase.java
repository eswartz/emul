/*
  ModuleDatabase.java

  (c) 2011-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.common.modules;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import v9t9.common.events.NotifyException;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.MemoryEntryInfo;
import ejs.base.utils.StorageException;
import ejs.base.utils.StreamXMLStorage;
import ejs.base.utils.XMLUtils;

/**
 * @author ejs
 *
 */
public class ModuleDatabase {
	private static final Logger logger = Logger.getLogger(ModuleDatabase.class);

	public static List<IModule> loadModuleListAndClose(IMemory memory, InputStream is, URI databaseURI) throws NotifyException {
		
		logger.debug("Loading modules database from " + databaseURI);
		
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setInputStream(is);
		List<IModule> modules = new ArrayList<IModule>();
		try {
			storage.load("modules");
		} catch (StorageException e) {
			logger.error("failed to load module database", e);
			if (e.getCause() instanceof StorageException)
				throw new NotifyException(null, "Error loading module database", e.getCause());
			throw new NotifyException(null, "Error parsing module database", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		for (Element moduleElement : XMLUtils.getChildElementsNamed(storage.getDocumentElement(), "module")) {
			String name = moduleElement.getAttribute("name");
			
			logger.debug("Processing " + name);
			
			Module module = new Module(databaseURI, name);
			
			Element[] entries;
			
			// image?
			entries = XMLUtils.getChildElementsNamed(moduleElement, "image");
			for (Element el : entries) {
				String image = el.getTextContent().trim();
				module.setImagePath(image);
			}

			// memory entries
			List<MemoryEntryInfo> memoryEntries = memory.getMemoryEntryFactory().loadEntriesFrom(
					name, moduleElement);
			module.setMemoryEntryInfos(memoryEntries);
			
			if (!memoryEntries.isEmpty())
				modules.add(module);
		}
		
		logger.debug("Done processing modules");
		return modules;
	}
	

	public static void saveModuleListAndClose(IMemory memory, OutputStream os, URI databaseURI, List<IModule> modules) throws NotifyException {
		
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setOutputStream(os);
		
		try {
			storage.create("modules");
		} catch (StorageException e1) {
			throw new NotifyException(null, "Error creating module XML", e1.getCause());
		}

		Element doc = storage.getDocumentElement();
		
		for (IModule module : modules) {
			if (!module.getDatabaseURI().equals(databaseURI))
				continue;
				
			Element moduleElement = doc.getOwnerDocument().createElement("module");
			
			moduleElement.setAttribute("name", module.getName());
			

			if (module.getImagePath() != null) {
				Element image = doc.getOwnerDocument().createElement("image");
				image.setTextContent(module.getImagePath());
				moduleElement.appendChild(image);
			}
			
			memory.getMemoryEntryFactory().saveEntriesTo(
					Arrays.asList(module.getMemoryEntryInfos()), moduleElement);
			
			doc.appendChild(moduleElement);

		}
		
		try {
			storage.save();
		} catch (StorageException e) {
			throw new NotifyException(null, "Error saving module database", e.getCause());
		}

	}
	
}
