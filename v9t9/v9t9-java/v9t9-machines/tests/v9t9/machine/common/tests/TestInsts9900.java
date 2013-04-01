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
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.settings.BasicSettingsHandler;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.machine.ti99.machine.StandardMachineModel;
import v9t9.machine.ti99.memory.ExpRamArea;

public class TestInsts9900  {
	
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
	public void testDiv() throws Exception {
		machine.getConsole().writeWord(0xa000, (short) 0x3c80);	 // DIV R0,R2
		cpuState.setWP((short) 0x8300);
		
		assertDiv(0x10000, 0x7ffe, 2, 4);
		assertDiv(0x20000, 0x7ffe, 4, 8);
		assertDiv(0x1000, 0x7ffe, 0, 0x1000);
		
		assertDiv(0x1ffff, 0xfffe, 2, 3);
		assertDiv(0xfffdffff, 0xfffe, 0xffff, 0xfffd);

	}

	/**
	 * @param i
	 * @param j
	 * @param k
	 * @param l
	 */
	private void assertDiv(int dividend, int divisor, int expQuotient, int expRemainder) {
		// R0
		machine.getConsole().writeWord(0x8300, (short) divisor);
		// R2
		machine.getConsole().writeWord(0x8304, (short) (dividend >> 16));
		// R3
		machine.getConsole().writeWord(0x8306, (short) (dividend));
		
		cpuState.setPC((short) 0xa000);
		machine.getExecutor().getInterpreter().executeChunk(1, machine.getExecutor());
		
		// R2
		int quotient = machine.getConsole().readWord(0x8304) & 0xffff;
		// R3
		int remainder = machine.getConsole().readWord(0x8306) & 0xffff;
		
		assertEquals(expQuotient, quotient);
		assertEquals(expRemainder, remainder);
		
	}
}
