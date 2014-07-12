/*
  DumpFullReporter9900.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.cpu;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;


import ejs.base.utils.Pair;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.Settings;

/**
 * @author ejs
 *
 */
public class DumpFullReporter9900 implements IInstructionListener {

	private final Cpu9900 cpu;
	private IProperty dumpSetting;
	private IProperty testSuccessSymbol;
	private IProperty testFailureSymbol;
	private String symbol;

	/**
	 * 
	 */
	public DumpFullReporter9900(Cpu9900 cpu) {
		this.cpu = cpu;
		dumpSetting = Settings.get(cpu, ICpu.settingDumpFullInstructions);
		testFailureSymbol = Settings.get(cpu, ICpu.settingTestFailureSymbol);
		testSuccessSymbol = Settings.get(cpu, ICpu.settingTestSuccessSymbol);
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#preExecute(v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public boolean preExecute(InstructionWorkBlock before) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before_, InstructionWorkBlock after_) {
		PrintWriter dumpfull = Logging.getLog(dumpSetting);
		if (dumpfull == null) return;
		
		InstructionWorkBlock9900 before = (InstructionWorkBlock9900) before_;
		InstructionWorkBlock9900 after = (InstructionWorkBlock9900) after_;
		dumpFullStart(before, before.inst, dumpfull);
		dumpFullMid(before, (MachineOperand9900)before.inst.getOp1(), (MachineOperand9900)before.inst.getOp2(), dumpfull);
		dumpFullEnd(after, before.cycles, (MachineOperand9900)after.inst.getOp1(), (MachineOperand9900)after.inst.getOp2(), dumpfull);
	}

	public void dumpFullStart(InstructionWorkBlock9900 wb,
			RawInstruction ins, PrintWriter dumpfull) {
		IMemoryEntry entry = wb.domain.getEntryAt(ins.pc);
		symbol = null;
		if (entry != null) 
			symbol = entry.lookupSymbol((short) ins.pc);
		if (symbol != null) {
			dumpfull.print('"' + symbol + "\"");
			// HACK
			if ("@NEXT".equals(symbol)) {
				// show current word
				int curIP = wb.cpu.getRegister(14);
				Pair<String, Short> info = entry.lookupSymbolNear((short) curIP, 128);
				if (info != null && info.second >= 0x100 && info.second <= 0x4000) {
					dumpfull.print(" in " + info.first);
				}
			}
			else if (";S".equals(symbol)) {
				dumpCallStack(dumpfull, entry, wb, true);
				dumpStack(dumpfull, entry, wb);
			} else if ("DOCOL".equals(symbol)) {
				dumpCallStack(dumpfull, entry, wb, false);
				dumpStack(dumpfull, entry, wb);
			} else if ("EXECUTE".equals(symbol)) {
				dumpStack(dumpfull, entry, wb);
			}
			dumpfull.println();
		}
		dumpfull.print(HexUtils.toHex4(ins.pc) + ": "
		        + ins.toString() + " ==> ");
	}
	/**
	 * @param dumpfull
	 * @param wb 
	 * @param showCurrentIP 
	 */
	private void dumpCallStack(PrintWriter dumpfull, IMemoryEntry entry, InstructionWorkBlock9900 wb, boolean showCurrentIP) {
		// walk stack
		dumpfull.print(" [ ");
		boolean first = true;
		int unknown = 0;
		int curRP = wb.cpu.getRegister(13) & 0xffff;
		do {
			int curIP;
			if (first) {
				if (showCurrentIP)
					curIP = (wb.val2 & 0xffff) - 2;	// MOV *R13+, R14 -- get old value
				else
					curIP = (wb.cpu.getRegister(11) & 0xffff);	// MOV *R13+, R14 -- get old value
				first = false;
			}
			else if (curRP < 0xff40) {
				curIP = wb.domain
						.flatReadWord(curRP);
				curRP += 2;
				if (first)
					first = false;
				else
					dumpfull.print(" > ");
			} else {
				break;
			}
			Pair<String, Short> info = entry.lookupSymbolNear((short) curIP, 128);
			if (info != null && info.second >= 0x100 && info.second <= 0x4000) {
				dumpfull.print(info.first);
				unknown = 0;
				if ("EVALUATE".equals(info.first))
					break;
			} else {
				dumpfull.print(HexUtils.toHex4(curIP));
				unknown++;
			}
		} while (unknown < 4);
		dumpfull.print(" ]");
		
	}

	private void dumpStack(PrintWriter dumpfull, IMemoryEntry entry, InstructionWorkBlock9900 wb) {
		dumpfull.print(" (");
		int curSP = wb.cpu.getRegister(15) & 0xfffe;
		int topSP = Math.min(curSP + 8 * 2, 0xffc0);
		int curVal;
		while (topSP > curSP) {
			topSP -= 2;
			curVal = wb.domain.flatReadWord(topSP);
			dumpfull.print(" ");
			dumpfull.print(HexUtils.toHex4(curVal));
		}
		curVal = wb.cpu.getRegister(1);	 // TOS
		dumpfull.print(" ");
		dumpfull.print(HexUtils.toHex4(curVal));

		dumpfull.print(" )");
		
	}

	private void dumpFullMid(InstructionWorkBlock9900 wb,
			MachineOperand9900 mop1, MachineOperand9900 mop2,
			PrintWriter dumpfull) {
		String str;
		if (mop1.type != IMachineOperand.OP_NONE
		        && mop1.dest != IOperand.OP_DEST_KILLED) {
		    str = mop1.valueString(wb.ea1, wb.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != IMachineOperand.OP_NONE
		        && mop2.dest != IOperand.OP_DEST_KILLED) {
		    str = mop2.valueString(wb.ea2, wb.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str);
			}
		}
		dumpfull.print(" || ");
	}
	public void dumpFullEnd(InstructionWorkBlock9900 wb, 
			int origCycleCount, MachineOperand9900 mop1,
			MachineOperand9900 mop2, PrintWriter dumpfull) {
		String str;
		if (mop1.type != IMachineOperand.OP_NONE
		        && (mop1.dest != IOperand.OP_DEST_FALSE
		        		|| mop1.type == MachineOperand9900.OP_INC)) {
		    str = mop1.valueString(wb.ea1, wb.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != IMachineOperand.OP_NONE
				&& (mop2.dest != IOperand.OP_DEST_FALSE
		        		|| mop2.type == MachineOperand9900.OP_INC)) {
		    str = mop2.valueString(wb.ea2, wb.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str + " ");
			}
		}
		dumpfull.print("st="
		        + Integer.toHexString(cpu.getST() & 0xffff)
		                .toUpperCase() + " wp="
		        + Integer.toHexString(((Cpu9900) cpu).getWP() & 0xffff).toUpperCase());
		
		int cycles = wb.cycles - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();

		if (symbol != null) {
			if (symbol.startsWith("$test")) {
				System.out.println(symbol);
			}
			if (symbol.equals(testSuccessSymbol.getString())) {
				finish(0, "*** SUCCESS ***");
			}
			else if (symbol.equals(testFailureSymbol.getString())) {
				finish(1, "*** FAILED ***");
			}
		}
		
		if (wb.inst.getInst() == InstTableCommon.Idata) {
			finish(2, "*** CRASHED ***");
		}
	}


	/**
	 * @param code
	 * @param string
	 */
	private void finish(int code, String string) {
		System.out.println(string);
		System.out.println("cycles = " + cpu.getTotalCurrentCycleCount());
		System.exit(code);
		
	}

}
