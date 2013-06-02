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

import ejs.base.utils.HexUtils;

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

/**
 * @author ejs
 *
 */
public class CycleCounter {
	private PrintStream out;
	private IExecutor executor;
	private int stopAddr;
	private ICpuState state;
	private ICpu cpu;
	private int numInstrs;
	private int maxLength;
	private boolean total;

	public CycleCounter(IMachine machine, int startAddr, int stopAddr,
			int numInstrs, boolean total, PrintStream out) {
		this.stopAddr = stopAddr;
		this.numInstrs = numInstrs;
		this.total = total;
		this.out = out;
		
		executor = machine.getExecutor();
		state = machine.getCpu().getState();
		cpu = machine.getCpu();

		if (machine.getCpu() instanceof Cpu9900) {
			((CpuState9900) state).setWP((short) 0x83e0);
			cpu.getConsole().writeWord(0x83e0 + 11 * 2, (short) 0);
			maxLength = 6; 
		}  else {
			maxLength = 2;
		}
		state.setPC((short) startAddr);
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
		CycleCounts counts = cpu.getCycleCounts();
		StringBuilder sb = new StringBuilder();
		int totalNum = 0;
		while (true) {
			if (stopAddr != 0 && (state.getPC() & 0xffff) == stopAddr)
				break;
			
			try {
				RawInstruction instr = cpu.getCurrentInstruction();
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
			
			if (numInstrs != 0) {
				if (totalNum >= numInstrs) {
					break;
				}
			}
		}
		
		if (total) {
			sb.append("# instructions=").append(totalNum);
			sb.append("; F=").append(counts.getFetch());
			sb.append("; L=").append(counts.getLoad());
			sb.append("; S=").append(counts.getStore());
			sb.append("; E=").append(counts.getExecute());
			sb.append("; O=").append(counts.getOverhead());
			sb.append("; total=").append(counts.getAndResetTotal());
			out.println(sb);
		}
	}

	protected void execInstrAndDump(CycleCounts counts, StringBuilder sb,
			RawInstruction instr) {
		sb.setLength(0);
		sb.append(state).append("; ");
		
		int pc;
		if (cpu.getConsole().isWordAccess()) {
			for (pc = instr.pc; pc < instr.pc + instr.getSize(); pc += 2) {
				sb.append(HexUtils.toHex4(cpu.getConsole().flatReadWord(pc))).append(' ');
			}
			while (pc < instr.pc + maxLength) {
				sb.append("     ");
				pc += 2;
			}
		} else {
			for (pc = instr.pc; pc < instr.pc + instr.getSize(); pc ++) {
				sb.append(HexUtils.toHex2(cpu.getConsole().flatReadByte(pc))).append(' ');
			}
			while (pc < instr.pc + maxLength) {
				sb.append("   ");
				pc ++;
			}
		}
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

}
