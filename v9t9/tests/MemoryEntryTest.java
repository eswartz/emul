/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.tests;

import junit.framework.TestCase;
import v9t9.emulator.hardware.TI994A;
import v9t9.engine.memory.ByteMemoryArea;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.ZeroWordMemoryArea;


/**
 * @author ejs
 */
public class MemoryEntryTest extends TestCase {
    private TI994A machine;
    private MemoryDomain CPU;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(MemoryEntryTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        machine = new TI994A();
        CPU = machine.getConsole();
   }

    /*
     * @see TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMemoryEntry() {
        MemoryArea anArea = new ZeroWordMemoryArea(); 
        MemoryEntry ent = new MemoryEntry("most mem", CPU, 2048, MemoryDomain.PHYSMEMORYSIZE-2048, 
                anArea);
        assertTrue(ent != null);
        assertEquals(ent.name, "most mem");
        //assertTrue(!ent.isMapped());
        assertTrue(ent.addr == 2048);
        assertTrue(ent.size == MemoryDomain.PHYSMEMORYSIZE-2048);
        assertTrue(ent.domain == CPU);

        boolean bCaught;
        
        /* illegal start addr */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", CPU, 1234, 1024, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);
        
        /* illegal size #2 */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", CPU, 1024, 123102, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);

        /* illegal size for normal entry */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", CPU, 1024, -1024, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);

        /* null params */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", CPU, 1024, 1024, null);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);

        /* null params */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", null, 1024, 1024, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);
    }

    public void testIsMapped() {
        MemoryArea anArea = new ZeroWordMemoryArea(); 
        MemoryEntry ent = new MemoryEntry("most mem", CPU, 2048, MemoryDomain.PHYSMEMORYSIZE-2048, 
                anArea);
        ent.map();
        assertTrue(ent.isMapped());
    }

    public void testMapAndUnmap() {
        MemoryArea zArea = new ZeroWordMemoryArea(); 
        MemoryEntry zEnt = new MemoryEntry("all mem", CPU, 0, MemoryDomain.PHYSMEMORYSIZE, 
                zArea);
        assertTrue(zEnt != null);
        zEnt.map();
        
        ByteMemoryArea anArea = new ByteMemoryArea();
        anArea.memory = new byte[1024];
        for (int i = 0; i < anArea.memory.length; i++) {
            anArea.memory[i] = (byte)0xaa;
        }
        anArea.read = anArea.memory;
        anArea.write = anArea.memory;
        MemoryEntry ent = new MemoryEntry("block", CPU, 2048, 1024, 
                anArea);
        assertTrue(ent != null);
        ent.map();
        
        byte val = CPU.readByte(2048); 
        assertTrue(val == (byte)0xaa);
        assertTrue(CPU.readByte(2047) == 0x0);
        assertTrue(CPU.readByte(2048+1024) == 0x0);

        /* allow map on top */
        zEnt.map();
        assertTrue(zEnt.isMapped());
        assertTrue(CPU.readByte(2048) == 0x0);
        
        /* unmapping leaves zeroes, not old stuff */
        zEnt.unmap();
        assertTrue(!ent.isMapped());
        assertTrue(!zEnt.isMapped());
        assertTrue(CPU.readByte(2048) == 0x0);
        
        ent.unmap();
        assertTrue(!ent.isMapped());
        assertTrue(!zEnt.isMapped());
        assertTrue(CPU.readByte(2048) == 0x0);
        
    }


}
