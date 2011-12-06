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
import java.util.Arrays;


import v9t9.base.settings.ISettingSection;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.DataFiles;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;

/**
 * @author ejs
 */
public class DiskMemoryEntry extends MemoryEntry {

    /**	file path */
    private String filepath;
    
    /**	file offset in bytes */
    private int fileoffs;
    
    /**	actual size of file */
    private int filesize;
    
    /**	is this nonvolatile RAM which should be saved back to disk? */
    private boolean bStorable;

    /**	is the file loaded yet? */
    private boolean bLoaded;
    
    /**	is the data dirty? */
    private boolean bDirty;

	private ISettingsHandler settings;
    
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
    		ISettingsHandler settings,
            int addr, int size, String name, 
            IMemoryDomain domain, String filepath, int fileoffs,
            boolean isStored) throws IOException {
    	
    	WordMemoryArea area;
    	
    	// placeholder
    	area = new WordMemoryArea();
    	
        DiskMemoryEntry entry = newFromFile(settings, area, addr, size, name, domain, filepath, fileoffs, isStored);
        
        entry.area = createWordMemoryArea(domain, addr, entry.getSize(), isStored);
        return entry;
    }

	private static WordMemoryArea createWordMemoryArea(IMemoryDomain domain, int addr,
			int size, boolean isStored) {
		WordMemoryArea area;
        
        if (!isStored) {
			area = new WordMemoryArea();
		} else {
			area = new WordMemoryArea() {
	    		public void writeByte(IMemoryEntry entry, int addr, byte val) {
	    			super.writeByte(entry, addr, val);
	    			((DiskMemoryEntry) entry).setDirty(true);
	    		}
	    		@Override
	    		public void writeWord(IMemoryEntry entry, int addr, short val) {
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
    		ISettingsHandler settings,
            int addr, int size, String name, 
            IMemoryDomain domain, String filepath, int fileoffs,
            boolean isStored) throws IOException {
    	
    	if (domain == null)
    		throw new IOException("no memory domain to load: " + name);
    	
    	ByteMemoryArea area = new ByteMemoryArea();
    	
        DiskMemoryEntry entry = newFromFile(settings, area, addr, size, name, domain, filepath, fileoffs, isStored);
        
        entry.area = createByteMemoryArea(domain, addr, entry.getSize(), isStored);
       
        return entry;
    }


	private static ByteMemoryArea createByteMemoryArea(IMemoryDomain domain, int addr,
			int size, boolean isStored) {
		ByteMemoryArea area;
        
		if (!isStored) {
			area = new ByteMemoryArea();
		} else {
			area = new ByteMemoryArea() {
	    		public void writeByte(IMemoryEntry entry, int addr, byte val) {
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
     * @param settings TODO
     * @param size if not isRam, the maximum acceptable size,
     * else the fixed size for a RAM file.  May be 0 to use
     * file size directly.
     * @param isStored if true, this is a RAM entry which can be rewritten
     * @return the entry
     * @throws IOException if the memory cannot be read and is not stored
     */
    static private DiskMemoryEntry newFromFile(
            ISettingsHandler settings, MemoryArea area, int addr, int size, 
            String name, IMemoryDomain domain, String filepath,
            int fileoffs, boolean isStored) throws IOException {

        if (size < -IMemoryDomain.PHYSMEMORYSIZE
                || isStored && size <= 0
                || isStored && fileoffs != 0
                ) {
			throw new AssertionError();
		}
        
        int filesize = 0;	
        try {
	        filesize = DataFiles.getImageSize(settings, filepath);
	        
			/* for large files selected, e.g., by accident */
			if (size > 0 && filesize > size) {
				filesize = size;
			} else if (size < 0 && filesize > -size) {
				filesize = -size;
			} else if (filesize + addr > 0x10000) {
				filesize = 0x10000 - addr;
			}
            
            // for files too large
            /*if (filesize > 0x2000 && (filesize & 0x1fff) != 0) {
				filesize &= ~0x1fff;
			}*/
            
        } catch (IOException e) {		// TODO
            if (isStored) {
				filesize = size;	/* not created yet */
			} else {
				throw e;
			}
        }
        
        if (size == 0)
        	size = filesize - fileoffs;
        
        DiskMemoryEntry entry = new DiskMemoryEntry(settings, area, addr, size, name, domain, filepath, fileoffs, filesize, isStored);
        
        return entry;
    }
    
    public DiskMemoryEntry() {
    	// only to be used when reconstructing
    	super();
    }
    DiskMemoryEntry(ISettingsHandler settings, MemoryArea area, int addr, int size,
            String name, 
            IMemoryDomain domain, String filepath, int fileoffs,
            int filesize, boolean isStorable) {
        super(name, domain, addr, size, area);
		this.settings = settings;
        
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
     * @see v9t9.common.memory.MemoryEntry#isVolatile()
     */
    @Override
    public boolean isVolatile() {
    	// don't destroy, ever
    	return false;
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
                DataFiles.readMemoryImage(settings, filepath, fileoffs, filesize, data);
                area.copyFromBytes(data);
               
                
                // see if it has symbols
                String symbolfilepath = getSymbolFilepath();
                File symfile = DataFiles.resolveFile(settings, symbolfilepath);
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
            File old = DataFiles.resolveFile(settings, filepath);
            File backup = DataFiles.resolveFile(settings, filepath + "~");
            boolean isNew = true;
            if (old.exists()) {
            	if (backup.exists()) {
            		byte[] origData = new byte[(int) backup.length()];
            		try {
            			DataFiles.readMemoryImage(settings, backup.getAbsolutePath(), 0, filesize, origData);
            			isNew = !Arrays.equals(data, origData);
            		} catch (IOException e) {
            			// ignore
            		}
            	}
            }
            if (isNew) {
            	backup.delete();
            	old.renameTo(backup);
            }
            DataFiles.writeMemoryImage(settings, filepath, filesize, data);
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
	static public BankedMemoryEntry newBankedWordMemoryFromFile(ISettingsHandler settings,
			Class<? extends BankedMemoryEntry> klass,
			int addr,
	        int size, 
	        IMemory memory, 
	        String name, IMemoryDomain domain,
	        String filepath, int fileoffs,
	        String filepath2, int fileoffs2) throws IOException {
		DiskMemoryEntry bank0 = newWordMemoryFromFile(settings,
				addr, size, name + " (bank 0)", domain, filepath, fileoffs, false);
		DiskMemoryEntry bank1 = newWordMemoryFromFile(settings,
				addr, size, name + " (bank 1)", domain, filepath2, fileoffs2, false);
		
		IMemoryEntry[] entries = new IMemoryEntry[] { bank0, bank1 };
		BankedMemoryEntry bankedMemoryEntry;
		try {
			bankedMemoryEntry = klass.getConstructor(
					ISettingsHandler.class,
					IMemory.class, String.class, entries.getClass()).newInstance(
							settings, memory, name, entries);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw (IOException) new IOException().initCause(e);
		} catch (Exception e) {
			throw (IOException) new IOException().initCause(e);
		}
		return bankedMemoryEntry;
	}

    /**
     * Create a memory entry for banked (ROM) memory.
     * @param settings TODO
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
			ISettingsHandler settings,
	        int addr, 
	        int size, 
	        IMemory memory, String name,
	        IMemoryDomain domain, String filepath,
	        int fileoffs, String filepath2, int fileoffs2) throws IOException {
		DiskMemoryEntry bank0 = newWordMemoryFromFile(settings,
				addr, size, name + " (bank 0)", domain, filepath, fileoffs, false);
		DiskMemoryEntry bank1 = newWordMemoryFromFile(settings,
				addr, size, name + " (bank 1)", domain, filepath2, fileoffs2, false);
		
		BankedMemoryEntry bankedMemoryEntry = new MultiBankedMemoryEntry(
				memory, name, new IMemoryEntry[] { bank0, bank1 });
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
	public void saveState(ISettingSection section) {
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
	protected void loadFields(ISettingSection section) {
		super.loadFields(section);
		filepath = section.get("FilePath");
		fileoffs = section.getInt("FileOffs");
		filesize = section.getInt("FileSize");
		bStorable = section.getBoolean("Storable");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#loadState(v9t9.base.core.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		super.loadState(section);
		bLoaded = false;
		load();
	}
	
	/**
	 * @param section  
	 */
	protected void loadMemoryContents(ISettingSection section) {
		if (isWordAccess())
			area = createWordMemoryArea(getDomain(), getAddr(), getSize(), bStorable);
		else
			area = createByteMemoryArea(getDomain(), getAddr(), getSize(), bStorable);
		
		bLoaded = false;
		load();
	}
	@Override
	public String getUniqueName() {
		return filepath;
	}
	
	/**
	 * @return the fileoffs
	 */
	public int getFileOffs() {
		return fileoffs;
	}
	/**
	 * @return the filesize
	 */
	public int getFileSize() {
		return filesize;
	}
	/**
	 * @return the bLoaded
	 */
	public boolean isLoaded() {
		return bLoaded;
	}
	/**
	 * @return the bStorable
	 */
	public boolean isStorable() {
		return bStorable;
	}
}

