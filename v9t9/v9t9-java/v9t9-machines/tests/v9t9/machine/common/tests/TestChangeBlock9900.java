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
import v9t9.machine.ti99.cpu.Changes.AdvancePC;
import v9t9.machine.ti99.cpu.Changes.ConditionalAdvancePC;
import v9t9.machine.ti99.cpu.Changes.ReadIncrementRegister;
import v9t9.machine.ti99.cpu.Changes.ReadIndirectRegister;
import v9t9.machine.ti99.cpu.Changes.ReadRegister;
import v9t9.machine.ti99.cpu.Changes.ReadRegisterOffset;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;

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
		
		if (klass == AdvancePC.class) {
			assertEquals(args[0] & 0xffff, ((AdvancePC) el).value & 0xffff);
		}
		else if (klass == ReadRegister.class) {
			assertEquals(args[0] & 0xffff, ((MachineOperand9900)((ReadRegister) el).state.mop).val & 0xffff);
		}
		else if (klass == ConditionalAdvancePC.class) {
			assertEquals(args[0] & 0xffff, ((ConditionalAdvancePC) el).value & 0xffff);
			
		}
//		else if (klass == ReadWord.class) {
//			if (args.length > 0)
//				assertEquals(args[0] & 0xffff, ((ReadWord) el).state.ea & 0xffff);
//		}
//		else if (klass == WriteWord.class) {
//			assertEquals(args[0] & 0xffff, ((WriteWord) el).state.ea & 0xffff);
//			assertEquals(args[1] & 0xffff, ((WriteWord) el).state.value & 0xffff);
//		}
	}

	
 	@Test
 	public void testOpWithImmediate() throws Exception {
 		writeInstruction(origPC, 0x204, 0x123);	// LI R4,>123
 		
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		int curPC = cpu.getState().getPC();
 		
 		assertEquals(origPC, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(2, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 4);
 		assertChange(change, 1, ReadRegister.class, 4);
 		
 		assertEquals((short)0xffff, cpu.getState().getRegister(4));	// no change yet
 		
 		change.apply(cpu.getState());
 		assertEquals((short)0xffff, cpu.getState().getRegister(4));	// still no change -- just operand fetches
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x404, newPC);
 		
 	}
 	
	@Test
 	public void testOpWithOneAddr() throws Exception {
 		writeInstruction(0x400, 0xd800, 0x00c0);	// MOVB R0, @>C0
 		
 		int curPC = cpu.getState().getPC();
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(0x400, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 4);
 		assertChange(change, 1, ReadRegister.class, 0);
 		assertChange(change, 2, ReadRegisterOffset.class, 0xc0);
 		
 		change.apply(cpu.getState());
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x404, newPC);
 		
 	}

	@Test
 	public void testOpWithTwoAddrs() throws Exception {
 		writeInstruction(0x400, 0xd820, 0x00c0, 0xff00);	// MOVB @>c0, @>ff00
 		
 		int curPC = cpu.getState().getPC();
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(0x400, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 6);
 		assertChange(change, 1, ReadRegisterOffset.class, 0xc0);
 		assertChange(change, 2, ReadRegisterOffset.class, 0xff00);
 		
 		change.apply(cpu.getState());
 		
 		int newPC = cpu.getState().getPC();
 		assertEquals(0x406, newPC);
 		assertEquals(0xff, ((ReadRegisterOffset) change.getElement(1)).state.value);
 		assertEquals(0xff, ((ReadRegisterOffset) change.getElement(2)).state.value);
 	}


	@Test
 	public void testOpWithAutoInc() throws Exception {
 		writeInstruction(0x400, 0xcf03);	// MOV 3, *12+
 		
 		int curPC = cpu.getState().getPC();
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(0x400, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 2);
 		assertChange(change, 1, ReadRegister.class, 3);
 		assertChange(change, 2, ReadIncrementRegister.class, origWP + 12 * 2);
 		
 		change.apply(cpu.getState());
 		
 		assertEquals(0x402, cpu.getState().getPC());
 		assertEquals((short)0x0001, cpu.getState().getRegister(12));
 		
 		change.revert(cpu.getState());
 		assertEquals(0x400, cpu.getState().getPC());
 		assertEquals((short)-1, cpu.getState().getRegister(12));
 		
 	}

	@Test
 	public void testOpWithTwoAutoInc() throws Exception {
 		writeInstruction(0x400, 0xdcb1);	// MOVB *1+, *2+
 		
 		int curPC = cpu.getState().getPC();
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(0x400, curPC);		// no changes fetching

 		assertNotNull(change.inst);
 		assertNull(change.xInst);
 		
 		assertNotNull(change.last());
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 2);
 		assertChange(change, 1, ReadIncrementRegister.class, origWP + 1 * 2);
 		assertChange(change, 2, ReadIncrementRegister.class, origWP + 2 * 2);
 		
 		change.apply(cpu.getState());
 		
 		assertEquals(0x402, (int) cpu.getState().getPC());
 		assertEquals((short)0x0000, cpu.getState().getRegister(1));
 		assertEquals((short)0x0000, cpu.getState().getRegister(2));
 		
 		change.revert(cpu.getState());
 		assertEquals(0x400, (int) cpu.getState().getPC());
 		assertEquals((short)-1, cpu.getState().getRegister(1));
 		assertEquals((short)-1, cpu.getState().getRegister(2));
 	}


	@Test
 	public void testOpWithAutoIncModify1() throws Exception {
 		writeInstruction(0x400, 0xc33a);	// MOV *12+,12
 		cpu.getState().setRegister(12, origWP + 12*2);
 		
 		// this will set R12 to itself; increment is lost
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 2);
 		assertChange(change, 1, ReadIncrementRegister.class, origWP + 12 * 2);
 		assertChange(change, 2, ReadRegister.class, 12);
 		
 		change.apply(cpu.getState());
 		
 		assertEquals(0x402, (int) cpu.getState().getPC());
 		assertEquals((short)(origWP + 12*2), cpu.getState().getRegister(12));
 		
 		change.revert(cpu.getState());
 		assertEquals(0x400, (int) cpu.getState().getPC());
 		assertEquals((short)(origWP + 12*2), cpu.getState().getRegister(12));
 	}

	@Test
 	public void testOpWithAutoIncModify2() throws Exception {
 		writeInstruction(0x400, 0xcf3c);	// MOV *12+,*12+
 		cpu.getState().setRegister(12, origWP + 12*2);
 		
 		// read R12 (@R12)
 		// incr R12
 		// read R12 (@R13)
 		// incr R12 (@R14)
 		// sets R13 = @R12
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(3, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 2);
 		assertChange(change, 1, ReadIncrementRegister.class, origWP + 12 * 2);
 		assertChange(change, 2, ReadIncrementRegister.class, origWP + 12 * 2);
 		
 		change.apply(cpu.getState());
 		
 		assertEquals(0x402, (int) cpu.getState().getPC());
 		assertEquals((short)(origWP + 14*2), cpu.getState().getRegister(12));
// 		assertEquals((short)(origWP + 12*2), cpu.getState().getRegister(13));	// MOV not executed
 		
 		assertEquals((short)(origWP + 12*2), ((ReadIncrementRegister) change.getElement(1)).state.value);
 		
 		change.revert(cpu.getState());
 		
 		assertEquals(0x400, (int) cpu.getState().getPC());
 		assertEquals((short)(origWP + 12*2), cpu.getState().getRegister(12));
 		assertEquals((short)-1, cpu.getState().getRegister(13));

 	}


	@Test
 	public void testOpJump() throws Exception {
 		writeInstruction(0x400, 0x10fe);	// JMP $-2

 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(2, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 2);
 		assertChange(change, 1, AdvancePC.class, -4);
 		
 		change.apply(cpu.getState());
 		
 		assertEquals(0x3fe, (int) cpu.getState().getPC());
 		
 		change.revert(cpu.getState());
 		
 		assertEquals(0x400, (int) cpu.getState().getPC());

 	}

	@Test
 	public void testOpJumpCond() throws Exception {
 		writeInstruction(0x400, 0x137f);	// JEQ $+256

 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(2, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 2);
 		assertChange(change, 1, ConditionalAdvancePC.class, 254);
 		
 		//// taken
 		
 		cpu.getState().setST((short) 0x2000);
 		change.apply(cpu.getState());
 		
 		assertEquals(0x500, (int) cpu.getState().getPC());
 		
 		change.revert(cpu.getState());
 		
 		assertEquals(0x400, (int) cpu.getState().getPC());

 		
 		//// not taken
 		
 		cpu.getState().setST((short) 0);
 		change.apply(cpu.getState());
 		
 		assertEquals(0x402, (int) cpu.getState().getPC());
 		
 		change.revert(cpu.getState());
 		
 		assertEquals(0x400, (int) cpu.getState().getPC());
 	}

	@Test
 	public void testOpBranch() throws Exception {
 		writeInstruction(0x400, 0x45b);	// B *R11
 		cpu.getState().setRegister(11, 0x800);

 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.appendOperandFetch();
 		
 		assertEquals(2, change.getCount());
 		assertChange(change, 0, AdvancePC.class, 2);
 		assertChange(change, 1, ReadIndirectRegister.class, 11);
 		
 		change.apply(cpu.getState());
 		
// 		assertEquals(0x800, (int) cpu.getState().getPC());
 		
 		change.revert(cpu.getState());
 		
 		assertEquals(0x400, (int) cpu.getState().getPC());

 	}

 	@Test
 	public void testInstrLi() throws Exception {
 		writeInstruction(0x400, 0x204, 0x123);	// LI R4,>123
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();
 		
 		assertEquals((short)0xffff, cpu.getState().getRegister(4));	// no change yet
 		
 		cpu.getState().setST((short) 0x0024);
 		
 		change.apply(cpu.getState());
 		assertEquals((short)0x123, cpu.getState().getRegister(4));	
 		assertEquals(0x404, (int) cpu.getState().getPC()); 	
 		
 		assertTrue(cpu.getState().getStatus().isGT());
 		assertTrue(cpu.getState().getStatus().isH());
 		assertFalse(cpu.getState().getStatus().isC());
 		assertEquals(4, cpu.getState().getStatus().getIntMask());
 		
 		//
 		change.revert(cpu.getState());
 		assertEquals((short)-1, cpu.getState().getRegister(4));	
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 		
 		assertEquals((short) 0x0024, cpu.getState().getStatus().flatten());
 	}

 	@Test
 	public void testInstrAi() throws Exception {
 		writeInstruction(0x400, 0x224, 0x123);	// AI R4,>123
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();
 		
 		cpu.getState().setST((short) 0x0024);
 		
 		change.apply(cpu.getState());
 		
 		assertEquals((short)0x122, cpu.getState().getRegister(4));	
 		assertEquals(0x404, (int) cpu.getState().getPC()); 	
 		
 		assertTrue(cpu.getState().getStatus().isGT());
 		assertTrue(cpu.getState().getStatus().isH());
 		assertTrue(cpu.getState().getStatus().isC());
 		assertEquals(4, cpu.getState().getStatus().getIntMask());
 		
 		//
 		change.revert(cpu.getState());
 		assertEquals((short)-1, cpu.getState().getRegister(4));	
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 		
 		assertEquals((short) 0x0024, cpu.getState().getStatus().flatten());
 	}

 	@Test
 	public void testInstrCi() throws Exception {
 		writeInstruction(0x400, 0x284, 0x123);	// CI R4,>123
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();
 		
 		cpu.getState().setST((short) 0xe024);
 		
 		change.apply(cpu.getState());
 		
 		assertEquals((short)-1, cpu.getState().getRegister(4));	// no change	
 		assertEquals(0x404, (int) cpu.getState().getPC()); 	
 		
 		assertFalse(cpu.getState().getStatus().isGT());
 		assertTrue(cpu.getState().getStatus().isH());
 		assertFalse(cpu.getState().getStatus().isC());
 		assertEquals(4, cpu.getState().getStatus().getIntMask());
 		
 		//
 		change.revert(cpu.getState());
 		assertEquals((short)-1, cpu.getState().getRegister(4));	
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 		
 		assertEquals((short) 0xe024, cpu.getState().getStatus().get());
 	}
 	

 	@Test
 	public void testInstrLwpi() throws Exception {
 		writeInstruction(0x400, 0x2e0, 0xcc01);	// LWPI >CC01
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();
 		
 		change.apply(cpu.getState());
 		
 		assertEquals((short) 0xcc01, ((CpuState9900) cpu.getState()).getWP());		
 		assertEquals(0x404, (int) cpu.getState().getPC()); 	
 		
 		//
 		change.revert(cpu.getState());
 		assertEquals((short)origWP, ((CpuState9900) cpu.getState()).getWP());		
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 	}
 	

 	@Test
 	public void testInstrRt() throws Exception {
 		writeInstruction(0x400, 0x45b);	// B *R11
 		cpu.getState().setRegister(11, 0x800);
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();
 		
 		change.apply(cpu.getState());
 		
 		assertEquals((short) 0x800, cpu.getState().getRegister(11));	// no change		
 		assertEquals(0x800, (int) cpu.getState().getPC()); 	
 		
 		//
 		change.revert(cpu.getState());
 		
 		assertEquals((short) 0x800, cpu.getState().getRegister(11));			
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 	}

 	@Test
 	public void testInstrBl() throws Exception {
 		writeInstruction(0x400, 0x691);	// BL *1
 		cpu.getState().setRegister(11, 0x800);
 		cpu.getState().setRegister(1, 0xc00);
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();

 		change.apply(cpu.getState());
 		
 		assertEquals((short) 0x402, cpu.getState().getRegister(11));		
 		assertEquals(0xc00, (int) cpu.getState().getPC()); 	
 		
 		//
 		change.revert(cpu.getState());
 		
 		assertEquals((short) 0x800, cpu.getState().getRegister(11));			
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 	}

 	@Test
 	public void testInstrBlR11() throws Exception {
 		writeInstruction(0x400, 0x69B);	// BL *11
 		cpu.getState().setRegister(11, 0x800);
 		
 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();

 		change.apply(cpu.getState());
 		
 		assertEquals((short) 0x402, cpu.getState().getRegister(11));		
 		assertEquals(0x800, (int) cpu.getState().getPC()); 	
 		
 		//
 		change.revert(cpu.getState());
 		
 		assertEquals((short) 0x800, cpu.getState().getRegister(11));			
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 	}
 	

 	@Test
 	public void testInstrBlwp() throws Exception {
 		writeInstruction(0x400, 0x420, 0x800);	// BLWP @>800
 		cpu.getConsole().writeWord(0x800, (short) 0x9000);
 		cpu.getConsole().writeWord(0x802, (short) 0x804);
 		
 		cpu.getState().setRegister(11, 0x0);
 		cpu.getState().setRegister(13, 0x1);	
 		cpu.getState().setRegister(14, 0x2);
 		cpu.getState().setRegister(15, 0x3);

 		cpu.getConsole().writeWord(0x9000 + 11*2, (short) 0x10);
 		cpu.getConsole().writeWord(0x9000 + 13*2, (short) 0x11);
 		cpu.getConsole().writeWord(0x9000 + 14*2, (short) 0x12);
 		cpu.getConsole().writeWord(0x9000 + 15*2, (short) 0x13);

 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();

 		cpu.getState().setST((short) 0x1034);
 		change.apply(cpu.getState());
 		
 		// no change
 		assertEquals(0x0, cpu.getConsole().readWord(origWP + 11 * 2));		
 		assertEquals(0x1, cpu.getConsole().readWord(origWP + 13 * 2)); 	
 		assertEquals(0x2, cpu.getConsole().readWord(origWP + 14 * 2)); 	
 		assertEquals(0x3, cpu.getConsole().readWord(origWP + 15 * 2)); 	
 		assertEquals((short) 0x9000, cpu.getConsole().readWord(0x800));
 		assertEquals((short) 0x804, cpu.getConsole().readWord(0x802));
 		
 		// actual context switch 
 		assertEquals(0x804, cpu.getState().getPC()); 	
 		assertEquals((short) 0x9000, ((CpuState9900) cpu.getState()).getWP());
 		assertEquals(0x1034, cpu.getState().getST()); 	
 		
 		assertEquals((short) origWP, (int) cpu.getState().getRegister(13)); 	
 		assertEquals(0x404, (int) cpu.getState().getRegister(14)); 	
 		assertEquals(0x1034, (int) cpu.getState().getRegister(15)); 	
 		
 		//
 		change.revert(cpu.getState());
 		
 		assertEquals((short) origWP, ((CpuState9900) cpu.getState()).getWP()); 	
 		assertEquals(0x0, cpu.getState().getRegister(11));			
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 		
 		assertEquals(0, cpu.getState().getRegister(11));
 		assertEquals(1, cpu.getState().getRegister(13));
 		assertEquals(2, cpu.getState().getRegister(14));
 		assertEquals(3, cpu.getState().getRegister(15));
 		
 		// and old WS restored 
 		assertEquals(0x10, cpu.getConsole().readWord(0x9000 + 11 * 2));
 		assertEquals(0x11, cpu.getConsole().readWord(0x9000 + 13 * 2));
 		assertEquals(0x12, cpu.getConsole().readWord(0x9000 + 14 * 2));
 		assertEquals(0x13, cpu.getConsole().readWord(0x9000 + 15 * 2));

 	}
 	

 	@Test
 	public void testInstrRtwp() throws Exception {
 		writeInstruction(0x400, 0x380);	// RTWP
 		
 		cpu.getState().setRegister(11, 0x0);
 		cpu.getState().setRegister(13, 0x5000);	
 		cpu.getState().setRegister(14, 0x204);
 		cpu.getState().setRegister(15, 0xffff);

 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();

 		cpu.getState().setST((short) 0x1034);
 		change.apply(cpu.getState());
 		
 		// no change
 		assertEquals(0x0, cpu.getConsole().readWord(origWP + 11 * 2));		
 		assertEquals(0x5000, cpu.getConsole().readWord(origWP + 13 * 2)); 	
 		assertEquals(0x204, cpu.getConsole().readWord(origWP + 14 * 2)); 	
 		assertEquals((short)0xffff, cpu.getConsole().readWord(origWP + 15 * 2)); 	
 		
 		// actual context switch 
 		assertEquals(0x204, cpu.getState().getPC()); 	
 		assertEquals((short) 0x5000, ((CpuState9900) cpu.getState()).getWP());
 		assertEquals((short) 0xffff, cpu.getState().getStatus().get()); 	
 		
 		//
 		change.revert(cpu.getState());
 		
 		assertEquals((short) origWP, ((CpuState9900) cpu.getState()).getWP()); 	
 		assertEquals(0x0, cpu.getState().getRegister(11));			
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 		
 		assertEquals(0, cpu.getState().getRegister(11));
 		assertEquals(0x5000, cpu.getState().getRegister(13));
 		assertEquals(0x204, cpu.getState().getRegister(14));
 		assertEquals((short) 0xffff, cpu.getState().getRegister(15));

 	}
 	
 	@Test
 	public void testInstrXop() throws Exception {
 		// rSP	*R	TERM^	,XOP  	#t.ttystr ,
 		writeInstruction(0x400, 0x2c5a);	// XOP *10, 1
 		
 		cpu.getConsole().writeWord(0x44, (short) 0x9000);
 		cpu.getConsole().writeWord(0x46, (short) 0x804);
 		
 		cpu.getState().setRegister(10, 0x4000);
 		cpu.getState().setRegister(11, 0x0);
 		cpu.getState().setRegister(13, 0x1);	
 		cpu.getState().setRegister(14, 0x2);
 		cpu.getState().setRegister(15, 0x3);

 		cpu.getConsole().writeWord(0x9000 + 11*2, (short) 0x10);
 		cpu.getConsole().writeWord(0x9000 + 13*2, (short) 0x11);
 		cpu.getConsole().writeWord(0x9000 + 14*2, (short) 0x12);
 		cpu.getConsole().writeWord(0x9000 + 15*2, (short) 0x13);

 		ChangeBlock9900 change = new ChangeBlock9900(cpu);
 		change.generate();

 		cpu.getState().setST((short) 0x1034);
 		change.apply(cpu.getState());
 		
 		// no change
 		assertEquals(0x4000, cpu.getConsole().readWord(origWP + 10 * 2));		
 		assertEquals(0x0, cpu.getConsole().readWord(origWP + 11 * 2));		
 		assertEquals(0x1, cpu.getConsole().readWord(origWP + 13 * 2)); 	
 		assertEquals(0x2, cpu.getConsole().readWord(origWP + 14 * 2)); 	
 		assertEquals(0x3, cpu.getConsole().readWord(origWP + 15 * 2)); 	
 		assertEquals((short) 0x9000, cpu.getConsole().readWord(0x44));
 		assertEquals((short) 0x804, cpu.getConsole().readWord(0x46));
 		
 		// actual context switch 
 		assertEquals((short) 0x9000, ((CpuState9900) cpu.getState()).getWP()); 	
 		assertEquals((short) 0x1234, ((CpuState9900) cpu.getState()).getST());	// XOP bit 	
 		assertEquals(0x804, cpu.getState().getPC()); 	
 		
 		assertEquals((short) origWP, (int) cpu.getState().getRegister(13)); 	
 		assertEquals(0x402, (int) cpu.getState().getRegister(14)); 	
 		assertEquals(0x1034, (int) cpu.getState().getRegister(15));	// no XOP bit
 		
 		// argument -- address in *R15
 		assertEquals((short) 0x4000, (int) cpu.getState().getRegister(11)); 	
 		
 		//
 		change.revert(cpu.getState());
 		
 		assertEquals((short) origWP, ((CpuState9900) cpu.getState()).getWP()); 	
 		assertEquals((short) 0x1034, ((CpuState9900) cpu.getState()).getST()); 	
 		assertEquals(0x0, cpu.getState().getRegister(11));			
 		assertEquals(0x400, (int) cpu.getState().getPC()); 	
 		
 		assertEquals(0x4000, cpu.getState().getRegister(10));
 		assertEquals(0, cpu.getState().getRegister(11));
 		assertEquals(1, cpu.getState().getRegister(13));
 		assertEquals(2, cpu.getState().getRegister(14));
 		assertEquals(3, cpu.getState().getRegister(15));
 		
 		// and old WS restored 
 		assertEquals(0x10, cpu.getConsole().readWord(0x9000 + 11 * 2));
 		assertEquals(0x11, cpu.getConsole().readWord(0x9000 + 13 * 2));
 		assertEquals(0x12, cpu.getConsole().readWord(0x9000 + 14 * 2));
 		assertEquals(0x13, cpu.getConsole().readWord(0x9000 + 15 * 2));

 	}
}
