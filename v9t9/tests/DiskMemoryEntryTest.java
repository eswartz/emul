/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.tests;

import v9t9.DiskMemoryEntry;
import v9t9.Machine;
import v9t9.Memory;
import junit.framework.TestCase;

/**
 * @author ejs
 */
public class DiskMemoryEntryTest extends TestCase {
    private Machine machine;
    private Memory memory;

    String basedir = "/usr/local/src/v9t9-data/roms/";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DiskMemoryEntryTest.class);
    }
	 /* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
	    // TODO Auto-generated method stub
	    super.setUp();
	    machine = new Machine();
	    memory = machine.getMemory();
	    memory.CPU.zero();
	}
 

    public void testNewFromFile() {
        DiskMemoryEntry ent = DiskMemoryEntry.newFromFile(0x000, 0x2000, "rom", memory.CPU,
                basedir+"994arom.bin", 0, false, false);
        assertTrue(ent != null);
        assertEquals(ent.size, 8192);
        assertEquals(ent.fileoffs, 0);
        assertEquals(ent.filesize, 8192);
        assertEquals(ent.bLoaded, false);
        assertEquals(ent.bRam, false);
        assertEquals(ent.bStorable, false);
        
        ent.map();
        
        assertEquals(ent.bLoaded, true);
        assertEquals(Integer.toHexString(memory.CPU.readWord(0)), Integer.toHexString((short)0x83e0));
        
        ent.unmap();
        assertEquals(ent.bLoaded, false);
        assertEquals(memory.CPU.readWord(0), (short)0);
    }

    public void testUpdateMemoryArea() {
    }

    public void testDiskMemoryEntry() {
    }

}
