/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.engine.memory;

import java.io.IOException;

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
     * @param size
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
    	WordMemoryArea area = new WordMemoryArea(domain.getReadWordLatency(addr));
        DiskMemoryEntry entry = newFromFile(area, addr, size, name, domain, filepath, fileoffs, isStored);
        if (isStored) {
        	area.write = area.memory;
        }
        return entry;
    }

    /**
     * Read memory and create a memory entry with no byte ordering adjustment.
     * @param addr
     * @param size
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
        
        if (isStored) {
    		area.write = area.memory;
    	}
        return entry;
    }

    /** Construct a DiskMemoryEntry based on the file length.
     * @param size if not isRam, the maximum acceptable size,
     * else the fixed size for a RAM file.  May be 0 to use
     * file size directly.
     * @param isStored if true, this is a RAM entry which can be rewritten
     * @return the entry
     * @throws IOException if the memory cannot be read and is not stored
     */
    static public DiskMemoryEntry newFromFile(
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
            if ((filesize & 0x1fff) != 0) {
				filesize &= 0x1fff;
			}
            
        } catch (IOException e) {		// TODO
            if (isStored) {
				filesize = size;	/* not created yet */
			} else {
				throw e;
			}
        }
        
        DiskMemoryEntry entry = new DiskMemoryEntry(area, addr, filesize, name, domain, filepath, fileoffs, filesize, isStored);

        entry.updateMemoryArea();
        
        return entry;
    }
    
    /**	Update the memory area given these parameters.
     * @return
     */
    public void updateMemoryArea() {
        if (area instanceof ByteMemoryArea) {
            ByteMemoryArea bArea = (ByteMemoryArea) area;
            bArea.memory = new byte[size];
            bArea.read = bArea.memory;
        } else {
            WordMemoryArea wArea = (WordMemoryArea) area;
            wArea.memory = new short[size/2];
            wArea.read = wArea.memory;
        }
        
        class DiskMemoryAreaHandlers extends MemoryArea.DefaultAreaHandlers {
            private DiskMemoryEntry entry;
            DiskMemoryAreaHandlers(DiskMemoryEntry entry) {
                this.entry = entry;
            }
            @Override
			public void writeByte(MemoryArea theArea, int address, byte val) {
               entry.bDirty = true;
               super.writeByte(theArea, address, val);
            }
        };
        
        /* notice when memory changes... */
        if (bStorable) {
			area.areaWriteByte = new DiskMemoryAreaHandlers(this);
		}
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
                bLoaded = true;
            } catch (java.io.IOException e) {
                // TODO: error
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
}
