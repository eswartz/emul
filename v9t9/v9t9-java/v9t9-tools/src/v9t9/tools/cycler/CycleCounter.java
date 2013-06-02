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

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.AbortedException;
import v9t9.common.cpu.CycleCounts;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IExecutor;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.machine.IMachine;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.tools.utils.ToolUtils;

/**
 * @author ejs
 *
 */
public class CycleCounter {
	private PrintStream out = System.out;
	private IExecutor executor;
	private int stopAddr;
	private ICpuState state;
	private ICpu cpu;
	private int numInstrs;
	private boolean stopAtSelfJump = true; 
	
	private boolean total;
	private IMachine machine;
	private int startAddr;

	public CycleCounter(IMachine machine) {
		this.machine = machine;
		executor = machine.getExecutor();
		state = machine.getCpu().getState();
		cpu = machine.getCpu();
		executor.addInstructionListener(new IInstructionListener() {
			
			@Override
			public boolean preExecute(InstructionWorkBlock before) {
				return true;
			}
			
			@Override
			public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
				
			}
		});
	}
	

	public void run() {
		if (machine.getCpu() instanceof Cpu9900) {
			((CpuState9900) state).setWP((short) 0x83e0);
			cpu.getConsole().writeWord(0x83e0 + 11 * 2, (short) 0);
		}
		state.setPC((short) startAddr);
		
		CycleCounts counts = cpu.getCycleCounts();
		StringBuilder sb = new StringBuilder();
		int totalNum = 0;
		int prevPC = state.getPC();
		
		while (true) {
			RawInstruction instr = null;
			if (stopAddr != 0 && (state.getPC() & 0xffff) == stopAddr)
				break;
			
			try {
				instr = cpu.getCurrentInstruction();
				if (!total)
					execInstrAndDump(counts, sb, instr);
				else
					executor.getInterpreter().executeChunk(1, executor);
				if (state.getPC() == 0)
					break;
			} catch (AbortedException e) {
				break;
			}
			
			totalNum++;
			
			if (stopAtSelfJump) {
				if (state.getPC() == prevPC)
					break;
			}
			else if (numInstrs != 0) {
				if (totalNum >= numInstrs) {
					break;
				}
			}
			
			prevPC = state.getPC();
		}
		
		if (total) {
			sb.append("count=").append(totalNum);
			sb.append("; F=").append(counts.getFetch());
			sb.append("; L=").append(counts.getLoad());
			sb.append("; S=").append(counts.getStore());
			sb.append("; E=").append(counts.getExecute());
			sb.append("; O=").append(counts.getOverhead());
			int total = counts.getAndResetTotal();
			sb.append("; total=").append(total);
			sb.append("; time=").append(Double.valueOf(total)/machine.getCpu().getBaseCyclesPerSec()).append(" sec");
			out.println(sb);
		}
	}

	protected void execInstrAndDump(CycleCounts counts, StringBuilder sb,
			RawInstruction instr) {
		sb.setLength(0);
		sb.append(state).append("; ");

		ToolUtils.appendInstructionCode(machine, sb, instr);
		sb.append("\t").append(instr);
		
		counts.getAndResetTotal();
		executor.getInterpreter().executeChunk(1, executor);
		sb.append("; F=").append(counts.getFetch());
		sb.append("; L=").append(counts.getLoad());
		sb.append("; S=").append(counts.getStore());
		sb.append("; E=").append(counts.getExecute());
		sb.append("; O=").append(counts.getOverhead());
		sb.append("; total=").append(counts.getAndResetTotal());
		out.println(sb);
	}

	/**
	 * @param addr
	 */
	public void setStartAddress(int addr) {
		this.startAddr = addr;
	}

	/**
	 * @param parseHexInt
	 */
	public void setStopAddress(int addr) {
		this.stopAddr = addr;
		stopAtSelfJump = false;
	}

	/**
	 * @param out2
	 */
	public void setPrintStream(PrintStream out) {
		this.out = out;
		
	}

	/**
	 * @param count
	 */
	public void setInstructionCount(int count) {
		numInstrs = count;
		stopAtSelfJump = count != 0;
	}

	/**
	 * @param total
	 */
	public void setShowTotal(boolean total) {
		this.total = total;
		
	}

}
