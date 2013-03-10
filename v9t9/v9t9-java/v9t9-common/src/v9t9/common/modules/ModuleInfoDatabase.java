/*
  ModuleInfoDatabase.java

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.MemoryEntryInfo;
import ejs.base.utils.StorageException;
import ejs.base.utils.StreamXMLStorage;
import ejs.base.utils.XMLUtils;

/**
 * This tracks information about modules.
 * @author ejs
 *
 */
public class ModuleInfoDatabase {
	
	private static final Logger logger = Logger.getLogger(ModuleInfoDatabase.class);

	public static final String NAME = "module_info.xml";

	private URL databaseURL;

	private Map<String, ModuleInfo> infoMap;

	/**
	 * Read the module info database and return a map of MD5 -> image name 
	 * @throws IOException 
	 */
	public static ModuleInfoDatabase loadModuleInfo(IMachine machine) {
		Map<String, ModuleInfo> infoMap = new HashMap<String, ModuleInfo>();
		
		URL databaseURL;
		try {
			databaseURL = new URL(machine.getModel().getDataURL(), NAME);
		} catch (MalformedURLException e) {
			logger.error("could not find " + NAME, e);
			return null;
		}
		
		logger.debug("Loading modules info from " + databaseURL);
		
		InputStream is;
		try {
			is = databaseURL.openStream();
		} catch (IOException e1) {
			logger.debug("failed to read modules info", e1);
			return new ModuleInfoDatabase(databaseURL, infoMap);
		}
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setInputStream(is);
		try {
			storage.load("files");
		} catch (StorageException e) {
			logger.error("failed to load module info", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		for (Element moduleElement : XMLUtils.getChildElementsNamed(storage.getDocumentElement(), "file")) {
			String name = moduleElement.getAttribute("name");
			String md5 = moduleElement.getAttribute("md5");
			logger.debug("Processing " + name);
			
			ModuleInfo info = new ModuleInfo();
			
			info.setName(name);
			
			Element[] entries;
			entries = XMLUtils.getChildElementsNamed(moduleElement, "image");
			for (Element el : entries) {
				String image = el.getTextContent().trim();
				info.setImagePath(image);
			}
			entries = XMLUtils.getChildElementsNamed(moduleElement, "description");
			for (Element el : entries) {
				String descr = el.getTextContent().trim();
				info.setDescription(descr);
			}

			infoMap.put(md5, info);
		}
		
		logger.debug("Done processing modules");
		return new ModuleInfoDatabase(databaseURL, infoMap);
	}
	

	public void saveModuleInfoAndClose(OutputStream os) throws IOException {
		saveModuleInfoAndClose(os, infoMap);

	}
		
	public static void saveModuleInfoAndClose(OutputStream os, Map<String, ModuleInfo> infoMap) throws IOException {
		
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setOutputStream(os);
		
		try {
			storage.create("files");
		} catch (StorageException e1) {
			throw new IOException("Error creating module info XML", e1.getCause());
		}

		Element doc = storage.getDocumentElement();
		
		for (Map.Entry<String, ModuleInfo> ent : infoMap.entrySet()) {
//			String md5 = null;
//			for (MemoryEntryInfo mei : module.getMemoryEntryInfos()) {
//				if ((md5 = mei.getFileMD5()) != null)
//					break;
//			}
//			if (md5 == null)
//				continue;
//			
//			ModuleInfo info = module.getInfo();
//			if (info == null)
//				continue;
			
			String md5 = ent.getKey();
			ModuleInfo info = ent.getValue();
			
			Element moduleElement = doc.getOwnerDocument().createElement("file");
			
			moduleElement.setAttribute("name", info.getName());
			moduleElement.setAttribute("md5", md5);
			
			if (info.getImagePath() != null) {
				Element image = doc.getOwnerDocument().createElement("image");
				image.setTextContent(info.getImagePath());
				moduleElement.appendChild(image);
			}
			
			if (info.getDescription() != null) {
				Element image = doc.getOwnerDocument().createElement("description");
				image.setTextContent(info.getDescription());
				moduleElement.appendChild(image);
			}
			
			doc.appendChild(moduleElement);

		}
		
		try {
			storage.save();
		} catch (StorageException e) {
			throw new IOException("Error saving module info database", e.getCause());
		}

	}

	private ModuleInfoDatabase(URL databaseURL, Map<String, ModuleInfo> infoMap) {
		this.databaseURL = databaseURL;
		//		this.machine = machine;
		this.infoMap = infoMap; 
		
	}

	/**
	 * @return the databaseURL
	 */
	public URL getDatabaseURL() {
		return databaseURL;
	}


	/**
	 * @param module
	 */
	public void syncModuleInfo(IModule module) {
		if (module.getInfo() != null)
			return;
		
		for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
			ModuleInfo moduleInfo = infoMap.get(info.getFileMD5());
			if (moduleInfo != null) {
				// find the first match
				module.setInfo(moduleInfo);
				break;
			}
		}

	}


	/**
	 * @param module
	 */
	public void register(IModule module) {
		if (module.getInfo() == null)
			return;
		
		for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
			if (info.getFileMD5() != null) {
				infoMap.put(info.getFileMD5(), module.getInfo());
			}
			if (info.getFile2MD5() != null) {
				infoMap.put(info.getFile2MD5(), module.getInfo());
			}

		}
	}
}
