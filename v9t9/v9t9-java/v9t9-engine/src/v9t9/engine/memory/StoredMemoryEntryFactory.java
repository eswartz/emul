/**
 * 
 */
package v9t9.engine.memory;

import java.io.IOException;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.PathFileLocator;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.StoredMemoryEntryInfo;
import v9t9.common.modules.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public class StoredMemoryEntryFactory {

	private static StoredMemoryEntryFactory INSTANCE;
	public static void setupInstance(ISettingsHandler settings, IMemory memory) {
		PathFileLocator.setupInstance(settings);
		INSTANCE = new StoredMemoryEntryFactory(settings, memory);
	}
	
	public static StoredMemoryEntryFactory getInstance() {
		return INSTANCE;
	}
	
	private PathFileLocator locator;
	private final IMemory memory;
	private final ISettingsHandler settings;

	public StoredMemoryEntryFactory(ISettingsHandler settings, IMemory memory) {
		this.settings = settings;
		this.memory = memory;
		this.locator = PathFileLocator.getInstance();
	}
	

    /**
     * Read memory and create a memory entry with CPU byte ordering.
     * @param addr
     * @param size	the expected size of the entry (a maximum if != 0, else for 0, the
     * actual size is used)
     * @param name
     * @param domain
     * @param filepath
     * @param fileoffs
     * @param isStored if true, this is a RAM entry which can be rewritten
     * @return new entry
     * @throws IOException if file cannot be read, and is not stored
     */
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
    	
    	if (info.getFilename().length() > 0) {
    		if (info.isByteSized())
        		area = new ByteMemoryArea();
        	else
        		area = new WordMemoryArea();
    		entry = newFromFile(info, area);
    	}
    	else {
    		if (info.isByteSized())
        		area = new ByteMemoryArea(info.getDomain(memory).getLatency(info.getAddress()),
        			new byte[info.getSize()]	
        			);
        	else
        		area = new WordMemoryArea(info.getDomain(memory).getLatency(info.getAddress()),
            			new short[info.getSize() / 2]	
            			);
    		entry = new MemoryEntry(info.getName(), info.getDomain(memory), 
    				info.getAddress(), info.getSize(), area);
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
    	
    	info.getProperties().put(MemoryEntryInfo.SIZE, entry.getSize());
    	
        entry.setArea(MemoryAreaFactory.createMemoryArea(memory, info)); 
        
    	return entry;
    }

	public StoredMemoryEntryInfo resolveMemoryEntry(
			MemoryEntryInfo info,
			String name,
			String filename,
			int fileoffs) throws IOException {

		return StoredMemoryEntryInfo.resolveStoredMemoryEntryInfo(
				locator, settings, memory, 
				info, name, filename, fileoffs);
	}
    

}
