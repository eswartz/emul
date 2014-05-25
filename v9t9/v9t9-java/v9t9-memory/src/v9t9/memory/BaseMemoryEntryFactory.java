/*
  MemoryEntryFactory.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;
import ejs.base.utils.XMLUtils;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IPathFileLocator;
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
public abstract class BaseMemoryEntryFactory implements IMemoryEntryFactory {
	
	protected IPathFileLocator locator;
	protected final IMemory memory;
	protected final ISettingsHandler settings;

	public BaseMemoryEntryFactory(IMemory memory, ISettingsHandler settings, IPathFileLocator locator) {
		this.memory = memory;
		this.settings = settings;
		this.locator = locator;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryEntryFactory#getMemory()
	 */
	@Override
	public IMemory getMemory() {
		return memory;
	}

    /* (non-Javadoc)
	 * @see v9t9.engine.memory.IMemoryEntryFactory#newMemoryEntry(v9t9.common.modules.MemoryEntryInfo)
	 */
    @Override
	public IMemoryEntry newMemoryEntry(MemoryEntryInfo info) throws IOException {
    	if (!info.isBanked())
    		return newSimpleMemoryEntry(info);
    	else
    		throw new IOException("banked entries not handled");
    	
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
    	
    	if (info.getResolvedFilename(settings) != null) {
    		if (info.isByteSized())
        		area = new ByteMemoryArea();
        	else
        		area = new WordMemoryArea();
    	}
    	else {
    		int size = Math.abs(info.getSize());
			if (info.isByteSized())
        		area = new ByteMemoryArea(info.getDomain(memory).getLatency(info.getAddress()),
        			new byte[size]	
        			);
        	else
        		area = new WordMemoryArea(info.getDomain(memory).getLatency(info.getAddress()),
            			new short[size / 2]	
            			);
    	}
        
        return area;
	}


	/** 
	 * Construct a DiskMemoryEntry 
     * @throws IOException if the memory cannot be read and is not stored
     */
    protected DiskMemoryEntry newFromFile(MemoryEntryInfo info, MemoryArea area) throws IOException {
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
	public IMemoryEntry createEntry(IMemoryDomain domain, ISettingSection entryStore) {
		MemoryEntry entry = null;
		String klazzName = entryStore.get("Class");
		if (klazzName != null) {
			Class<?> klass;
			try {
				klass = Class.forName(klazzName);
				
				entry = (MemoryEntry) klass.newInstance();
			} catch (Exception e) {
				klass = restoreMovedMemoryEntryClass(klazzName);
				
				if (klass == null) {
					e.printStackTrace();
					return null;
				}

				try {
					entry = (MemoryEntry) klass.newInstance();
				} catch (Exception e2) {
					e2.printStackTrace();
					return null;
					
				}
			}
			
			entry.setLocator(locator);
			entry.setDomain(domain);
			entry.setWordAccess(domain.getIdentifier().equals(IMemoryDomain.NAME_CPU));	// TODO
			int latency = domain.getLatency(entryStore.getInt("Address"));
			if (entry.isWordAccess())
				entry.setArea(new WordMemoryArea(latency));
			else
				entry.setArea(new ByteMemoryArea(latency));
			
			entry.setMemory(domain.getMemory());
			
			entry.loadState(entryStore);
		}
		return entry;
	}

	
	/**
	 * @param klazzName
	 * @return
	 */
	protected Class<? extends MemoryEntry> restoreMovedMemoryEntryClass(String klazzName) {
		Class<? extends MemoryEntry> klass;
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
			return null;
		}
		return klass;
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
				
				if (!el.getNodeName().equals("memoryEntry")) {
					// helpers
					if (!handleCustomLoadEntry(el, properties)) {
						System.err.println("Unknown entry: " + el.getNodeName());
						continue;
					}
				}
				
				getStringAttribute(el, MemoryEntryInfo.FILENAME, info);
				getStringAttribute(el, MemoryEntryInfo.FILENAME2, info);
				getStringAttribute(el, MemoryEntryInfo.FILE_MD5, info);
				getIntAttribute(el, MemoryEntryInfo.FILE_MD5_LIMIT, info);
				getStringAttribute(el, MemoryEntryInfo.FILE2_MD5, info);
				getIntAttribute(el, MemoryEntryInfo.FILE2_MD5_LIMIT, info);
				getStringAttribute(el, MemoryEntryInfo.DOMAIN, info);
				getIntAttribute(el, MemoryEntryInfo.ADDRESS, info);
				getIntAttribute(el, MemoryEntryInfo.SIZE, info);
				getIntAttribute(el, MemoryEntryInfo.OFFSET, info);
				getIntAttribute(el, MemoryEntryInfo.OFFSET2, info);
				getBooleanAttribute(el, MemoryEntryInfo.STORED, info);
				getClassAttribute(el, MemoryEntryInfo.CLASS, MemoryEntry.class, info);
				getBooleanAttribute(el, MemoryEntryInfo.REVERSED, info);

				memoryEntries.add(info);
			}
		}
		
		return memoryEntries;
	}
	

	protected abstract boolean handleCustomLoadEntry(Element el, Map<String, Object> properties);

	protected static class SaveEntryInfo {
		public Map<String, Object> properties;
		public Element entry = null;
		
		public boolean needAddress = true;
		public boolean needSize = true;
		public boolean needDomain = true;
		public Class<?> cls;
		
		public boolean isBanked;
		
		public Integer offset;
		public Integer size;

		public SaveEntryInfo(Map<String, Object> properties) {
			this.properties = properties;
			cls = (Class<?>)properties.get(MemoryEntryInfo.CLASS);
			
			isBanked =
				(cls != null && BankedMemoryEntry.class.isAssignableFrom(cls))
				|| (cls == null && (properties.containsKey(MemoryEntryInfo.FILENAME2)
						|| properties.containsKey(MemoryEntryInfo.FILE2_MD5)));
			
			offset = (Integer) properties.get(MemoryEntryInfo.OFFSET);
			size = (Integer) properties.get(MemoryEntryInfo.SIZE);
		}

	}

	public void saveEntriesTo(Collection<MemoryEntryInfo> memoryEntries, Element root) {
		Element memoryEntriesEl = root.getOwnerDocument().createElement("memoryEntries");
		root.appendChild(memoryEntriesEl);
		
		for (MemoryEntryInfo info : memoryEntries) {
			
			Map<String, Object> properties = info.getProperties();
			SaveEntryInfo saveInfo = new SaveEntryInfo(properties);
			
			if (!handleCustomSaveEntry(root.getOwnerDocument(), saveInfo)) {
				saveInfo.entry = root.getOwnerDocument().createElement("memoryEntry");
			}

			if (saveInfo.needDomain) {
				saveInfo.entry.setAttribute(MemoryEntryInfo.DOMAIN, ""+properties.get(MemoryEntryInfo.DOMAIN));
			}
			if (saveInfo.needAddress) {
				saveInfo.entry.setAttribute(MemoryEntryInfo.ADDRESS, 
						"0x" + HexUtils.toHex4(((Number) properties.get(MemoryEntryInfo.ADDRESS)).intValue()));
			}
			if (saveInfo.needSize && saveInfo.size != null) {
				if (saveInfo.size < 0) {
					saveInfo.entry.setAttribute(MemoryEntryInfo.SIZE, Integer.toString(saveInfo.size));
				} else {
					saveInfo.entry.setAttribute(MemoryEntryInfo.SIZE, 
							"0x" + HexUtils.toHex4(saveInfo.size));
				}
			}
			if (properties.containsKey(MemoryEntryInfo.FILENAME))
				saveInfo.entry.setAttribute(MemoryEntryInfo.FILENAME, properties.get(MemoryEntryInfo.FILENAME).toString());
			if (properties.containsKey(MemoryEntryInfo.FILE_MD5))
				saveInfo.entry.setAttribute(MemoryEntryInfo.FILE_MD5, properties.get(MemoryEntryInfo.FILE_MD5).toString());
			
			if (properties.containsKey(MemoryEntryInfo.FILENAME2))
				saveInfo.entry.setAttribute(MemoryEntryInfo.FILENAME2, properties.get(MemoryEntryInfo.FILENAME2).toString());
			if (properties.containsKey(MemoryEntryInfo.FILE2_MD5))
				saveInfo.entry.setAttribute(MemoryEntryInfo.FILE2_MD5, properties.get(MemoryEntryInfo.FILE2_MD5).toString());
			
			if (properties.containsKey(MemoryEntryInfo.OFFSET) && ((Number) properties.get(MemoryEntryInfo.OFFSET)).intValue() != 0)
				saveInfo.entry.setAttribute(MemoryEntryInfo.OFFSET, 
						"0x" + HexUtils.toHex4(((Number) properties.get(MemoryEntryInfo.OFFSET)).intValue()));
			if (info.isStored())
				saveInfo.entry.setAttribute(MemoryEntryInfo.STORED, "true");
			if (info.isBanked() && info.isReversed())
				saveInfo.entry.setAttribute(MemoryEntryInfo.REVERSED, "true");

			memoryEntriesEl.appendChild(saveInfo.entry);
		}
	}

	abstract protected boolean handleCustomSaveEntry(Document document, SaveEntryInfo saveInfo);


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
