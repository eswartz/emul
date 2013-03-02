/*
  NativeFileMemoryEntry.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.memory;

import java.io.IOException;

import ejs.base.utils.Check;


import v9t9.common.files.EmulatedFile;
import v9t9.common.files.NativeFile;
import v9t9.common.memory.IMemoryDomain;

public class NativeFileMemoryEntry extends MemoryEntry {

    
    private EmulatedFile file;
    private boolean bLoaded;
    private int filesize;
    private int fileoffs;

    public NativeFileMemoryEntry(
            MemoryArea area, int addr, int size, String name,
            IMemoryDomain domain, 
            EmulatedFile file, int fileoffs, int filesize) {
        super(name, domain, addr, size, area);
        Check.checkArg(file);
        this.file = file;
        this.fileoffs = fileoffs;
        this.filesize = filesize;
    }

    public static MemoryEntry newWordMemoryFromFile(int addr, String name, IMemoryDomain domain, 
            NativeFile file, int fileoffs) throws IOException {
        int filesize = file.getFileSize();
        NativeFileMemoryEntry entry = new NativeFileMemoryEntry(
                new WordMemoryArea(domain.getLatency(addr)), 
                addr, filesize, name, domain, file, fileoffs, filesize);

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
