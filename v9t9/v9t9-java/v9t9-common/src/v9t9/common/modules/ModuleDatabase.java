/*
  ModuleDatabase.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
import ejs.base.utils.TextUtils;
import ejs.base.utils.XMLUtils;

/**
 * @author ejs
 *
 */
public class ModuleDatabase {
	private static final String ELEMENT_MODULE = "module";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_MD5 = "md5";
	private static final String ATTR_KEYWORDS = "keywords";
	private static final String ATTR_AUTO_START = "autoStart";
	
	private static final Logger logger = Logger.getLogger(ModuleDatabase.class);

	public static List<IModule> loadModuleListAndClose(IMemory memory, 
			ModuleInfoDatabase moduleInfoDb,
			InputStream is, URI databaseURI) throws IOException {
		
		logger.debug("Loading modules database from " + databaseURI);
		
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setInputStream(is);
		List<IModule> modules = new ArrayList<IModule>();
		try {
			storage.load("modules");
		} catch (StorageException e) {
			logger.error("failed to load module database", e);
			if (e.getCause() instanceof StorageException)
				throw new IOException("Error loading module database", e.getCause());
			throw new IOException("Error parsing module database", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		for (Element moduleElement : XMLUtils.getChildElementsNamed(storage.getDocumentElement(), ELEMENT_MODULE)) {
			String name = moduleElement.getAttribute(ATTR_NAME);
			logger.debug("Processing " + name);
			
			Module module = new Module(databaseURI, name);
			
			
			// image?
//			Element[] entries;
//			entries = XMLUtils.getChildElementsNamed(moduleElement, "image");
//			for (Element el : entries) {
//				String image = el.getTextContent().trim();
//				module.setImagePath(image);
//			}
			
			// memory entries
			List<MemoryEntryInfo> memoryEntries = memory.getMemoryEntryFactory().loadEntriesFrom(
					name, moduleElement);
			module.setMemoryEntryInfos(memoryEntries);
			
			if (!memoryEntries.isEmpty()) {
				String moduleMd5 = moduleElement.getAttribute(ATTR_MD5);
				if (moduleMd5 != null && !moduleMd5.isEmpty()) {
					module.setMD5(moduleMd5);
				}
				
				String keywordStr = moduleElement.getAttribute(ATTR_KEYWORDS);
				if (keywordStr != null && keywordStr.length() > 0) {
					String[] kws = keywordStr.split("\\s+");
					module.getKeywords().addAll(Arrays.asList(kws));
				}
				
				String autoStartStr = moduleElement.getAttribute(ATTR_AUTO_START);
				if ("true".equals(autoStartStr)) {
					module.setAutoStart(true);
				}

				if (moduleInfoDb != null)
					moduleInfoDb.syncModuleInfo(module);
				
				modules.add(module);
			}
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
			if (databaseURI != null && !module.getDatabaseURI().equals(databaseURI))
				continue;
				
			Element moduleElement = doc.getOwnerDocument().createElement(ELEMENT_MODULE);
			
			moduleElement.setAttribute(ATTR_NAME, module.getName());
			
			if (!module.getKeywords().isEmpty()) {
				String keywordStr = TextUtils.catenateStrings(module.getKeywords(), " ");
				moduleElement.setAttribute(ATTR_KEYWORDS, keywordStr);
			}

			moduleElement.setAttribute(ATTR_MD5, module.getMD5());
			
			if (module.isAutoStart())
				moduleElement.setAttribute(ATTR_AUTO_START, "true");

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
