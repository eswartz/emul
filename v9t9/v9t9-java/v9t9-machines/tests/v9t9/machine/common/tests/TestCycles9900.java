/*
  TestInsts9900.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.machine.ti99.machine.StandardMachineModel;
import v9t9.machine.ti99.memory.ExpRamArea;

public class TestCycles9900  {
	
	private IMachine machine;
	private ISettingsHandler settings;
	private CpuState9900 cpuState;

	@Before
	public void setup() throws Exception {
        IMachineModel model = new StandardMachineModel();
        assertNotNull(model);
        settings = new BasicSettingsHandler();
        machine = model.createMachine(settings);
        settings.get(ExpRamArea.settingExpRam).setBoolean(true);
        cpuState = (CpuState9900) machine.getCpu().getState();
	}
	
	@Test
	public void testLimi() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x0300);	 // LIMI >0
		machine.getConsole().writeWord(0x8302, (short) 0x0000);	 
		machine.getConsole().writeWord(0xa000, (short) 0x0300);	 // LIMI >0
		machine.getConsole().writeWord(0xa002, (short) 0x0000);	 
		
		// minimal
		int min = 16;
		assertCycles(0x83e0, 0x8300, min);
		
		// fetch instr from slow
		assertCycles(0x83e0, 0xA000, min + 8);
		
		// fetch ops from slow 
		assertCycles(0xA020, 0x8300, min);
		
		// fetch instr & ops from slow
		assertCycles(0xA020, 0xA000, min + 8);
	}
	@Test
	public void testClr() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x4c2);	 // CLR R2
		machine.getConsole().writeWord(0xa000, (short) 0x4c2);	 // CLR R2
		
		// minimal
		int min = 10;
		assertCycles(0x83e0, 0x8300, min);
		
		// fetch instr from slow
		assertCycles(0x83e0, 0xA000, min + 4);
		
		// fetch ops from slow 
		assertCycles(0xA020, 0x8300, min + 8);
		
		// fetch instr & ops from slow
		assertCycles(0xA020, 0xA000, min + 4 + 8);
	}
	

	@Test
	public void testClrMem() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x4e0);	 // CLR @>83e0
		machine.getConsole().writeWord(0x8302, (short) 0x83e0);	 
		machine.getConsole().writeWord(0x8310, (short) 0x4e0);	 // CLR @>f000
		machine.getConsole().writeWord(0x8312, (short) 0xf000);	 //
		
		machine.getConsole().writeWord(0xA000, (short) 0x4e0);	 // CLR @>83e0
		machine.getConsole().writeWord(0xA002, (short) 0x83e0);	 
		machine.getConsole().writeWord(0xa010, (short) 0x4e0);	 // CLR @>f000
		machine.getConsole().writeWord(0xa012, (short) 0xf000);	 // 
		
		int min = 10 + 8 /* @>xxxx */;
		
		// minimal
		assertCycles(0x83e0, 0x8300, min);
		
		// fetch instr + op immed from slow
		assertCycles(0x83e0, 0xA000, min + 8);
		
		// fetch ops from slow 
		assertCycles(0x83e0, 0x8310, min + 8);
		
		// fetch instr & ops from slow
		assertCycles(0x83e0, 0xA010, min + 8 + 8);
	}
	

	@Test
	public void testClrInd() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x4D0);	 // CLR *R0
		machine.getConsole().writeWord(0x8310, (short) 0x4D1);	 // CLR *R1
		machine.getConsole().writeWord(0xA000, (short) 0x4D0);	 // CLR *R0
		machine.getConsole().writeWord(0xA010, (short) 0x4D1);	 // CLR *R1
		
		// R0 and R1
		machine.getConsole().writeWord(0x83e0, (short) 0x83c0);	 
		machine.getConsole().writeWord(0x83e2, (short) 0xF000);	 
		machine.getConsole().writeWord(0xA020, (short) 0x83c0);	 
		machine.getConsole().writeWord(0xA022, (short) 0xF000);	 
		
		int min = 10 + 4 /* *Rx */;
		
		// minimal:  R2 and *R2 in fast
		assertCycles(0x83e0, 0x8300, min);
		// *R2 in slow
		assertCycles(0x83e0, 0x8310, min + 8);
		
		// fetch instr from slow
		assertCycles(0x83e0, 0xA000, min + 4);
		// *R2 in slow
		assertCycles(0x83e0, 0xA010, min + 4 + 8);
		
		// fetch ops from slow 
		assertCycles(0xa020, 0x8300, min + 4);
		// *R2 in slow
		assertCycles(0xa020, 0x8310, min + 4 + 8);
		
		// fetch instr & ops from slow
		assertCycles(0xa020, 0xA000, min + 4 + 4);
		// *R2 in slow 
		assertCycles(0xa020, 0xA010, min + 4 + 4 + 8);
	}
	
	@Test
	public void testSrl() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x942);	 // SRL R2, 4
		machine.getConsole().writeWord(0xa000, (short) 0x942);	 // SRL R2, 4
		
		// minimal
		int min = 12 + 2 * 4 /* per shift */;
		assertCycles(0x83e0, 0x8300, min);
		
		// fetch instr from slow
		assertCycles(0x83e0, 0xA000, min + 4);
		
		// fetch ops from slow 
		assertCycles(0xA020, 0x8300, min + 8);
		
		// fetch instr & ops from slow
		assertCycles(0xA020, 0xA000, min + 4 + 8);
	}
	
	@Test
	public void testSrlVar() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x902);	 // SRL R2, 0
		machine.getConsole().writeWord(0xa000, (short) 0x902);	 // SRL R2, 0
		
		machine.getConsole().writeWord(0x83e0, (short) 5);	 
		machine.getConsole().writeWord(0xA020, (short) 5);
		
		// minimal
		int min = 20 + 2 * 5 /* per shift */;
		assertCycles(0x83e0, 0x8300, min);
		
		// fetch instr from slow
		assertCycles(0x83e0, 0xA000, min + 4);
		
		// fetch ops from slow 
		assertCycles(0xA020, 0x8300, min + 8 + 4);
		
		// fetch instr & ops from slow
		assertCycles(0xA020, 0xA000, min + 4 + 8 + 4);
	}
	
	
	@Test
	public void testMovVdp() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0xD804);	 // MOVB R4, @>837C
		machine.getConsole().writeWord(0x8302, (short) 0x837C);	 
		machine.getConsole().writeWord(0xA000, (short) 0xD804);	 // MOVB R4, @>837C
		machine.getConsole().writeWord(0xA002, (short) 0x837C);	 
		
		int min = 14 + 8 /* @>... */;
		
		// minimal: opc and dest in fast
		assertCycles(0x83e0, 0x8300, min);
		
		// opc in slow
		assertCycles(0x83e0, 0xA000, min + 8);
		
		// wp in slow
		assertCycles(0xa020, 0x8300, min + 4);
		
		// wp + opc in slow
		assertCycles(0xa020, 0xA000, min + 8 + 4);
	}
	

	@Test
	public void testLdcr() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x31C1);	 // LDCR R1,7
		machine.getConsole().writeWord(0xa000, (short) 0x31C1);	 // LDCR R1,7
		
		// minimal
		int min = 20 + 2 * 7 /* per bit */;
		assertCycles(0x83e0, 0x8300, min);
		
		// fetch instr from slow
		assertCycles(0x83e0, 0xA000, min + 4);
		
		// fetch ops from slow (read R1, R12)
		assertCycles(0xA020, 0x8300, min + 4 + 4);
		
		// fetch instr & ops from slow (read R1, R12)
		assertCycles(0xA020, 0xA000, min + 4 + 4 + 4);
	}

	@Test
	public void testStcr() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x35C1);	 // STCR R1,7
		machine.getConsole().writeWord(0xa000, (short) 0x35C1);	 // STCR R1,7
		
		// minimal
		int min = 42;
		assertCycles(0x83e0, 0x8300, min);
		
		// fetch instr from slow
		assertCycles(0x83e0, 0xA000, min + 4);
		
		// fetch ops from slow (read R1, R12, write R1)
		assertCycles(0xA020, 0x8300, min + 4 + 4 + 4);
		
		// fetch instr & ops from slow (read R1, R12, write R1)
		assertCycles(0xA020, 0xA000, min + 4 + 4 + 4 + 4);
	}
	@Test
	public void testStcrAutoInc() throws Exception {
		machine.getConsole().writeWord(0x8300, (short) 0x3531);	 // STCR *R1+,4
		machine.getConsole().writeWord(0x8302, (short) 0x3532);	 // STCR *R2+,4
		machine.getConsole().writeWord(0x8310, (short) 0x3431);	 // STCR *R1+,0  // 16
		machine.getConsole().writeWord(0x8312, (short) 0x3432);	 // STCR *R2+,0  // 16
		machine.getConsole().writeWord(0xa000, (short) 0x3531);	 // STCR *R1+,4
		machine.getConsole().writeWord(0xa002, (short) 0x3532);	 // STCR *R2+,4
		machine.getConsole().writeWord(0xa010, (short) 0x3431);	 // STCR *R1+,0  // 16
		machine.getConsole().writeWord(0xa012, (short) 0x3432);	 // STCR *R2+,0  // 16
		
		// minimal
		int min4 = 42 /* 1-7 */ + 6 /* *R1+ @ byte */;
		int min16 = 60 /* 16 */ + 8 /* *R1+ @ word */;
		
		machine.getConsole().writeWord(0x83E2, (short) 0x83A0);	
		machine.getConsole().writeWord(0x83E4, (short) 0xF000);
		machine.getConsole().writeWord(0xA022, (short) 0x83A0);	
		machine.getConsole().writeWord(0xA024, (short) 0xF000);
		
		// all regs fast
		assertCycles(0x83e0, 0x8300, min4);
		assertCycles(0x83e0, 0x8310, min16);
		// store partial to slow (4 + 4)
		assertCycles(0x83e0, 0x8302, min4 + 8);
		assertCycles(0x83e0, 0x8312, min16 + 8);
		
		// fetch instr from slow, *R1 fast
		assertCycles(0x83e0, 0xA000, min4 + 4);
		assertCycles(0x83e0, 0xA010, min16 + 4);
		// *R1 slow
		assertCycles(0x83e0, 0xA002, min4 + 4 + 8);
		assertCycles(0x83e0, 0xA012, min16 + 4 + 8);
		
		// fetch ops from slow, read R12, r/w slow R1
		assertCycles(0xA020, 0x8300, min4 + 4 + 8);
		assertCycles(0xA020, 0x8310, min16 + 4 + 8);
		// *R1 slow
		assertCycles(0xA020, 0x8302, min4 + 4 + 8 + 8);
		assertCycles(0xA020, 0x8312, min16 + 4 + 8 + 8);
		
		// fetch instr & ops from slow
		assertCycles(0xA020, 0xA000, min4 + 4 + 4 + 8);
		assertCycles(0xA020, 0xA010, min16 + 4 + 4 + 8);
		// *R1 slow
		assertCycles(0xA020, 0xA002, min4 + 4 + 4 + 8 + 8);
		assertCycles(0xA020, 0xA012, min16 + 4 + 4 + 8 + 8);
	}
	private void assertCycles(int wp, int pc, int expCycles) {
		cpuState.getStatus().expand((short) 0);
		cpuState.setWP((short) wp);
		cpuState.setPC((short) pc);
		
		CycleCounts counts = machine.getCpu().getCycleCounts();
		counts.getAndResetTotal();
		machine.getExecutor().getInterpreter().executeChunk(1, machine.getExecutor());
		assertEquals(counts.toString(), expCycles, counts.getTotal());
		
	}
}
