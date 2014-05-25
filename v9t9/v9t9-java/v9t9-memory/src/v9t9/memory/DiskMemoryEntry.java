/*
  DiskMemoryEntry.java

  (c) 2005-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.memory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import ejs.base.settings.ISettingSection;
import ejs.base.utils.FileUtils;

import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.memory.StoredMemoryEntryInfo;

/**
 * @author ejs
 */
public class DiskMemoryEntry extends MemoryEntry {

    /**	file path */
    private String filename;
    
    /**	is the file loaded yet? */
    private boolean bLoaded;
    
    /**	is the data dirty? */
    private boolean bDirty;

	private StoredMemoryEntryInfo storedInfo;

	private MemoryEntryInfo info;
    
    public DiskMemoryEntry() {
    	// only to be used when reconstructing
    	super();
    }
    public DiskMemoryEntry(MemoryEntryInfo info, MemoryArea area, StoredMemoryEntryInfo storedInfo) {
    	super(info.getName(), info.getDomain(storedInfo.memory), info.getAddress(), 
    			Math.min(Math.abs(info.getSize()), storedInfo.size - storedInfo.fileoffs), area);
		this.storedInfo = storedInfo;
		this.info = storedInfo.createMemoryEntryInfo();
		this.locator = storedInfo.locator;
    	
    	/* this should be set up already */
    	if (area == null) {
    		throw new AssertionError();
    	}
    	
    	this.filename = storedInfo.fileName;
    	this.bDirty = false;
    	this.area = area;
    }
    
    
    /**
	 * @return the storedInfo
	 */
	public StoredMemoryEntryInfo getStoredInfo() {
		return storedInfo;
	}
	
	/**
	 * @return the info
	 */
	public MemoryEntryInfo getInfo() {
		return info;
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
            	URI uri = null;
            	if (storedInfo != null) {
					uri = locator.findFile(storedInfo.settings, info);
				}
            	if (uri == null) {
                    // TODO: send alert
            		return;
            	}

            	if (!info.isStored()) {
            		filename = locator.splitFileName(uri).second;
            	}
            	
            	int filesize = locator.getContentLength(uri);
            	
            	filesize = fixupFileSize(uri, filesize);
            	
                InputStream is = locator.createInputStream(uri);
                FileUtils.skipFully(is, storedInfo.fileoffs);
				byte[] data = FileUtils.readInputStreamContentsAndClose(is, 
						filesize);
                area.copyFromBytes(data);

            	bLoaded = true;
                
                // see if it has symbols
                String symbolFileName = getSymbolFileName();
                URI symfile = locator.findFile(symbolFileName);
            	if (symfile != null) {
            		try {
            			loadSymbolsAndClose(locator.createInputStream(symfile));
            		} catch (IOException e) {
            			// TODO: send alert
            		}
            	}
            } catch (java.io.IOException e) {
                // TODO: send alert
            }
        }
    }

    /**
	 * @param uri
	 * @param filesize
	 * @return
     * @throws IOException 
	 */
	private int fixupFileSize(URI uri, int filesize) throws IOException {

		int size = info.getSize();
		
		try {
			try {
				filesize = storedInfo.locator.getContentLength(uri);
			} catch (IllegalArgumentException e) {
				filesize = locator.getContentLength(uri);
			}
	        
			// for large files selected, e.g., by accident 
			if (size > 0 && filesize > size) {
				filesize = size;
			} else if (size < 0 && filesize > -size) {
				filesize = -size;
			} else if (filesize + info.getAddress() > 0x10000) {
				filesize = 0x10000 - info.getAddress();
			}
            
        } catch (IOException e) {		// TODO
            if (info.isStored()) {
				filesize = size;	// not created yet
			} else {
				throw e;
			}
        }
        
        return filesize;
	}
	
	public void setDirty(boolean dirty) {
    	bDirty = dirty;
    }
    
    /* (non-Javadoc)
     * @see v9t9.MemoryEntry#save()
     */
    @Override
	public void save() throws IOException {
        if (info.isStored() && bDirty) {
            byte[] data = new byte[getSize()];
            area.copyToBytes(data);
            
            URI uri = locator.getWriteURI(filename);
            URI backup = URI.create(uri.toString() + "~");
            
            // only make backup if the current backup differs from the new contents
            boolean isNew = true;
            byte[] origData = null;
            if (locator.exists(uri)) {
            	if (locator.exists(backup)) {
            		try {
            			origData = FileUtils.readInputStreamContentsAndClose(
            					locator.createInputStream(backup), getSize());
            			isNew = !Arrays.equals(data, origData);
            		} catch (IOException e) {
            			// ignore
            		}
            	} else {
            		try {
            			origData = FileUtils.readInputStreamContentsAndClose(
            					locator.createInputStream(uri), getSize());
            		} catch (IOException e) {
            			// ignore
            		}
            	}
            }
            if (isNew) {
            	if (origData != null) {
            		try {
            			FileUtils.writeOutputStreamContentsAndClose(
            					locator.createOutputStream(backup), origData, Math.min(getSize(), origData.length));
            		} catch (IOException e) {
            			e.printStackTrace();
            			// ignore
            		}
            	}
            }
            FileUtils.writeOutputStreamContentsAndClose(
            		locator.createOutputStream(uri), data, getSize());
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
		return filename;
	}
	
	public String getSymbolFileName() {
		if (filename == null)
			return null;
		int idx = filename.lastIndexOf('.');
        if (idx >= 0) {
        	return filename.substring(0, idx) + ".sym";
        } else {
        	return filename + ".sym";
        }
	}
	
	@Override
	public void saveState(ISettingSection section) {
		super.saveState(section);
		section.put("FileName", filename);
		section.put("FileMD5", storedInfo.md5);
		if (storedInfo == null)
			return;
		section.put("FileOffs", storedInfo.fileoffs);
		//section.put("FileSize", storedInfo.filesize);
		section.put("Storable", storedInfo.info.isStored());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#loadFields(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	@Override
	protected void loadFields(ISettingSection section) {
		super.loadFields(section);
		if (info == null) {
			info = (isWordAccess() ? MemoryEntryInfoBuilder.wordMemoryEntry() : MemoryEntryInfoBuilder.byteMemoryEntry())
				.withFilename(section.get("FileName") != null ? section.get("FileName") : section.get("FilePath"))
				.withFileMD5(section.get("FileMD5"))
				.withOffset(section.getInt("FileOffs"))
				.withSize(section.getInt("Size"))
				.withAddress(getAddr())
				.withDomain(getDomain().getIdentifier())
				.storable(section.getBoolean("Storable")).create(getName());
		}
		
		try {
			storedInfo = memory.getMemoryEntryFactory().resolveMemoryEntry(info);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		if (storedInfo != null) {
			try {
				area = (MemoryArea) memory.getMemoryEntryFactory().createMemoryArea(storedInfo.info);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		bLoaded = false;
		load();
	}
	@Override
	public String getUniqueName() {
		return filename;
	}
	
	/**
	 * @return the fileoffs
	 */
	public int getFileOffs() {
		return storedInfo.fileoffs;
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
		return storedInfo.info.isStored();
	}
}

