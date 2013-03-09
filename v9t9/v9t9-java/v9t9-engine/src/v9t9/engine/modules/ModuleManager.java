/*
  ModuleManager.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.modules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.AbortedException;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyException;
import v9t9.common.files.DataFiles;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.Module;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.engine.memory.MemoryEntryInfoBuilder;
import v9t9.engine.memory.StdMultiBankedMemoryEntry;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;
import ejs.base.utils.StorageException;
import ejs.base.utils.StreamXMLStorage;
import ejs.base.utils.TextUtils;
import ejs.base.utils.XMLUtils;

/**
 * @author ejs
 *
 */
public class ModuleManager implements IModuleManager {
	private static Logger log = Logger.getLogger(ModuleManager.class);
	
	private List<IModule> modules;
	private final IMachine machine;
	
	private List<IModule> loadedModules = new ArrayList<IModule>();
	
	public static SettingSchema settingLastLoadedModule = new SettingSchema(
			ISettingsHandler.MACHINE,
			"LastLoadedModule", "");
	
	private Map<IMemoryEntry, IModule> memoryEntryModules = new HashMap<IMemoryEntry, IModule>();
	private IProperty lastLoadedModule;
	private final String stockModuleDatabase;
	
	public ModuleManager(IMachine machine, String stockModuleDatabase) {
		this.machine = machine;
		this.stockModuleDatabase = stockModuleDatabase;
		this.modules = new ArrayList<IModule>();
		
		lastLoadedModule = Settings.get(machine, settingLastLoadedModule);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#getStockDatabaseURL()
	 */
	@Override
	public URL getStockDatabaseURL() {
		try {
			return new URL(machine.getModel().getDataURL(), stockModuleDatabase);
		} catch (MalformedURLException e) {
			log.error("could not find " + stockModuleDatabase, e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#clearModules()
	 */
	@Override
	public void clearModules() {
		modules.clear();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#addModules(java.util.Collection)
	 */
	@Override
	public void addModules(Collection<IModule> modList) {
		for (IModule module : modList) {
			if (!modules.contains(module))
				modules.add(module);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#getModules()
	 */
	@Override
	public IModule[] getModules() {
		return (IModule[]) modules.toArray(new IModule[modules.size()]);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#switchModule(v9t9.common.modules.IModule)
	 */
	@Override
	public void switchModule(IModule module) throws NotifyException {
		unloadAllModules();
		
		loadModule(module);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#unloadAllModules()
	 */
	@Override
	public void unloadAllModules() {
		for (IModule loaded : (IModule[]) loadedModules.toArray(new IModule[loadedModules.size()])) {
			try {
				unloadModule(loaded);
			} catch (AbortedException e) {
				// ignore
			}
		}
		loadedModules.clear();
		lastLoadedModule.setString(null);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#loadModule(v9t9.common.modules.IModule)
	 */
	@Override
	public void loadModule(IModule module) throws NotifyException {
		if (module != null) {
			if (loadedModules.contains(module))
				return;
			
			Collection<IMemoryEntry> entries = getModuleMemoryEntries(module);
			for (IMemoryEntry entry : entries) {
				try {
					machine.getMemory().addAndMap(entry);
				} catch (AbortedException e) {
					// ignore
				}
				memoryEntryModules.put(entry, module);
			}
			loadedModules.add(module);
			
			lastLoadedModule.setString(module.getName());
		} else {
			lastLoadedModule.setString(null);
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#unloadModule(v9t9.common.modules.IModule)
	 */
	@Override
	public void unloadModule(IModule loaded) {
		if (loaded == null)
			return;
		
		IMemory memory = machine.getMemory();
		
		for (IMemoryDomain domain : memory.getDomains())
			for (IMemoryEntry entry : domain.getMemoryEntries()) {
				if (loaded.equals(memoryEntryModules.get(entry))) {
					domain.unmapEntry(entry);
					memoryEntryModules.remove(entry);
				}
			}
		
		loadedModules.remove(loaded);
		lastLoadedModule.setString(null);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#switchModule(java.lang.String)
	 */
	@Override
	public void switchModule(String name) throws NotifyException {
		switchModule(findModuleByName(name, true));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#findModuleByName(java.lang.String, boolean)
	 */
	@Override
	public IModule findModuleByName(String string, boolean exact) {
		for (IModule module : modules) {
			if (exact) {
				if (module.getName().equals(string))
					return module;
			} else {
				if (module.getName().toLowerCase().contains(string.toLowerCase()))
					return module;
			}
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#getLoadedModules()
	 */
	@Override
	public IModule[] getLoadedModules() {
		return (IModule[]) loadedModules.toArray(new IModule[loadedModules.size()]);
	}

	public void saveState(ISettingSection section) {
		String[] moduleNames = new String[loadedModules.size()];
		for (int i = 0; i < moduleNames.length; i++)
			moduleNames[i] = loadedModules.get(i).getName();
		section.put("LoadedModules", moduleNames);
	}
	
	public void loadState(ISettingSection section) {
		unloadAllModules();
		if (section == null)
			return;
		String[] loaded = section.getArray("LoadedModules");
		if (loaded == null)
			return;
		for (String name : loaded) {
			try {
				loadModule(findModuleByName(name, true));
			} catch (NotifyException e) {
				machine.notifyEvent(e.getEvent());
			}
		}
		
	}


	public IMemoryEntry createMemoryEntry(MemoryEntryInfo info, IMemory memory) throws NotifyException {
		try {

			IMemoryEntry entry = null;
			entry = memory.getMemoryEntryFactory().newMemoryEntry(info);
			return entry;
		} catch (IOException e) {
			String filename = info.getString(MemoryEntryInfo.FILENAME); 
			throw new NotifyException(null, "Failed to load file '" + filename + "' for '" + info.getString(MemoryEntryInfo.NAME) +"'", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#getModuleMemoryEntries(v9t9.common.modules.IModule)
	 */
	@Override
	public Collection<IMemoryEntry> getModuleMemoryEntries(IModule module)
			throws NotifyException {
		List<IMemoryEntry> entries = new ArrayList<IMemoryEntry>();
		IMemory memory = machine.getMemory();
		for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
			IMemoryEntry entry = createMemoryEntry(info, memory);
			if (entry != null)
				entries.add(entry);
		}
		return entries;
	}
	

	public void registerModules(URI uri) {
		if (uri == null)
			return;
		
		//boolean anyErrors = false;
		InputStream is = null;
		try {
			is = machine.getRomPathFileLocator().createInputStream(uri);
			List<IModule> modList = ModuleDatabase.loadModuleListAndClose(machine.getMemory(), is, uri);
			addModules(modList);
		} catch (NotifyException e) {
			machine.getClient().getEventNotifier().notifyEvent(e.getEvent());
			//anyErrors = true;
		} catch (IOException e) {
			machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
					"Could not load module list: " + e.getMessage());
		}
		
		/*
		if (anyErrors) {
			machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
					"Be sure your " + DataFiles.settingBootRomsPath.getName() + " setting is established in "
					+ settings.findSettingStorage(DataFiles.settingBootRomsPath.getName()).getConfigFilePath());
		}
		*/
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#reload()
	 */
	@Override
	public void reload() {
		URI databaseURI;
		
		// first, get stock module database
		machine.getModuleManager().clearModules();
		databaseURI = machine.getRomPathFileLocator().findFile("stock_modules.xml");
		if (databaseURI != null) {
			registerModules(databaseURI);
		} else {
			//throw new AssertionError("missing stock_modules.xml");
			return;
		}
		
		// then load any user entries
		IProperty moduleList = Settings.get(machine, IModuleManager.settingModuleList);
		List<String> dbNames = moduleList.getList();
		for (String dbName : dbNames) {
			databaseURI = machine.getRomPathFileLocator().findFile(dbName);
			if (databaseURI != null) {
				registerModules(databaseURI);
			} else {
				machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
						"Could not find module list " + dbName);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#removeModule(v9t9.common.modules.IModule)
	 */
	@Override
	public void removeModule(IModule module) {
		modules.remove(module);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#scanModules(java.io.File)
	 */
	@Override
	public Collection<IModule> scanModules(URI databaseURI, File base) {
		
		File[] files = base.listFiles(); 
		if (files == null) {
			return Collections.emptyList();
		}
		
		Map<String, IModule> moduleMap = new HashMap<String, IModule>();
		
		for (File file : files) {
			if (file.isDirectory())
				continue;

			if (analyzeV9t9ModuleFile(databaseURI, file, moduleMap))
				continue;
			
			if (analyzeGRAMKrackerModuleFile(databaseURI, file, moduleMap))
				continue;
			
			if (analyzeRPKModuleFile(databaseURI, file, moduleMap))
				continue;
		}

		List<IModule> modules = new ArrayList<IModule>(moduleMap.values());
		Collections.sort(modules, new Comparator<IModule>() {

			@Override
			public int compare(IModule o1, IModule o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return modules;
	}

	/**
	 * Look for V9t9 module parts -- bare binaries with pieces identified
	 * by the final filename letter ('C' = console module ROM, 'D' = console module ROM,
	 * bank #2, 'G' = module GROM). 
	 * @param databaseURI
	 * @param file
	 * @param moduleMap
	 * @return 
	 */
	private boolean analyzeV9t9ModuleFile(URI databaseURI, File file,
			Map<String, IModule> moduleMap) {
		String fname = file.getName();
		if (!fname.toLowerCase().endsWith(".bin"))
			return false;
		
		String moduleName = readHeaderName(file);
		if (TextUtils.isEmpty(moduleName) || !isASCII(moduleName))
			return false;
		
		moduleName = cleanupTitle(moduleName);
		
		String base = fname.substring(0, fname.length() - 4); // remove extension
		String key = base.substring(0, base.length() - 1);	// minus indicator char
		
		IModule module = moduleMap.get(key);
		if (module == null) {
			module = new Module(databaseURI, key);
		}
		if (moduleName.length() > 0)
			module.setName(moduleName);
		
		char last = base.charAt(base.length() - 1);
		switch (last) {
		case 'c': {
			
			injectModuleRom(module, databaseURI, moduleName, file.getName(), 0);
			
			break;
		}
		case 'd': {
			injectModuleBank2Rom(module, databaseURI, moduleName, file.getName(), 0);
			
			break;
		}

		case 'g': {
			injectModuleGrom(module, databaseURI, moduleName, file.getName(), 0);
			break;
		}
		default:
			return false;
		}

		// register/update module
		moduleMap.put(key, module);

		return true;
	}

	private void injectModuleGrom(IModule module, URI databaseURI,
			String moduleName, String fileName, int fileOffset) {
		MemoryEntryInfo info = MemoryEntryInfoBuilder.standardModuleGrom(fileName)
				.withOffset(fileOffset)
				.create(moduleName);
		module.addMemoryEntryInfo(info);		
	}

	private void injectModuleRom(IModule module, URI databaseURI,
			String moduleName, String fileName, int fileOffset) {
		MemoryEntryInfo info;
		boolean found = false;
		for (MemoryEntryInfo ex : module.getMemoryEntryInfos()) {
			// modify a banked entry
			if (ex.isBanked() && ex.getAddress() == 0x6000 && IMemoryDomain.NAME_CPU.equals(ex.getDomainName())) {
				ex.getProperties().put(MemoryEntryInfo.FILENAME, fileName);
				ex.getProperties().put(MemoryEntryInfo.CLASS, StdMultiBankedMemoryEntry.class);
				ex.getProperties().put(MemoryEntryInfo.OFFSET, fileOffset);
				found = true;
				break;
			} 
		}
		if (!found) {
			info = MemoryEntryInfoBuilder.standardModuleRom(fileName)
					.withOffset(fileOffset)
					.create(moduleName);
			module.addMemoryEntryInfo(info);
		}
		
	}

	private void injectModuleBank2Rom(IModule module, URI databaseURI,
			String moduleName, String fileName, int fileOffset) {

		MemoryEntryInfo info;
		boolean found = false;
		for (MemoryEntryInfo ex : module.getMemoryEntryInfos()) {
			// modify a non-banked entry
			if (ex.getAddress() == 0x6000 && IMemoryDomain.NAME_CPU.equals(ex.getDomainName())) {
				ex.getProperties().put(MemoryEntryInfo.FILENAME2, fileName);
				ex.getProperties().put(MemoryEntryInfo.CLASS, StdMultiBankedMemoryEntry.class);
				ex.getProperties().put(MemoryEntryInfo.SIZE, -0x2000);
				ex.getProperties().put(MemoryEntryInfo.OFFSET, fileOffset);
				found = true;
				break;
			} 
		}
		if (!found) {
			// make temporary
			info = MemoryEntryInfoBuilder.standardModuleRom(fileName)
					.withFilename2(fileName)
					.withOffset2(fileOffset)
					.withBankClass(StdMultiBankedMemoryEntry.class)
					.create(moduleName);
			module.addMemoryEntryInfo(info);
		}
		
	}
	
	
	/**
	 * Analyze GRAM Kracker-format files
	 * 
	 * &lt;http://www.ninerpedia.org/index.php/GRAM_Kracker_format&gt;
	 *  
	 * @param databaseURI 
	 * @param file
	 * @param moduleMap
	 * @return
	 */
	private boolean analyzeGRAMKrackerModuleFile(URI databaseURI, File file,
			Map<String, IModule> moduleMap) {
		
		
		return false;
	}
	
	
	/**
	 * Analyze MESS *.rpk format files
	 *  
	 * @param databaseURI 
	 * @param file
	 * @param moduleMap
	 * @return
	 */
	private boolean analyzeRPKModuleFile(URI databaseURI, File file,
			Map<String, IModule> moduleMap) {
		
		if (!file.getName().toLowerCase().endsWith(".rpk")) {
			return false;
		}
		
		// it is a zip container: softlist.xml contains the info
		ZipFile zf;
		try {
			zf = new ZipFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements(); ) {
				ZipEntry ent = en.nextElement();
				if (ent.getName().equals("softlist.xml")) {
					InputStream is = zf.getInputStream(ent);
					IModule module = convertSoftList(databaseURI, file.toURI(), is);
					if (module != null) {
						moduleMap.put(module.getName(), module);
					}
					is.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				zf.close();
			} catch (IOException e) {
			}
		}
		
		return true;
	}
	
	/**
	 * @param is
	 * @return
	 */
	private IModule convertSoftList(URI databaseURI, URI zipUri, InputStream is) {
		IModule module;
		
		StreamXMLStorage storage = new StreamXMLStorage();
		storage.setInputStream(is);
		try {
			storage.load("software");
		} catch (StorageException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		
		String moduleName = storage.getDocumentElement().getAttribute("name");
		module = new Module(databaseURI, moduleName);
		
		for (Element el : XMLUtils.getChildElements(storage.getDocumentElement())) {
			if (el.getNodeName().equals("description")) {
				moduleName = XMLUtils.getText(el);
				module.setName(moduleName);
			}
			else if (el.getNodeName().equals("year")
					|| el.getNodeName().equals("publisher")
					|| el.getNodeName().equals("info")) {
				//
			}
		}
		for (Element partEl : XMLUtils.getChildElementsNamed(storage.getDocumentElement(),  "part")) {
			if (!partEl.getAttribute("interface").equals("ti99_cart"))
				return null;
			
			//Element[] feats = XMLUtils.getChildElementsNamed(partEl, "feature");
			
			Element[] dataAreas = XMLUtils.getChildElementsNamed(partEl, "dataarea");
			for (Element dataArea : dataAreas) {
				String name = dataArea.getAttribute("name");
				if (name.equals("rom_socket")) {
					for (Element rom : XMLUtils.getChildElementsNamed(dataArea, "rom")) {
						injectModuleRom(module, databaseURI, moduleName, 
								makeZipUriString(zipUri, rom.getAttribute("name")),
								HexUtils.parseInt(rom.getAttribute("offset")));
					}
				}
				else if (name.equals("rom2_socket")) {
					for (Element rom : XMLUtils.getChildElementsNamed(dataArea, "rom")) {
						injectModuleBank2Rom(module, databaseURI, moduleName, 
								makeZipUriString(zipUri, rom.getAttribute("name")),
								HexUtils.parseInt(rom.getAttribute("offset")));
					}
				}
				else if (name.equals("grom_socket")) {
					for (Element rom : XMLUtils.getChildElementsNamed(dataArea, "rom")) {
						injectModuleGrom(module, databaseURI, moduleName, 
								makeZipUriString(zipUri, rom.getAttribute("name")),
								HexUtils.parseInt(rom.getAttribute("offset")));
					}
				}
			}
			
		}

		return module;
	}

	private String makeZipUriString(URI zipUri, String name) {
		try {
			return new URI("jar", zipUri + "!/" + name, null).toString();
		} catch (URISyntaxException e) {
			assert false;
			return name;
		}
	}

	/**
	 * @param file
	 * @return
	 */
	private String readHeaderName(File file) {
		byte[] content;
		try {
			content = DataFiles.readMemoryImage(file, 0, 0x2000);
		} catch (IOException e) {
			return null;
		}
		if (content[0] != (byte) 0xaa) {
			return "";
		}
		
		// program list
		int addr = readAddr(content, 0x6);
		if (addr == 0)
			return "";

		// get the last name (ordered reverse, non-English first)
		String name;
		int next;
		do {
			next = readAddr(content, (addr & 0x1fff));
			name = readString(content, (addr & 0x1fff) + 4);
			addr = next;
		} while (next >= 0x6000);		/* else not really module? */
		
		return name.trim();
	}

	/**
	 * @param name
	 * @return
	 */
	private boolean isASCII(String name) {
		for (char ch : name.toCharArray()) {
			if (ch < 0x20 || ch >= 127)
				return false;
		}
		return true;
	}

	/**
	 * @param addr
	 * @return
	 */
	private String readString(byte[] content, int addr) {
		int len = content[addr++] & 0xff;
		StringBuilder sb = new StringBuilder();
		while (len != 0 && addr < content.length) {
			sb.append((char) content[addr++]);
			len--;
		}
		return sb.toString();
	}

	/**
	 * @param content
	 * @param i
	 * @return
	 */
	private int readAddr(byte[] content, int i) {
		return ((content[i] << 8) & 0xff00) | (content[i+1] & 0xff);
	}
	
	private String cleanupTitle(String allCaps) {
		// remove spurious quotes
		allCaps = TextUtils.unquote(allCaps, '"');
		
		// capitalize each word
		StringBuilder sb = new StringBuilder();
		boolean newWord = true;
		for (char ch : allCaps.toCharArray()) {
			
			if (Character.isLetter(ch)) {
				if (newWord) {
					ch = Character.toUpperCase(ch);
					newWord = false;
				} else {
					ch = Character.toLowerCase(ch);
				}
			} else {
				newWord = true;
			}
				
			sb.append(ch);
		}
		
		String titledName = sb.toString();
		
		// lowercase prepositions
		for (String prep : new String[] { "And", "Or", "Of", "In", "For", "The" }) {
			titledName = replaceWord(titledName, prep, prep.toLowerCase(), false);
		}
				
		// uppercase common acronyms
		for (String acr : new String[] { "Ti", "Ii", "Iii" }) {
			titledName = replaceWord(titledName, acr, acr.toUpperCase(), true);
		}
				
		return titledName;		
	}

	private String replaceWord(String str, String word, String repl, boolean allowAtStart) {
		int idx = str.indexOf(word);
		if (idx > (allowAtStart ? -1 : 0)) {
			str = str.substring(0, idx) + repl + str.substring(idx + word.length());
		}

		return str;
	}
}
