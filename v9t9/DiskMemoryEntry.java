/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9;

import java.io.IOException;

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
    
    /*	is this RAM? */
    public boolean bRam;

    /*	is this nonvolatile RAM? */
    public boolean bStorable;

    /*	is the file loaded yet? */
    public boolean bLoaded;
    
    /*	is the data dirty? */
    private boolean bDirty;
    
    /** Construct a DiskMemoryEntry based on the file length.
     * @param size if not isRam, the maximum acceptable size,
     * else the fixed size for a RAM file.  May be 0 to use
     * file size directly.
     */
    static public DiskMemoryEntry newFromFile(
            int addr, int size, String name, 
            MemoryDomain domain, String filepath, int fileoffs,
            boolean isRam, boolean isStored) {

        if (size < -MemoryDomain.PHYSMEMORYSIZE
                || (isStored && size <= 0)
                || (isStored && fileoffs != 0)
                ||	(isStored && !isRam))
            throw new AssertionError();
        
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
        } catch (Exception e) {		// TODO
            if (isStored)
                filesize = size;	/* not created yet */
            else
                return null;
        }
        
        MemoryArea area = new MemoryArea();
        DiskMemoryEntry entry = new DiskMemoryEntry(addr, filesize, name, domain, area, filepath, fileoffs, filesize, isRam, isStored);

        entry.updateMemoryArea();
        
        return entry;
    }
    
    /**	Update the memory area given these parameters.
     * Should be overriden in subclasses.
     * @param isRam
     * @param isStored
     * @return
     */
    public void updateMemoryArea() {
        area.memory = new byte[size];
        area.read = area.memory;
        if (bRam)
            area.write = area.memory;
        
        class DiskMemoryAreaHandlers extends MemoryArea.DefaultAreaHandlers {
            private DiskMemoryEntry entry;
            DiskMemoryAreaHandlers(DiskMemoryEntry entry) {
                this.entry = entry;
            }
            public void writeByte(MemoryArea theArea, int address, byte val) {
               entry.bDirty = true;
               super.writeByte(theArea, address, val);
            }
        };
        
        /* notice when memory changes... */
        if (bStorable)
            area.areaWriteByte = new DiskMemoryAreaHandlers(this);
    }
    
    DiskMemoryEntry(int addr, int size, String name,
            MemoryDomain domain, MemoryArea area, 
            String filepath, int fileoffs, int filesize,
            boolean isRam, boolean isStorable) {
        super(name, domain, addr, size, area);
        
        /* this should be set up already */
        if (area == null)
            throw new AssertionError();
        
        this.filepath = filepath;
        this.fileoffs = fileoffs;
        this.filesize = filesize;
        this.bRam = isRam;
        this.bStorable = isStorable;
        this.bDirty = false;
    }
    
    /* (non-Javadoc)
     * @see v9t9.MemoryEntry#load()
     */
    public void load() {
        super.load();
        if (!bLoaded) {
            try {
                DataFiles.readMemoryImage(filepath, fileoffs, filesize, area.memory);
                bLoaded = true;
            } catch (java.io.IOException e) {
                // TODO: error
            }
        }
    }

    /* (non-Javadoc)
     * @see v9t9.MemoryEntry#save()
     */
    public void save() {
        if (bStorable && bDirty) {
            try {
                DataFiles.writeMemoryImage(filepath, filesize, area.memory);
	            bDirty = false;
            } catch (IOException e) {
                // TODO: error
            }
        }
        super.save();
    }
    
    public void unload() {
        super.unload();
        bLoaded = false;
    }
}
