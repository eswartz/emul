/*
  MemoryEntryFactory.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.memory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.memory.StoredMemoryEntryInfo;
import v9t9.machine.ti99.memory.mmio.StdMultiBankedMemoryEntry;
import v9t9.memory.BankedMemoryEntry;
import v9t9.memory.BaseMemoryEntryFactory;
import v9t9.memory.MemoryAreaFactory;
import v9t9.memory.MemoryEntry;
import ejs.base.utils.HexUtils;

/**
 * This factory assists in creating {@link IMemoryEntry} instances.
 * @author ejs
 *
 */
public class TI994AMemoryEntryFactory extends BaseMemoryEntryFactory {
	
	public TI994AMemoryEntryFactory(IMemory memory, ISettingsHandler settings, IPathFileLocator locator) {
		super(memory, settings, locator);
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
    	
    	IMemoryEntry[] entries = new IMemoryEntry[numBanks];
    	
    	boolean reversed = storedInfo.info.isReversed();
    	
    	for (int bank = 0; bank < entries.length; bank++) {
    		int entryIdx = reversed ? entries.length - bank - 1 : bank;
    		entries[entryIdx] = newFromFile(info.asBank(entryIdx, info.getOffset() + bank * bankSize), 
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


	/* (non-Javadoc)
	 * @see v9t9.memory.BaseMemoryEntryFactory#handleCustomNode(org.w3c.dom.Element, java.util.Map)
	 */
	@Override
	protected boolean handleCustomLoadEntry(Element el,
			Map<String, Object> properties) {
		if (el.getNodeName().equals("romModuleEntry")) {
			properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_CPU);
			properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
			properties.put(MemoryEntryInfo.SIZE, 0x2000);
		}
		else if (el.getNodeName().equals("gromModuleEntry")) {
			properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_GRAPHICS);
			properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
			properties.put(MemoryEntryInfo.SIZE, -0xA000);
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
		else {
			return false;
		}
		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.memory.BaseMemoryEntryFactory#handleCustomSaveEntry(v9t9.memory.BaseMemoryEntryFactory.SaveEntryInfo)
	 */
	@Override
	protected boolean handleCustomSaveEntry(Document document, SaveEntryInfo saveInfo) {

		Map<String, Object> properties = saveInfo.properties;
		
		if (IMemoryDomain.NAME_CPU.equals(properties.get(MemoryEntryInfo.DOMAIN))
				&& (Integer) properties.get(MemoryEntryInfo.ADDRESS) == 0x6000
				&& (saveInfo.size == null || saveInfo.size == 0x2000)
				&& (saveInfo.offset == null || saveInfo.offset == 0)
				&& !saveInfo.isBanked) {
			saveInfo.entry = document.createElement("romModuleEntry");
			saveInfo.needAddress = saveInfo.needSize = saveInfo.needDomain = false;
		}
		else if (IMemoryDomain.NAME_GRAPHICS.equals(properties.get(MemoryEntryInfo.DOMAIN))
				&& (Integer) properties.get(MemoryEntryInfo.ADDRESS) == 0x6000) {
			saveInfo.entry = document.createElement("gromModuleEntry");
			saveInfo.needAddress = saveInfo.needDomain = false;
		} else if (IMemoryDomain.NAME_CPU.equals(properties.get(MemoryEntryInfo.DOMAIN))
				&& (Integer) properties.get(MemoryEntryInfo.ADDRESS) == 0x6000
				&& saveInfo.isBanked) {
			saveInfo.entry = document.createElement("bankedModuleEntry");
			saveInfo.needAddress = saveInfo.needDomain = false;
			if ((saveInfo.size == null || saveInfo.size == 0x2000)
					&& (saveInfo.offset == null || saveInfo.offset == 0))
				saveInfo.needSize = false;
			
			if (saveInfo.cls == null || BankedMemoryEntry.class.equals(saveInfo.cls))
				saveInfo.entry.setAttribute("custom", "true");
		}
		else {
			return false;
		}
		
		return true;
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
					&& (size == null || size == 0x2000)
					&& (offset == null || offset == 0)
					&& !isBanked) {
				entry = root.getOwnerDocument().createElement("romModuleEntry");
				needAddress = needSize = needDomain = false;
			}
			else if (IMemoryDomain.NAME_GRAPHICS.equals(properties.get(MemoryEntryInfo.DOMAIN))
					&& (Integer) properties.get(MemoryEntryInfo.ADDRESS) == 0x6000) {
				entry = root.getOwnerDocument().createElement("gromModuleEntry");
				needAddress = needDomain = false;
			} else {
				if (IMemoryDomain.NAME_CPU.equals(properties.get(MemoryEntryInfo.DOMAIN))
						&& (Integer) properties.get(MemoryEntryInfo.ADDRESS) == 0x6000
						&& isBanked) {
					entry = root.getOwnerDocument().createElement("bankedModuleEntry");
					needAddress = needDomain = false;
					if ((size == null || size == 0x2000)
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
			
			if (properties.containsKey(MemoryEntryInfo.FILENAME2))
				entry.setAttribute(MemoryEntryInfo.FILENAME2, properties.get(MemoryEntryInfo.FILENAME2).toString());
			if (properties.containsKey(MemoryEntryInfo.FILE2_MD5))
				entry.setAttribute(MemoryEntryInfo.FILE2_MD5, properties.get(MemoryEntryInfo.FILE2_MD5).toString());
			
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

	/* (non-Javadoc)
	 * @see v9t9.memory.BaseMemoryEntryFactory#newMemoryEntry(v9t9.common.memory.MemoryEntryInfo)
	 */
	@Override
	public IMemoryEntry newMemoryEntry(MemoryEntryInfo info) throws IOException {
		if (info.isBanked())
			return newBankedMemoryFromFile(info);
		return super.newMemoryEntry(info);
	}

	/* (non-Javadoc)
	 * @see v9t9.memory.BaseMemoryEntryFactory#restoreMovedMemoryEntryClass(java.lang.String)
	 */
	@Override
	protected Class<? extends MemoryEntry> restoreMovedMemoryEntryClass(
			String klazzName) {
		if (klazzName.endsWith(".StdMultiBankedMemoryEntry"))
			return StdMultiBankedMemoryEntry.class;
		return super.restoreMovedMemoryEntryClass(klazzName);
	}
}
