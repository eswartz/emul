/*
  CycleCounter.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.cycler;

import java.io.PrintStream;

import v9t9.common.asm.IInstruction;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IExecutor;
import v9t9.common.machine.IMachine;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.CpuState9900;

/**
 * @author ejs
 *
 */
public class CycleCounter {
	private IMachine machine;
	private PrintStream out;
	private IExecutor executor;
	private int stopAddr;
	private ICpuState state;
	private ICpu cpu;
	private int numInstrs;

	public CycleCounter(IMachine machine, int startAddr, int stopAddr,
			int numInstrs, PrintStream out) {
		this.machine = machine;
		this.stopAddr = stopAddr;
		this.numInstrs = numInstrs;
		this.out = out;
		
		executor = machine.getExecutor();
		state = machine.getCpu().getState();
		cpu = machine.getCpu();

		if (machine.getCpu() instanceof Cpu9900) {
			((CpuState9900) state).setWP((short) 0x83e0);
			cpu.getConsole().writeWord(0x83e0 + 11 * 2, (short) 0);
		} 
		state.setPC((short) startAddr);
	}

	public void run() {
		CycleCounts counts = cpu.getCycleCounts();
		while (true) {
			if (stopAddr != 0 && (state.getPC() & 0xffff) == stopAddr)
				break;
			
			try {
				counts.getAndResetTotal();
				IInstruction instr = cpu.getInstructionFactory().decodeInstruction(
						state.getPC(), machine.getConsole());
				out.print(state + "; " + instr);
				executor.getInterpreter().executeChunk(1, executor);
				out.print("; F=" + counts.getFetch() + "; L=" + counts.getLoad() + 
						"; S=" + counts.getStore() + "; E="+counts.getExecute() +
						"; O="+ counts.getOverhead());
				out.println("; total=" + counts.getAndResetTotal());
				if (state.getPC() == 0)
					break;
			} catch (AbortedException e) {
				break;
			}
			
			if (numInstrs != 0) {
				if (--numInstrs == 0) {
					break;
				}
			}
		}
	}
	
}
