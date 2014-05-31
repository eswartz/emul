/*
  NativeFileMemoryEntry.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.memory;

import java.io.IOException;

import ejs.base.utils.Check;


import v9t9.common.files.IEmulatedFile;
import v9t9.common.files.NativeFile;
import v9t9.common.memory.IMemoryDomain;

public class NativeFileMemoryEntry extends MemoryEntry {

    
    private IEmulatedFile file;
    private boolean bLoaded;
    private int filesize;
    private int fileoffs;

    public NativeFileMemoryEntry(
            MemoryArea area, int addr, int size, String name,
            IMemoryDomain domain, 
            IEmulatedFile file, int fileoffs, int filesize) {
        super(name, domain, addr, size, area);
        Check.checkArg(file);
        this.file = file;
        this.fileoffs = fileoffs;
        this.filesize = filesize;
    }

    public static MemoryEntry newMemoryFromFile(int addr, String name, IMemoryDomain domain, 
    		NativeFile file, int fileoffs) throws IOException {
    	int filesize = file.getFileSize();
    	int roundedSize = (IMemoryDomain.AREASIZE - 1 + filesize) & ~(IMemoryDomain.AREASIZE - 1);
    	NativeFileMemoryEntry entry = new NativeFileMemoryEntry(
    			domain.isWordAccess() ? new WordMemoryArea(domain.getLatency(addr)) : 
    					new ByteMemoryArea(domain.getLatency(addr)), 
    			addr, roundedSize, name, domain, file, fileoffs, filesize);
    	
    	entry.updateMemoryArea();
    	
    	return entry;
    }

    /** Update the memory area given these parameters.
     * @return
     */
    private void updateMemoryArea() {
        if (area instanceof ByteMemoryArea) {
            ByteMemoryArea bArea = (ByteMemoryArea) area;
            bArea.memory = new byte[getSize()];
            bArea.read = bArea.memory;
        } else {
            WordMemoryArea wArea = (WordMemoryArea) area;
            wArea.memory = new short[getSize()/2];
            wArea.read = wArea.memory;
        }
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
                file.readContents(data, 0, fileoffs, filesize);
                area.copyFromBytes(data);
                bLoaded = true;
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* (non-Javadoc)
     * @see v9t9.MemoryEntry#save()
     */
    @Override
	public void save() throws IOException {
        super.save();
    }
    
    @Override
	public void unload() {
        super.unload();
        bLoaded = false;
    }
}
