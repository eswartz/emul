/**
 * 
 */
package v9t9.machine.common.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpu;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.machine.ti99.cpu.ChangeBlock9900;
import v9t9.machine.ti99.cpu.OperandChanges9900.*;
import v9t9.machine.ti99.cpu.Cpu9900;

/**
 * @author ejs
 *
 */
public class TestChangeBlock9900
{
	private ICpu cpu;
	private int origWP;
	private int origPC;
 
 	@Before
 	public void setup() throws Exception {

		IMachineModel model = new Basic9900MachineModelTest();
 		ISettingsHandler settings = new BasicSettingsHandler();
		IMachine machine = model.createMachine(settings);
		
		cpu = machine.getCpu();
		for (int i = 0; i < 65536; i += 2) {
			cpu.getConsole().flatWriteWord(i, (short) 0xffff);
		}
		origWP = 0x8000;
		origPC = 0x400;
		cpu.getState().setRegister(Cpu9900.REG_WP, origWP);
		cpu.getState().setRegister(Cpu9900.REG_PC, origPC);
		cpu.getState().setRegister(Cpu9900.REG_ST, 0);
 	}
	
 	@Test
 	public void testNoChange() throws Exception {
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		ChangeBlock9900 change2 = new ChangeBlock9900(cpu);
 		assertEquals(change, change2);
 	}
 	
 	protected void writeInstruction(int pc, int... words) {
 		for (int i = 0; i < words.length; i++)
 			cpu.getConsole().writeWord(pc + i * 2, (short) words[i]);
 	}
 	

	private void assertChange(ChangeBlock9900 change, int index,
			Class<? extends IChangeElement> klass, int... args) {
		IChangeElement el = change.getElement(index);
		assertTrue("expected " + klass.getSimpleName()  + " got " + el.toString() , 
				klass.isAssignableFrom(el.getClass()));
		
//		if (index == 0)
//			assertEquals(cpu, el.getParent());
//		else
//			assertEquals(change.getElement(index-1), el.getParent());
		
		if (klass == WriteRegister.class) {
			assertEquals(args[0] & 0xffff, ((WriteRegister) el).reg & 0xffff);
			assertEquals(args[1] & 0xffff, ((WriteRegister) el).value & 0xffff);
		}
		else if (klass == ReadWord.class) {
			if (args.length > 0)
				assertEquals(args[0] & 0xffff, ((ReadWord) el).state.ea & 0xffff);
		}
		else if (klass == WriteWord.class) {
			assertEquals(args[0] & 0xffff, ((WriteWord) el).state.ea & 0xffff);
			assertEquals(args[1] & 0xffff, ((WriteWord) el).state.value & 0xffff);
		}
	}

	
 	@Test
 	public void testInstrWithImmediate() throws Exception {
 		writeInstruction(origPC, 0x204, 0x123);	// LI R4,>123
 		
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.createOperandFetch();
 		int curPC = cpu.getState().getPC();
 		
 		assertEquals(origPC, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(2, change.getCount());
 		assertChange(change, 0, WriteRegister.class, Cpu9900.REG_PC, origPC + 4);
 		assertChange(change, 1, ReadRegister.class, 4);
 		
 		assertEquals((short)0xffff, cpu.getState().getRegister(4));	// no change yet
 		
 		change.apply();
 		assertEquals((short)0xffff, cpu.getState().getRegister(4));	// still no change -- just operand fetches
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x404, newPC);
 		
 	}
 	
	@Test
 	public void testInstrWithOneAddr() throws Exception {
 		writeInstruction(0x400, 0xd800, 0x00c0);	// MOVB R0, @>C0
 		
 		int curPC = cpu.getState().getPC();
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.createOperandFetch();
 		
 		assertEquals(0x400, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, WriteRegister.class, Cpu9900.REG_PC, origPC + 4);
 		assertChange(change, 1, ReadRegister.class, 0);
 		assertChange(change, 2, ReadByte.class, 0xc0);
 		
 		change.apply();
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x404, newPC);
 		
 	}

	@Test
 	public void testInstrWithTwoAddrs() throws Exception {
 		writeInstruction(0x400, 0xd820, 0x00c0, 0xff00);	// MOVB @>c0, @>ff00
 		
 		int curPC = cpu.getState().getPC();
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.createOperandFetch();
 		
 		assertEquals(0x400, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, WriteRegister.class, Cpu9900.REG_PC, origPC + 6);
 		assertChange(change, 1, ReadByte.class, 0xc0);
 		assertChange(change, 2, ReadByte.class, 0xff00);
 		
 		change.apply();
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x406, newPC);
 		assertEquals(0xff, ((ReadByte) change.getElement(1)).state.value);
 		assertEquals(0xff, ((ReadByte) change.getElement(2)).state.value);
 	}


	@Test
 	public void testInstrWithAutoInc() throws Exception {
 		writeInstruction(0x400, 0xcf03);	// MOV 3, *12+
 		
 		int curPC = cpu.getState().getPC();
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.createOperandFetch();
 		
 		assertEquals(0x400, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, WriteRegister.class, Cpu9900.REG_PC, origPC + 2);
 		assertChange(change, 1, ReadRegister.class, 3);
 		assertChange(change, 2, ReadIncrementRegister.class, origWP + 12 * 2);
 		
 		change.apply();
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x402, newPC);
 		assertEquals((short)0x0001, cpu.getState().getRegister(12));
 	}

	@Test
 	public void testInstrWithTwoAutoInc() throws Exception {
 		writeInstruction(0x400, 0xdcb1);	// MOVB *1+, *2+
 		
 		int curPC = cpu.getState().getPC();
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.createOperandFetch();
 		
 		assertEquals(0x400, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, WriteRegister.class, Cpu9900.REG_PC, origPC + 2);
 		assertChange(change, 1, ReadIncrementRegister.class, origWP + 1 * 2);
 		assertChange(change, 2, ReadIncrementRegister.class, origWP + 2 * 2);
 		
 		change.apply();
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x402, newPC);
 		assertEquals((short)0x0000, cpu.getState().getRegister(1));
 		assertEquals((short)0x0000, cpu.getState().getRegister(2));
 	}


	@Test
 	public void testInstrWithAutoIncModify1() throws Exception {
 		writeInstruction(0x400, 0xc33a);	// MOV *12+,12
 		cpu.getState().setRegister(12, origWP + 12*2);
 		
 		// this will set R12 to itself; increment is lost
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.createOperandFetch();
 		
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, WriteRegister.class, Cpu9900.REG_PC, origPC + 2);
 		assertChange(change, 1, ReadIncrementRegister.class, origWP + 12 * 2);
 		assertChange(change, 2, ReadRegister.class, 12);
 		
 		change.apply();
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x402, newPC);
 		assertEquals((short)(origWP + 12*2), cpu.getState().getRegister(12));
 	}

	@Test
 	public void testInstrWithAutoIncModify2() throws Exception {
 		writeInstruction(0x400, 0xcf3c);	// MOV *12+,*12+
 		cpu.getState().setRegister(12, origWP + 12*2);
 		
 		// read R12 (@R12)
 		// incr R12
 		// read R12 (@R13)
 		// incr R12 (@R14)
 		// sets R13 = @R12
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.createOperandFetch();
 		
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, WriteRegister.class, Cpu9900.REG_PC, origPC + 2);
 		assertChange(change, 1, ReadIncrementRegister.class, origWP + 12 * 2);
 		assertChange(change, 2, ReadIncrementRegister.class, origWP + 12 * 2);
 		
 		change.apply();
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x402, newPC);
 		assertEquals((short)(origWP + 14*2), cpu.getState().getRegister(12));
// 		assertEquals((short)(origWP + 12*2), cpu.getState().getRegister(13));	// MOV not executed
 		
 		assertEquals((short)(origWP + 12*2), ((ReadIncrementRegister) change.getElement(1)).state.value);
 	}

	
 	//@Test
 	public void testLi() throws Exception {
 		writeInstruction(0x400, 0x204, 0x123);	// LI R4,>123
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.createOperandFetch();
 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertEquals((short)0xffff, cpu.getState().getRegister(4));	// no change yet
 		
 		change.apply();
 		assertEquals((short)0x123, cpu.getState().getRegister(4));	// applied
 		
 		change.revert();
 		assertEquals((short)0xffff, cpu.getState().getRegister(4));	// original
 		
 	}

}
