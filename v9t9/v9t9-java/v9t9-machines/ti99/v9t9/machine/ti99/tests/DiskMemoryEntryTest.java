/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.machine.ti99.tests;

import junit.framework.TestCase;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.tests.TestSettingsHandler;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.machine.ti99.machine.TI994A;

/**
 * @author ejs
 */
public class DiskMemoryEntryTest extends TestCase {
    private TI994A machine;
    private IMemoryDomain CPU;
    
    String basedir = "/usr/local/src/v9t9-data/roms/";
	private ISettingsHandler settings;

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
	    settings = new TestSettingsHandler();
	    machine = new TI994A(settings);
        CPU = machine.getConsole();
	    CPU.zero();
	}
 

    public void testNewFromFile() throws Exception {
        DiskMemoryEntry ent = DiskMemoryEntry.newWordMemoryFromFile(
        		settings, 0x000, 0x2000, "rom", CPU,
                basedir+"994arom.bin", 0, false);
        assertTrue(ent != null);
        assertEquals(ent.getSize(), 8192);
        assertEquals(ent.getFileOffs(), 0);
        assertEquals(ent.getFileSize(), 8192);
        assertEquals(ent.isLoaded(), false);
        assertEquals(ent.isStorable(), false);
        
        CPU.mapEntry(ent);
        
        assertEquals(ent.isLoaded(), true);
        assertEquals(Integer.toHexString(CPU.readWord(0)), Integer.toHexString((short)0x83e0));
        
        CPU.unmapEntry(ent);
        assertEquals(ent.isLoaded(), false);
        assertEquals(CPU.readWord(0), (short)0);
    }


}
