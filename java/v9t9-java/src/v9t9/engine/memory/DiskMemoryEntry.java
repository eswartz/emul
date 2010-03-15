/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.engine.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.coffee.core.properties.IPropertyStorage;

import v9t9.engine.files.DataFiles;

/**
 * @author ejs
 */
public class DiskMemoryEntry extends MemoryEntry {

    /*	file path */
    private String filepath;
    
    /*	file offset in bytes */
    public int fileoffs;
    
    /*	actual size of file */
    public int filesize;
    
    /*	is this nonvolatile RAM? */
    public boolean bStorable;

    /*	is the file loaded yet? */
    public boolean bLoaded;
    
    /*	is the data dirty? */
    private boolean bDirty;
    
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
    static public DiskMemoryEntry newWordMemoryFromFile(
            int addr, int size, String name, 
            MemoryDomain domain, String filepath, int fileoffs,
            boolean isStored) throws IOException {
    	
    	WordMemoryArea area;
    	
    	// placeholder
    	area = new WordMemoryArea();
    	
        DiskMemoryEntry entry = newFromFile(area, addr, size, name, domain, filepath, fileoffs, isStored);
        
        entry.area = createWordMemoryArea(domain, addr, entry.size, isStored);
        return entry;
    }

	private static WordMemoryArea createWordMemoryArea(MemoryDomain domain, int addr,
			int size, boolean isStored) {
		WordMemoryArea area;
        
        if (!isStored) {
			area = new WordMemoryArea();
		} else {
			area = new WordMemoryArea() {
	    		public void writeByte(MemoryEntry entry, int addr, byte val) {
	    			super.writeByte(entry, addr, val);
	    			((DiskMemoryEntry) entry).setDirty(true);
	    		}
	    		@Override
	    		public void writeWord(MemoryEntry entry, int addr, short val) {
	    			super.writeWord(entry, addr, val);
	    			((DiskMemoryEntry) entry).setDirty(true);
	    		}
    		};
		}
        area.setLatency(domain.getLatency(addr));
        area.memory = new short[size / 2];
        area.read = area.memory;
        if (isStored)
        	area.write = area.memory;
        if (isStored) {
        	area.write = area.memory;
        }

		return area;
	}

	/**
     * Read memory and create a memory entry with no byte ordering adjustment.
     * @param addr
     * @param size	the expected size of the entry (a maximum if != 0, else for 0, the
     * actual size is used)
     * @param name
     * @param domain
     * @param filepath
     * @param fileoffs
     * @param isStored if true, this is a RAM entry which can be rewritten
     * @return the entry
     * @throws IOException if the memory cannot be read and is not stored
     */
    static public DiskMemoryEntry newByteMemoryFromFile(
            int addr, int size, String name, 
            MemoryDomain domain, String filepath, int fileoffs,
            boolean isStored) throws IOException {
    	
    	
    	ByteMemoryArea area = new ByteMemoryArea();
    	
        DiskMemoryEntry entry = newFromFile(area, addr, size, name, domain, filepath, fileoffs, isStored);
        
        entry.area = createByteMemoryArea(domain, addr, entry.size, isStored);
       
        return entry;
    }


	private static ByteMemoryArea createByteMemoryArea(MemoryDomain domain, int addr,
			int size, boolean isStored) {
		ByteMemoryArea area;
        
		if (!isStored) {
			area = new ByteMemoryArea();
		} else {
			area = new ByteMemoryArea() {
	    		public void writeByte(MemoryEntry entry, int addr, byte val) {
	    			super.writeByte(entry, addr, val);
	    			((DiskMemoryEntry) entry).setDirty(true);
	    		}
    		};
		}
    	
		area.setLatency(domain.getLatency(addr));
		area.memory = new byte[size];
		area.read = area.memory;
		if (isStored)
			area.write = area.memory;

		return area;
	}

    /** Construct a DiskMemoryEntry based on the file length.
     * @param size if not isRam, the maximum acceptable size,
     * else the fixed size for a RAM file.  May be 0 to use
     * file size directly.
     * @param isStored if true, this is a RAM entry which can be rewritten
     * @return the entry
     * @throws IOException if the memory cannot be read and is not stored
     */
    static private DiskMemoryEntry newFromFile(
            MemoryArea area, int addr, int size, String name, 
            MemoryDomain domain, String filepath, int fileoffs,
            boolean isStored) throws IOException {

        if (size < -MemoryDomain.PHYSMEMORYSIZE
                || isStored && size <= 0
                || isStored && fileoffs != 0
                ) {
			throw new AssertionError();
		}
        
        int filesize = 0;	
        try {
	        filesize = DataFiles.getImageSize(filepath);
	        
			/* for large files selected, e.g., by accident */
			if (size > 0 && filesize > size) {
				filesize = size;
			} else if (size < 0 && filesize > -size) {
				filesize = -size;
			} else if (filesize + addr > 0x10000) {
				filesize = 0x10000 - addr;
			}
            
            // for files too large
            if (filesize > 0x2000 && (filesize & 0x1fff) != 0) {
				filesize &= ~0x1fff;
			}
            
        } catch (IOException e) {		// TODO
            if (isStored) {
				filesize = size;	/* not created yet */
			} else {
				throw e;
			}
        }
        
        if (size == 0)
        	size = filesize;
        
        DiskMemoryEntry entry = new DiskMemoryEntry(area, addr, size, name, domain, filepath, fileoffs, filesize, isStored);
        
        return entry;
    }
    
    public DiskMemoryEntry() {
    	// only to be used when reconstructing
    	super();
    }
    DiskMemoryEntry(MemoryArea area, int addr, int size, String name,
            MemoryDomain domain, 
            String filepath, int fileoffs, int filesize,
            boolean isStorable) {
        super(name, domain, addr, size, area);
        
        /* this should be set up already */
        if (area == null) {
			throw new AssertionError();
		}
        
        this.filepath = filepath;
        this.fileoffs = fileoffs;
        this.filesize = filesize;
        this.bStorable = isStorable;
        this.bDirty = false;
        this.area = area;
    }
    
    /* (non-Javadoc)
     * @see v9t9.MemoryEntry#load()
     */
    @Override
	public void load() {
        super.load();
        if (!bLoaded) {
            try {
                byte[] data = new byte[filesize];
                DataFiles.readMemoryImage(filepath, fileoffs, filesize, data);
                area.copyFromBytes(data);
               
                
                // see if it has symbols
                String symbolfilepath = getSymbolFilepath();
                File symfile = DataFiles.resolveFile(symbolfilepath);
            	if (symfile.exists()) {
            		loadSymbols(new FileInputStream(symfile));
            	}
            } catch (java.io.IOException e) {
                // TODO: send alert
            } finally {
            	bLoaded = true;
            }
        }
    }

    public void setDirty(boolean dirty) {
    	bDirty = dirty;
    }
    
    /* (non-Javadoc)
     * @see v9t9.MemoryEntry#save()
     */
    @Override
	public void save() throws IOException {
        if (bStorable && bDirty) {
            byte[] data = new byte[filesize];
            area.copyToBytes(data);
            DataFiles.writeMemoryImage(filepath, filesize, data);
            bDirty = false;
        }
        super.save();
    }
    
    @Override
	public void unload() {
        super.unload();
        bLoaded = false;
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
	static public BankedMemoryEntry newBankedWordMemoryFromFile(
			Class<? extends BankedMemoryEntry> klass,
			int addr,
	        int size, 
	        Memory memory, 
	        String name, MemoryDomain domain,
	        String filepath, int fileoffs,
	        String filepath2, int fileoffs2) throws IOException {
		DiskMemoryEntry bank0 = newWordMemoryFromFile(
				addr, size, name + " (bank 0)", domain, filepath, fileoffs, false);
		DiskMemoryEntry bank1 = newWordMemoryFromFile(
				addr, size, name + " (bank 1)", domain, filepath2, fileoffs2, false);
		
		MemoryEntry[] entries = new MemoryEntry[] { bank0, bank1 };
		BankedMemoryEntry bankedMemoryEntry;
		try {
			bankedMemoryEntry = klass.getConstructor(
					Memory.class, String.class, entries.getClass()).newInstance(
							memory, name, entries);
		} catch (Exception e) {
			throw new IOException(e);
		}
		return bankedMemoryEntry;
	}

    /**
     * Create a memory entry for banked (ROM) memory.
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
	static public BankedMemoryEntry newBankedWordMemoryFromFile(
			int addr,
	        int size, 
	        Memory memory, 
	        String name, MemoryDomain domain,
	        String filepath, int fileoffs,
	        String filepath2, int fileoffs2) throws IOException {
		DiskMemoryEntry bank0 = newWordMemoryFromFile(
				addr, size, name + " (bank 0)", domain, filepath, fileoffs, false);
		DiskMemoryEntry bank1 = newWordMemoryFromFile(
				addr, size, name + " (bank 1)", domain, filepath2, fileoffs2, false);
		
		BankedMemoryEntry bankedMemoryEntry = new MultiBankedMemoryEntry(
				memory, name, new MemoryEntry[] { bank0, bank1 });
		return bankedMemoryEntry;
	}

    /**
     * Create a memory entry for banked (ROM) memory that toggles based
     * on the address written.
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
	static public BankedMemoryEntry newWriteTogglingBankedWordMemoryFromFile(
			int addr,
	        int size, 
	        Memory memory, 
	        String name, MemoryDomain domain,
	        String filepath, int fileoffs,
	        String filepath2, int fileoffs2) throws IOException {
		DiskMemoryEntry bank0 = newWordMemoryFromFile(
				addr, size, name + " (bank 0)", domain, filepath, fileoffs, false);
		DiskMemoryEntry bank1 = newWordMemoryFromFile(
				addr, size, name + " (bank 1)", domain, filepath2, fileoffs2, false);
		
		BankedMemoryEntry bankedMemoryEntry = new StdMultiBankedMemoryEntry(memory, name, new MemoryEntry[] { bank0, bank1 });
		
		return bankedMemoryEntry;
	}

	public String getFilepath() {
		return filepath;
	}
	
	public String getSymbolFilepath() {
		int idx = filepath.lastIndexOf('.');
        if (idx >= 0) {
        	return filepath.substring(0, idx) + ".sym";
        } else {
        	return filepath + ".sym";
        }
	}
	
	@Override
	public void saveState(IPropertyStorage section) {
		super.saveState(section);
		section.put("FilePath", filepath);
		section.put("FileOffs", fileoffs);
		section.put("FileSize", filesize);
		section.put("Storable", bStorable);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#loadFields(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
	protected void loadFields(IPropertyStorage section) {
		super.loadFields(section);
		filepath = section.get("FilePath");
		fileoffs = section.getInt("FileOffs");
		filesize = section.getInt("FileSize");
		bStorable = section.getBoolean("Storable");
	}
	
	protected void loadMemoryContents(IDialogSettings section) {
		if (bWordAccess)
			area = createWordMemoryArea(domain, addr, size, bStorable);
		else
			area = createByteMemoryArea(domain, addr, size, bStorable);
		
		bLoaded = false;
		load();
	}
	@Override
	public String getUniqueName() {
		return filepath;
	}
}
