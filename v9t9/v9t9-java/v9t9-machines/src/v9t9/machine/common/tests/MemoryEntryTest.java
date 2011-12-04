/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 16, 2004
 *
 */
package v9t9.machine.common.tests;

import junit.framework.TestCase;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.memory.ByteMemoryArea;
import v9t9.engine.memory.MemoryArea;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.memory.ZeroWordMemoryArea;
import v9t9.machine.ti99.memory.ConsoleRamArea;


/**
 * @author ejs
 */
public class MemoryEntryTest extends TestCase {
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
        CPU = new MemoryDomain(IMemoryDomain.NAME_CPU);
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
        IMemoryEntry ent = new MemoryEntry("most mem", CPU, 2048, IMemoryDomain.PHYSMEMORYSIZE-2048, 
                anArea);
        assertTrue(ent != null);
        assertEquals(ent.getName(), "most mem");
        //assertTrue(!ent.isMapped());
        assertTrue(ent.getAddr() == 2048);
        assertTrue(ent.getSize() == IMemoryDomain.PHYSMEMORYSIZE-2048);
        assertTrue(ent.getDomain() == CPU);

        boolean bCaught;
        
        /* illegal start addr */
        bCaught = false;
        try {
            ent = new MemoryEntry("error 1", CPU, 1234, 1024, anArea);
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
            ent = new MemoryEntry("error 1", null, 1024, 1024, anArea);
        } catch (Throwable e) {
            bCaught = true;
        }
        assertTrue(bCaught);
    }

    public void testIsMapped() {
        MemoryArea anArea = new ZeroWordMemoryArea(); 
        MemoryEntry ent = new MemoryEntry("most mem", CPU, 2048, IMemoryDomain.PHYSMEMORYSIZE-2048, 
                anArea);
        CPU.mapEntry(ent);
        assertTrue(CPU.isEntryFullyMapped(ent));
        assertTrue(CPU.isEntryMapped(ent));
    }

    public void testMapAndUnmap() {
        MemoryArea zArea = new ZeroWordMemoryArea(); 
        MemoryEntry zEnt = new MemoryEntry("all mem", CPU, 0, IMemoryDomain.PHYSMEMORYSIZE, 
                zArea);
        assertTrue(zEnt != null);
        CPU.mapEntry(zEnt);
        
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
        CPU.mapEntry(ent);
        
        byte val = CPU.readByte(2048); 
        assertTrue(val == (byte)0xaa);
        assertTrue(CPU.readByte(2047) == 0x0);
        assertTrue(CPU.readByte(2048+1024) == 0x0);

        /* allow map on top */
        CPU.mapEntry(zEnt);
        assertTrue(CPU.isEntryMapped(zEnt));
        assertTrue(CPU.isEntryFullyMapped(zEnt));
        assertTrue(CPU.readByte(2048) == 0x0);
        assertFalse(CPU.isEntryFullyMapped(ent));
        assertTrue(CPU.isEntryMapped(ent));
        
        /* unmapping leaves old stuff */
        CPU.unmapEntry(zEnt);
        assertFalse(CPU.isEntryMapped(zEnt));
        assertFalse(CPU.isEntryFullyMapped(zEnt));
        assertTrue(CPU.isEntryFullyMapped(ent));
        assertTrue(CPU.isEntryMapped(ent));
        assertEquals((byte)0xaa, CPU.readByte(2048));
        
        CPU.unmapEntry(ent);
        assertFalse(CPU.isEntryFullyMapped(zEnt));
        assertFalse(CPU.isEntryMapped(zEnt));
        assertEquals(0x0, CPU.readByte(2048));
        
    }


    public void testCpuRamMemorySemantics() throws Exception {
    	MemoryEntry entry = new MemoryEntry("CPU RAM",
    			CPU,
    			0x8000,
    			0x400,
    			new ConsoleRamArea());
    	if (ConsoleRamArea.settingEnhRam.getBoolean()) {
    		entry.writeWord(0x8000, (short) 0x1234);
    		entry.writeWord(0x8300, (short) 0x5678);
    		assertEquals(0x1234, entry.readWord(0x8000));
    		assertEquals(0x5678, entry.readWord(0x8000));
    	}

    	ConsoleRamArea.settingEnhRam.setBoolean(false);
    	_testCPURamNonEnhanced(entry);
    	
    	ConsoleRamArea.settingEnhRam.setBoolean(true);
    	_testCPURamEnhanced(entry);
    	
    }

	private void _testCPURamEnhanced(MemoryEntry entry) {
		entry.writeWord(0x8000, (short) 0x1234);
		entry.writeWord(0x8300, (short) 0x5678);
		assertEquals(0x1234, entry.readWord(0x8000));
		assertEquals(0x5678, entry.readWord(0x8300));
		
		entry.writeByte(0x8157, (byte) 0x11);
		entry.writeByte(0x8357, (byte) 0x22);
		assertEquals(0x11, entry.readByte(0x8157));
		assertEquals(0x22, entry.readByte(0x8357));
	}

	private void _testCPURamNonEnhanced(MemoryEntry entry) {
		entry.writeWord(0x8000, (short) 0x1234);
		entry.writeWord(0x8300, (short) 0x5678);
		assertEquals(0x5678, entry.readWord(0x8000));
		assertEquals(0x5678, entry.readWord(0x8300));
		
		entry.writeByte(0x8157, (byte) 0x11);
		entry.writeByte(0x8357, (byte) 0x22);
		assertEquals(0x22, entry.readByte(0x8157));
		assertEquals(0x22, entry.readByte(0x8357));
		
	}
}
