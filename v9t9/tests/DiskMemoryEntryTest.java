/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.tests;

import junit.framework.TestCase;
import v9t9.emulator.hardware.TI994A;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 */
public class DiskMemoryEntryTest extends TestCase {
    private TI994A machine;
    private MemoryDomain CPU;
    
    String basedir = "/usr/local/src/v9t9-data/roms/";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DiskMemoryEntryTest.class);
    }
	 /* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
	    // TODO Auto-generated method stub
	    super.setUp();
	    machine = new TI994A();
        CPU = machine.CPU;
	    CPU.zero();
	}
 

    public void testNewFromFile() {
        DiskMemoryEntry ent = DiskMemoryEntry.newWordMemoryFromFile(0x000, 0x2000, "rom", CPU,
                basedir+"994arom.bin", 0, false);
        assertTrue(ent != null);
        assertEquals(ent.size, 8192);
        assertEquals(ent.fileoffs, 0);
        assertEquals(ent.filesize, 8192);
        assertEquals(ent.bLoaded, false);
        assertEquals(ent.bStorable, false);
        
        ent.map();
        
        assertEquals(ent.bLoaded, true);
        assertEquals(Integer.toHexString(CPU.readWord(0)), Integer.toHexString((short)0x83e0));
        
        ent.unmap();
        assertEquals(ent.bLoaded, false);
        assertEquals(CPU.readWord(0), (short)0);
    }


}
