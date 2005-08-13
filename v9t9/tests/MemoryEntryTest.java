/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.tests;

import v9t9.Machine;
import v9t9.Memory;
import v9t9.MemoryArea;
import v9t9.MemoryDomain;
import v9t9.MemoryEntry;
import v9t9.ZeroMemoryArea;
import junit.framework.TestCase;


/**
 * @author ejs
 */
public class MemoryEntryTest extends TestCase {
    private Machine machine;
    private Memory memory;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(MemoryEntryTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        machine = new Machine();
        memory = machine.getMemory();

   }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMemoryEntry() {
        MemoryArea anArea = new ZeroMemoryArea(); 
        MemoryEntry ent = new MemoryEntry("most mem", memory.CPU, 2048, MemoryDomain.PHYSMEMORYSIZE-2048, 
                anArea);
        assertTrue(ent != null);
        assertEquals(ent.name, "most mem");
        assertTrue(!ent.isMapped());
        assertTrue(ent.addr == 2048);
        assertTrue(ent.size == MemoryDomain.PHYSMEMORYSIZE-2048);
        assertTrue(ent.domain == memory.CPU);

        boolean bCaught;
        
        /* illegal start addr */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", memory.CPU, 1234, 1024, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);
        
        /* illegal size */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", memory.CPU, 1024, 1234, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);


        /* illegal size #2 */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", memory.CPU, 1024, 123102, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);

        /* illegal size for normal entry */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", memory.CPU, 1024, -1024, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);

        /* null params */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", memory.CPU, 1024, 1024, null);
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
        MemoryArea anArea = new ZeroMemoryArea(); 
        MemoryEntry ent = new MemoryEntry("most mem", memory.CPU, 2048, MemoryDomain.PHYSMEMORYSIZE-2048, 
                anArea);
        ent.map();
        assertTrue(ent.isMapped());
    }

    public void testMapAndUnmap() {
        MemoryArea zArea = new ZeroMemoryArea(); 
        MemoryEntry zEnt = new MemoryEntry("all mem", memory.CPU, 0, MemoryDomain.PHYSMEMORYSIZE, 
                zArea);
        assertTrue(zEnt != null);
        zEnt.map();
        
        MemoryArea anArea = new MemoryArea();
        anArea.memory = new byte[1024];
        for (int i = 0; i < anArea.memory.length; i++) {
            anArea.memory[i] = (byte)0xaa;
        }
        anArea.read = anArea.memory;
        anArea.write = anArea.memory;
        MemoryEntry ent = new MemoryEntry("block", memory.CPU, 2048, 1024, 
                anArea);
        assertTrue(ent != null);
        ent.map();
        
        byte val = memory.CPU.readByte(2048); 
        assertTrue(val == (byte)0xaa);
        assertTrue(memory.CPU.readByte(2047) == 0x0);
        assertTrue(memory.CPU.readByte(2048+1024) == 0x0);

        /* allow map on top */
        zEnt.map();
        assertTrue(zEnt.isMapped());
        assertTrue(memory.CPU.readByte(2048) == 0x0);
        
        /* unmapping leaves zeroes, not old stuff */
        zEnt.unmap();
        assertTrue(!ent.isMapped());
        assertTrue(!zEnt.isMapped());
        assertTrue(memory.CPU.readByte(2048) == 0x0);
        
        ent.unmap();
        assertTrue(!ent.isMapped());
        assertTrue(!zEnt.isMapped());
        assertTrue(memory.CPU.readByte(2048) == 0x0);
        
    }


}
