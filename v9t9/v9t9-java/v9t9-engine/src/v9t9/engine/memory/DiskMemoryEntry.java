/*
  DiskMemoryEntry.java

  (c) 2005-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.files.URIUtils;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.memory.StoredMemoryEntryInfo;
import ejs.base.settings.ISettingSection;
import ejs.base.utils.FileUtils;

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
    DiskMemoryEntry(MemoryEntryInfo info, MemoryArea area, StoredMemoryEntryInfo storedInfo) {
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
        if (info != null && info.isStored() && bDirty) {
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
    
    public void overwrite() throws FileNotFoundException, IOException {
    	byte[] data = new byte[getSize()];
        area.copyToBytes(data);
        URI uri = locator.findFile(getFilepath());
		FileUtils.writeOutputStreamContentsAndClose(
				locator.createOutputStream(uri),data, getSize());

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
	protected void loadFields(IEventNotifier notifier, ISettingSection section) {
		super.loadFields(notifier, section);
		if (info == null) {
			info = (isWordAccess() ? MemoryEntryInfoBuilder.wordMemoryEntry() : MemoryEntryInfoBuilder.byteMemoryEntry())
				.withFilename(section.get("FileName") != null ? section.get("FileName") : section.get("FilePath"))
				.withFileMD5(section.get("FileMD5"))
				.withFileMD5Algorithm(section.get("FileMD5Algorithm"))
				.withOffset(section.getInt("FileOffs"))
				.withSize(section.getInt("Size"))
				.withAddress(getAddr())
				.withDomain(getDomain().getIdentifier())
				.storable(section.getBoolean("Storable")).create(getName());
		}
		
		try {
			storedInfo = memory.getMemoryEntryFactory().resolveMemoryEntry(info);
		} catch (IOException e) {
			// if failed to load essential ROM/GROM, just fill in from model
			for (MemoryEntryInfo rinfo : memory.getModel().getRequiredRomMemoryEntries()) {
				if (rinfo.getDomainName().equals(info.getDomainName())
						&& rinfo.getAddress() == info.getAddress()) {
					
					try {
						storedInfo = memory.getMemoryEntryFactory().resolveMemoryEntry(rinfo);
						
						String message = "Loaded '"
								+ URIUtils.splitFileName(storedInfo.uri).second
								+ "' instead of missing ROM file '" 
								+ info.getFilename() + "'";
						if (notifier != null) {
							notifier.notifyEvent(null, Level.WARNING, message);
						} else {
							System.err.println(message);
						}
						
						info = rinfo;
					} catch (IOException e2) {
						if (notifier != null) {
							notifier.notifyEvent(null, Level.ERROR, 
									e2 instanceof FileNotFoundException ? 
											"Failed to load ROM file '" + info.getResolvedFilename(null) +
											"' and fallback ROM file '" + rinfo.getFilename() + "'"
									: e2.getMessage());
						} else {
							e2.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryEntry#loadMemory(v9t9.common.events.IEventNotifier, ejs.base.settings.ISettingSection)
	 */
	@Override
	public void loadMemory(IEventNotifier notifier, ISettingSection section) {
		bLoaded = false;
		info = null;
		super.loadMemory(notifier, section);
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
	/**
	 * @param b
	 */
	public void setStorable(boolean b) {
		info.getProperties().put(MemoryEntryInfo.STORED, b);
	}
}

