/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.memory;

import java.io.IOException;

import v9t9.engine.files.NativeFile;
import v9t9.utils.Check;

public class NativeFileMemoryEntry extends MemoryEntry {

    
    private NativeFile file;
    private boolean bLoaded;
    private int filesize;
    private int fileoffs;

    public NativeFileMemoryEntry(
            MemoryArea area, int addr, int size, String name,
            MemoryDomain domain, 
            NativeFile file, int fileoffs, int filesize) {
        super(name, domain, addr, size, area);
        Check.checkArg(file);
        this.file = file;
        this.fileoffs = fileoffs;
        this.filesize = filesize;
    }

    public static MemoryEntry newWordMemoryFromFile(int addr, int size, String name, 
            MemoryDomain domain, NativeFile file, int fileoffs) throws IOException {
        int filesize = file.getFileSize();
        NativeFileMemoryEntry entry = new NativeFileMemoryEntry(
                new WordMemoryArea(domain.getReadWordLatency(addr)), 
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
            bArea.memory = new byte[size];
            bArea.read = bArea.memory;
        } else {
            WordMemoryArea wArea = (WordMemoryArea) area;
            wArea.memory = new short[size/2];
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
                file.readContents(data, fileoffs, filesize);
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
