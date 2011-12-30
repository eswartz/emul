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

import ejs.base.settings.ISettingSection;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.DataFiles;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.memory.StoredMemoryEntryInfo;

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

	private StoredMemoryEntryInfo storedInfo;

	private MemoryEntryInfo info;
    
    public DiskMemoryEntry() {
    	// only to be used when reconstructing
    	super();
    }
    DiskMemoryEntry(MemoryEntryInfo info, String name, MemoryArea area, StoredMemoryEntryInfo storedInfo) {
    	super(name, info.getDomain(storedInfo.memory), info.getAddress(), storedInfo.size, area);
		this.info = info;
		this.storedInfo = storedInfo;
    	this.settings = storedInfo.settings;
    	
    	/* this should be set up already */
    	if (area == null) {
    		throw new AssertionError();
    	}
    	
    	// TODO
    	this.filepath = storedInfo.uri.getPath();
    	this.fileoffs = storedInfo.fileoffs;
    	this.filesize = storedInfo.filesize;
    	this.bStorable = info.isStored();
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
			area = MemoryAreaFactory.createWordMemoryArea(storedInfo.memory, info);
		else
			area = MemoryAreaFactory.createByteMemoryArea(storedInfo.memory, info);
		
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

