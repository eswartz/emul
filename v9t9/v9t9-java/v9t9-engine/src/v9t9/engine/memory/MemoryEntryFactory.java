/**
 * 
 */
package v9t9.engine.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.HexUtils;
import ejs.base.utils.XMLUtils;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.IMemoryEntryFactory;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.memory.StoredMemoryEntryInfo;
import v9t9.engine.modules.ModuleLoader;

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
		MemoryArea area;
		IMemoryEntry entry;
    	
    	if (info.getFilename() != null) {
    		if (info.isByteSized())
        		area = new ByteMemoryArea();
        	else
        		area = new WordMemoryArea();
    		entry = newFromFile(info, area);
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
    		entry = new MemoryEntry(info.getName(), info.getDomain(memory), 
    				info.getAddress(), size, area);
    	}
        
        return entry;
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
		
		IMemoryEntry bank0 = newFromFile(info, info.getName() + " (bank 0)", 
				info.getFilename(), info.getOffset(), MemoryAreaFactory.createMemoryArea(memory, info));
		IMemoryEntry bank1 = newFromFile(info, info.getName() + " (bank 1)", 
				info.getFilename2(), info.getOffset2(), MemoryAreaFactory.createMemoryArea(memory, info));
		
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


    /** Construct a DiskMemoryEntry based on the file length.
     * @throws IOException if the memory cannot be read and is not stored
     */
    private DiskMemoryEntry newFromFile(MemoryEntryInfo info, MemoryArea area) throws IOException {
    	return newFromFile(info, info.getName(), info.getFilename(), info.getOffset(), area);
    }
    
    /** Construct a DiskMemoryEntry based on the file length.
     * @return the entry
     * @throws IOException if the memory cannot be read and is not stored
     */
    private DiskMemoryEntry newFromFile(MemoryEntryInfo info, String name, String filename, int offset, MemoryArea area) throws IOException {
    	
    	StoredMemoryEntryInfo storedInfo = resolveMemoryEntry(info, name, filename, offset);
    	
    	DiskMemoryEntry entry = new DiskMemoryEntry(info, name, area, storedInfo);
    	
    	//info.getProperties().put(MemoryEntryInfo.SIZE, entry.getSize());
    	
        entry.setArea(MemoryAreaFactory.createMemoryArea(memory, info)); 
        
    	return entry;
    }

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.IMemoryEntryFactory#resolveMemoryEntry(v9t9.common.modules.MemoryEntryInfo, java.lang.String, java.lang.String, int)
	 */
	@Override
	public StoredMemoryEntryInfo resolveMemoryEntry(
			MemoryEntryInfo info,
			String name,
			String filename,
			int fileoffs) throws IOException {

		return StoredMemoryEntryInfo.createStoredMemoryEntryInfo(
				locator, settings, memory, 
				info, name, filename, fileoffs);
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
				}
				else if (el.getNodeName().equals("bankedModuleEntry")) {
					properties.put(MemoryEntryInfo.DOMAIN, IMemoryDomain.NAME_CPU);
					properties.put(MemoryEntryInfo.ADDRESS, 0x6000);
					properties.put(MemoryEntryInfo.SIZE, -0x2000);
					
					if ("true".equals(el.getAttribute("custom"))) {
						properties.put(MemoryEntryInfo.CLASS, BankedMemoryEntry.class);
					} else {
						properties.put(MemoryEntryInfo.CLASS, StdMultiBankedMemoryEntry.class);
					}
				}
				else if (!el.getNodeName().equals("memoryEntry")) {
					System.err.println("Unknown entry: " + el.getNodeName());
					continue;
				}
				
				getStringAttribute(el, MemoryEntryInfo.FILENAME, info);
				getStringAttribute(el, MemoryEntryInfo.FILENAME2, info);
				getStringAttribute(el, MemoryEntryInfo.DOMAIN, info);
				getIntAttribute(el, MemoryEntryInfo.ADDRESS, info);
				getIntAttribute(el, MemoryEntryInfo.SIZE, info);
				getIntAttribute(el, MemoryEntryInfo.OFFSET, info);
				getBooleanAttribute(el, MemoryEntryInfo.STORED, info);
				getClassAttribute(el, MemoryEntryInfo.CLASS, MemoryEntry.class, info);

				memoryEntries.add(info);
			}
		}
		
		return memoryEntries;
	}
	
	private void getClassAttribute(Element el, String name, Class<?> baseKlass,
			MemoryEntryInfo info) {

		if (el.hasAttribute(name)) {
			Class<?> klass;
			
			String klassName = el.getAttribute(name);
			try {
				klass = ModuleLoader.class.getClassLoader().loadClass(klassName);
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

}
