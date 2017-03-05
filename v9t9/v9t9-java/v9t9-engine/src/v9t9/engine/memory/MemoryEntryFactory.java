/*
  MemoryEntryFactory.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;
import ejs.base.utils.XMLUtils;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.MD5FilterAlgorithms;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryArea;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryEntryFactory;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.memory.StoredMemoryEntryInfo;
import v9t9.common.modules.ModuleDatabase;

/**
 * This factory assists in creating {@link IMemoryEntry} instances.
 * @author ejs
 *
 */
public class MemoryEntryFactory implements IMemoryEntryFactory {
	
	private IPathFileLocator locator;
	private final IMemory memory;
	private final ISettingsHandler settings;

	public MemoryEntryFactory(ISettingsHandler settings, IMemory memory, IPathFileLocator locator) {
		this.settings = settings;
		this.memory = memory;
		this.locator = locator;
	}
	

    /* (non-Javadoc)
	 * @see v9t9.engine.memory.IMemoryEntryFactory#newMemoryEntry(v9t9.common.modules.MemoryEntryInfo)
	 */
    @Override
	public IMemoryEntry newMemoryEntry(MemoryEntryInfo info) throws IOException {
    	if (!info.isBanked())
    		return newSimpleMemoryEntry(info);
    	else
    		return newBankedMemoryFromFile(info);
    	
    }

	/**
	 * @param info
	 * @return
	 * @throws IOException
	 */
	protected IMemoryEntry newSimpleMemoryEntry(MemoryEntryInfo info)
			throws IOException {
		IMemoryEntry entry;
    	
    	if (info.getResolvedFilename(settings) != null) {
    		entry = newFromFile(info, (MemoryArea) createMemoryArea(info));
    	}
    	else {
    		int size = Math.abs(info.getSize());
    		entry = new MemoryEntry(info.getName(), info.getDomain(memory), 
    				info.getAddress(), size,  (MemoryArea) createMemoryArea(info));
    	}
        
        return entry;
	}


	/**
	 * @param info
	 * @return
	 * @throws IOException
	 */
	public IMemoryArea createMemoryArea(MemoryEntryInfo info)
			throws IOException {
		MemoryArea area;
    	
		int latency = info.getDomain(memory).getLatency(info.getAddress());

		boolean hasFile = info.getResolvedFilename(settings) != null && !info.isStored();
		int size = Math.abs(info.getSize());
		
		if (info.isByteSized()) {
			ByteMemoryArea bma = new ByteMemoryArea(latency, hasFile ? null : new byte[size], info.isStored());
			area = bma;
		} else {
			WordMemoryArea wma = new WordMemoryArea(latency, hasFile ? null : new short[size/2], info.isStored());
			area = wma;
		}
        
        return area;
	}

    /**
     * Create a memory entry for banked (ROM) memory.
     * @param klass
     * @param addr
     * @param size
     * @param memory
     * @param name
     * @param domain
     * @param filepath
     * @param fileoffs
     * @param filepath2
     * @param fileoffs2
     * @return
     * @throws IOException
     */
	private BankedMemoryEntry newBankedMemoryFromFile(MemoryEntryInfo info) throws IOException {
		@SuppressWarnings("unchecked")
		Class<? extends BankedMemoryEntry> klass = (Class<? extends BankedMemoryEntry>) info.getBankedClass();
		if (klass == null)
			throw new IOException("no 'class' specified for banked memory in " + info);
		
		if (info.getFilename2() == null && info.getFilename2Property() == null) {
			return newMultiBankedMemoryFromFile(info);
		}
		
		IMemoryEntry bank0 = newFromFile(info.asFirstBank(),  
				MemoryAreaFactory.createMemoryArea(memory, info));
		IMemoryEntry bank1 = newFromFile(info.asSecondBank(), 
				MemoryAreaFactory.createMemoryArea(memory, info));
		
		IMemoryEntry[] entries = new IMemoryEntry[] { bank0, bank1 };
		BankedMemoryEntry bankedMemoryEntry;
		try {
			bankedMemoryEntry = klass.getConstructor(
					ISettingsHandler.class,
					IMemory.class, String.class, entries.getClass()).newInstance(
							settings, memory, info.getName(), entries);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw (IOException) new IOException().initCause(e);
		} catch (Exception e) {
			throw (IOException) new IOException().initCause(e);
		}
		return bankedMemoryEntry;
	}


    /**
	 * @param info
	 * @return
	 */
	private BankedMemoryEntry newMultiBankedMemoryFromFile(MemoryEntryInfo info) throws IOException {

    	StoredMemoryEntryInfo storedInfo = resolveMemoryEntry(info);

    	int bankSize = info.getBankSize();
    	if (bankSize == 0)
    		bankSize = 0x2000;
    	
    	int numBanks = storedInfo.size / bankSize;
    	if (numBanks == 0)
    		throw new IOException("no banks found for " + info.getName());
    	
    	IMemoryEntry[] entries = new IMemoryEntry[numBanks];
    	
    	boolean reversed = storedInfo.info.isReversed();
    	
    	for (int bank = 0; bank < entries.length; bank++) {
    		int entryIdx = reversed ? entries.length - bank - 1 : bank;
    		entries[entryIdx] = newFromFile(info.asBank(entryIdx, info.getOffset() + bank * bankSize, bankSize), 
    				MemoryAreaFactory.createMemoryArea(memory, info));
    	}
		
		BankedMemoryEntry bankedMemoryEntry;
		try {
			bankedMemoryEntry = new StdMultiBankedMemoryEntry(
							settings, memory, info.getName(), entries);
		} catch (Exception e) {
			throw (IOException) new IOException().initCause(e);
		}
		return bankedMemoryEntry;
	}


	/** 
	 * Construct a DiskMemoryEntry 
     * @throws IOException if the memory cannot be read and is not stored
     */
    private DiskMemoryEntry newFromFile(MemoryEntryInfo info, MemoryArea area) throws IOException {
    	StoredMemoryEntryInfo storedInfo = resolveMemoryEntry(info);
    	
    	DiskMemoryEntry entry = new DiskMemoryEntry(info, area, storedInfo);
    	
        entry.setArea(MemoryAreaFactory.createMemoryArea(memory, info)); 
        
    	return entry;
    }

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.IMemoryEntryFactory#resolveMemoryEntry(v9t9.common.modules.MemoryEntryInfo, java.lang.String, java.lang.String, int)
	 */
	@Override
	public StoredMemoryEntryInfo resolveMemoryEntry(MemoryEntryInfo info) throws IOException {

		return StoredMemoryEntryInfo.createStoredMemoryEntryInfo(
				locator, settings, memory, info);
	}



	/**
	 * Create a memory entry from storage
	 * @param entryStore
	 * @return
	 */
	public IMemoryEntry createEntry(IMemoryDomain domain, IEventNotifier notifier, ISettingSection entryStore) {
		MemoryEntry entry = null;
		String klazzName = entryStore.get("Class");
		if (klazzName != null) {
			Class<?> klass;
			try {
				klass = Class.forName(klazzName);
				
				entry = (MemoryEntry) klass.newInstance();
			} catch (Exception e) {
				// in case packages change...
				if (klazzName.endsWith(".DiskMemoryEntry")) {
					klass = DiskMemoryEntry.class;
				} else if (klazzName.endsWith(".MemoryEntry")) {
					klass = MemoryEntry.class;
				} else if (klazzName.endsWith(".MultiBankedMemoryEntry")) {
					klass = MultiBankedMemoryEntry.class;
				} else if (klazzName.endsWith(".WindowBankedMemoryEntry")) {
					klass = WindowBankedMemoryEntry.class;
				} else {
					e.printStackTrace();
					return null;
				}
			}
			
			entry.setLocator(locator);
			entry.setDomain(domain);
			if (entryStore.get("WordAccess") != null)
				entry.setWordAccess(entryStore.getBoolean("WordAccess"));
			else
				entry.setWordAccess(domain.getIdentifier().equals(IMemoryDomain.NAME_CPU));	// TODO
			int latency = domain.getLatency(entryStore.getInt("Address"));
			if (entry.isWordAccess())
				entry.setArea(new WordMemoryArea(latency));
			else
				entry.setArea(new ByteMemoryArea(latency));
			
			entry.setMemory(domain.getMemory());
			
			try {
				entry.loadMemory(notifier, entryStore);
			} catch (IOException e) {
				notifier.notifyEvent(this, Level.ERROR, e.getMessage());
			}
		}
		return entry;
	}

	
	public List<MemoryEntryInfo> loadEntriesFrom(String name, Element root) {

		List<MemoryEntryInfo> memoryEntries = new ArrayList<MemoryEntryInfo>();
		
		Element[] entries;
		
		entries = XMLUtils.getChildElementsNamed(root, "memoryEntries");
		for (Element entry : entries) {
			for (Element el : XMLUtils.getChildElements(entry)) {
				MemoryEntryInfo info = new MemoryEntryInfo();
				Map<String, Object> properties = info.getProperties();
				
				properties.put(MemoryEntryInfo.NAME, name);
				
				// helpers
				if (el.getNodeName().equals("romModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_CPU);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					properties.put(MemoryEntryInfo.SIZE, 0x2000);
				}
				else if (el.getNodeName().equals("gromModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_GRAPHICS);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					properties.put(MemoryEntryInfo.SIZE, -0xA000);
					properties.put(MemoryEntryInfo.FILE_MD5_ALGORITHM, MD5FilterAlgorithms.ALGORITHM_GROM);
				}
				else if (el.getNodeName().equals("bankedModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_CPU);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					properties.put(MemoryEntryInfo.SIZE, 0x2000);
					
					if ("true".equals(el.getAttribute("custom"))) {
						properties.put(MemoryEntryInfo.CLASS, BankedMemoryEntry.class);
					} else {
						properties.put(MemoryEntryInfo.CLASS, StdMultiBankedMemoryEntry.class);
						if (el.getAttribute(MemoryEntryInfo.FILENAME2).isEmpty()) {
							properties.put(MemoryEntryInfo.SIZE, -0x40000);
						}
					}
				}
				else if (!el.getNodeName().equals("memoryEntry")) {
					System.err.println("Unknown entry: " + el.getNodeName());
					continue;
				}
				
				getStringAttribute(el, MemoryEntryInfo.FILENAME, info);
				getStringAttribute(el, MemoryEntryInfo.FILENAME2, info);
				
				getStringAttribute(el, MemoryEntryInfo.FILE_MD5, info);
				getStringAttribute(el, MemoryEntryInfo.FILE_MD5_ALGORITHM, info);
				getIntAttribute(el, MemoryEntryInfo.FILE_MD5_LIMIT, info);
				
				getStringAttribute(el, MemoryEntryInfo.FILE2_MD5, info);
				getStringAttribute(el, MemoryEntryInfo.FILE2_MD5_ALGORITHM, info);
				getIntAttribute(el, MemoryEntryInfo.FILE2_MD5_LIMIT, info);
				
				getStringAttribute(el, MemoryEntryInfo.DOMAIN, info);
				getIntAttribute(el, MemoryEntryInfo.ADDRESS, info);
				getIntAttribute(el, MemoryEntryInfo.SIZE, info);
				getIntAttribute(el, MemoryEntryInfo.OFFSET, info);
				getIntAttribute(el, MemoryEntryInfo.OFFSET2, info);
				getBooleanAttribute(el, MemoryEntryInfo.STORED, info);
				getClassAttribute(el, MemoryEntryInfo.CLASS, MemoryEntry.class, info);
				getBooleanAttribute(el, MemoryEntryInfo.REVERSED, info);

				properties.put(MemoryEntryInfo.UNIT_SIZE, 
						IMemoryDomain.NAME_CPU.equals(properties.get(MemoryEntryInfo.DOMAIN)) ? 2 : 1);
				
				memoryEntries.add(info);
			}
		}
		
		return memoryEntries;
	}
	

	public void saveEntriesTo(Collection<MemoryEntryInfo> memoryEntries, Element root) {
		Element memoryEntriesEl = root.getOwnerDocument().createElement("memoryEntries");
		root.appendChild(memoryEntriesEl);
		
		for (MemoryEntryInfo info : memoryEntries) {
			
			Map<String, Object> properties = info.getProperties();
			Element entry = null;
			
			// helpers
			boolean needAddress = true;
			boolean needSize = true;
			boolean needDomain = true;
			Class<?> cls = (Class<?>)properties.get(MemoryEntryInfo.CLASS);
			
			boolean isBanked = 
					(cls != null && BankedMemoryEntry.class.isAssignableFrom(cls))
					|| (cls == null && (properties.containsKey(MemoryEntryInfo.FILENAME2)
							|| properties.containsKey(MemoryEntryInfo.FILE2_MD5)));
			
			Integer offset = (Integer) properties.get(MemoryEntryInfo.OFFSET);
			Integer size = (Integer) properties.get(MemoryEntryInfo.SIZE);
			if (IMemoryDomain.NAME_CPU.equals(properties.get(MemoryEntryInfo.DOMAIN))
					&& (Integer) properties.get(MemoryEntryInfo.ADDRESS) == 0x6000
					&& (size == null || Math.abs(size) <= 0x2000)
					&& (offset == null || offset == 0)
					&& !isBanked) {
				entry = root.getOwnerDocument().createElement("romModuleEntry");
				needAddress = needDomain = false;
				if (size == null || Math.abs(size) == 0x2000)
					needSize = false;
			}
			else if (IMemoryDomain.NAME_GRAPHICS.equals(properties.get(MemoryEntryInfo.DOMAIN))
					&& (Integer) properties.get(MemoryEntryInfo.ADDRESS) == 0x6000) {
				entry = root.getOwnerDocument().createElement("gromModuleEntry");
				needAddress = needDomain = false;
				if (size == null || size < 0)
					needSize = false;
			} else {
				if (IMemoryDomain.NAME_CPU.equals(properties.get(MemoryEntryInfo.DOMAIN))
						&& (Integer) properties.get(MemoryEntryInfo.ADDRESS) == 0x6000
						&& isBanked) {
					entry = root.getOwnerDocument().createElement("bankedModuleEntry");
					needAddress = needDomain = false;
					if ((size == null || Math.abs(size) == 0x2000)
							&& (offset == null || offset == 0))
						needSize = false;
					
					if (cls == null || BankedMemoryEntry.class.equals(cls))
						entry.setAttribute("custom", "true");
				}
				else {
					entry = root.getOwnerDocument().createElement("memoryEntry");
				}
			}

			if (needDomain) {
				entry.setAttribute(MemoryEntryInfo.DOMAIN, ""+properties.get(MemoryEntryInfo.DOMAIN));
			}
			if (needAddress) {
				entry.setAttribute(MemoryEntryInfo.ADDRESS, 
						"0x" + HexUtils.toHex4(((Number) properties.get(MemoryEntryInfo.ADDRESS)).intValue()));
			}
			if (needSize && size != null) {
				if (size < 0) {
					entry.setAttribute(MemoryEntryInfo.SIZE, Integer.toString(size));
				} else {
					entry.setAttribute(MemoryEntryInfo.SIZE, 
							"0x" + HexUtils.toHex4(size));
				}
			}
			if (properties.containsKey(MemoryEntryInfo.FILENAME))
				entry.setAttribute(MemoryEntryInfo.FILENAME, properties.get(MemoryEntryInfo.FILENAME).toString());
			if (properties.containsKey(MemoryEntryInfo.FILE_MD5))
				entry.setAttribute(MemoryEntryInfo.FILE_MD5, properties.get(MemoryEntryInfo.FILE_MD5).toString());
			if (properties.containsKey(MemoryEntryInfo.FILE_MD5_ALGORITHM)) {
				setMd5Algorithm(properties, MemoryEntryInfo.FILE_MD5_ALGORITHM, entry);
			}
			
			if (properties.containsKey(MemoryEntryInfo.FILENAME2))
				entry.setAttribute(MemoryEntryInfo.FILENAME2, properties.get(MemoryEntryInfo.FILENAME2).toString());
			if (properties.containsKey(MemoryEntryInfo.FILE2_MD5))
				entry.setAttribute(MemoryEntryInfo.FILE2_MD5, properties.get(MemoryEntryInfo.FILE2_MD5).toString());
			if (properties.containsKey(MemoryEntryInfo.FILE2_MD5_ALGORITHM)) {
				setMd5Algorithm(properties, MemoryEntryInfo.FILE2_MD5_ALGORITHM, entry);
			}
			
			if (properties.containsKey(MemoryEntryInfo.OFFSET) && ((Number) properties.get(MemoryEntryInfo.OFFSET)).intValue() != 0)
				entry.setAttribute(MemoryEntryInfo.OFFSET, 
						"0x" + HexUtils.toHex4(((Number) properties.get(MemoryEntryInfo.OFFSET)).intValue()));
			if (info.isStored())
				entry.setAttribute(MemoryEntryInfo.STORED, "true");
			if (info.isBanked() && info.isReversed())
				entry.setAttribute(MemoryEntryInfo.REVERSED, "true");

			memoryEntriesEl.appendChild(entry);
		}
	}


	/**
	 * @param properties
	 * @param entry
	 */
	private void setMd5Algorithm(Map<String, Object> properties,
			String propName,
			Element entry) {
		String alg = properties.get(propName).toString();
		if ((MD5FilterAlgorithms.ALGORITHM_GROM.equals(alg) 
				&& IMemoryDomain.NAME_GRAPHICS.equals(properties.get(MemoryEntryInfo.DOMAIN)))) {
			// ignore
			return;
		}
		if ((MD5FilterAlgorithms.ALGORITHM_FULL.equals(alg) 
				&& false == IMemoryDomain.NAME_GRAPHICS.equals(properties.get(MemoryEntryInfo.DOMAIN)))) {
			// ignore
			return;
		}
		entry.setAttribute(propName, alg);
	}

	private void getClassAttribute(Element el, String name, Class<?> baseKlass,
			MemoryEntryInfo info) {

		if (el.hasAttribute(name)) {
			Class<?> klass;
			
			String klassName = el.getAttribute(name);
			try {
				klass = ModuleDatabase.class.getClassLoader().loadClass(klassName);
				if (!baseKlass.isAssignableFrom(klass)) {
					throw new AssertionError("Illegal class: wanted instance of " + baseKlass + " but got " + klass);
				} else {
					info.getProperties().put(name, klass);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	public void getStringAttribute(Element el, String name, MemoryEntryInfo info) {
		if (el.hasAttribute(name)) {
			String attr = el.getAttribute(name);
			info.getProperties().put(name, attr);
		}
	}
	public void getIntAttribute(Element el, String name, MemoryEntryInfo info) {
		if (el.hasAttribute(name)) {
			String attr = el.getAttribute(name);
			info.getProperties().put(name, HexUtils.parseInt(attr));
		}
	}
	public void getBooleanAttribute(Element el, String name, MemoryEntryInfo info) {
		if (el.hasAttribute(name)) {
			String attr = el.getAttribute(name);
			info.getProperties().put(name, "true".equals(attr));
		}
	}
	
	
}
