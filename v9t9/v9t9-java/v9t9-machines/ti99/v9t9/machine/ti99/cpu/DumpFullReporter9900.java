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

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.IChangeElement;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.Settings;
import v9t9.machine.ti99.cpu.Changes.BaseOperandChangeElement;
import v9t9.machine.ti99.cpu.Changes.WriteResult;
import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;

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
	private int cyclesAtStart;

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
	public boolean preExecute(ChangeBlock block) {
		cyclesAtStart = cpu.getCycleCounts().getTotal();
		return true;
	}
	
	public void dumpFullStart(ICpuState cpu, IMemoryDomain domain,
			RawInstruction ins, PrintWriter dumpfull) {
		IMemoryEntry entry = domain.getEntryAt(ins.pc);
		symbol = null;
		if (entry != null) 
			symbol = entry.lookupSymbol((short) ins.pc);
		if (symbol != null && !symbol.startsWith("_")) {
			dumpfull.print('"' + symbol + "\"");
			// HACK
			if ("@NEXT".equals(symbol)) {
				// show current word
				int curIP = cpu.getRegister(14);
				Pair<String, Short> info = entry.lookupSymbolNear((short) curIP, 128);
				if (info != null && info.second >= 0x100 && info.second <= 0x4000) {
					dumpfull.print(" in " + info.first);
				}
			}
			else if (";S".equals(symbol)) {
				dumpCallStack(dumpfull, entry, cpu, true);
				dumpStack(dumpfull, entry, cpu, cpu.getConsole());
			} else if ("DOCOL".equals(symbol)) {
				dumpCallStack(dumpfull, entry, cpu, false);
				dumpStack(dumpfull, entry, cpu, cpu.getConsole());
			} else if ("EXECUTE".equals(symbol)) {
				dumpStack(dumpfull, entry, cpu, cpu.getConsole());
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
	private void dumpCallStack(PrintWriter dumpfull, IMemoryEntry entry, ICpuState state, boolean showCurrentIP) {
		// walk stack
		dumpfull.print(" [ ");
		boolean first = true;
		int unknown = 0;
		int curRP = cpu.getState().getRegister(13) & 0xffff;
		do {
			int curIP;
			if (first) {
//				if (showCurrentIP)
//					curIP = (wb.val2 & 0xffff) - 2;	// MOV *R13+, R14 -- get old value
//				else
					curIP = (cpu.getState().getRegister(11) & 0xffff);	// MOV *R13+, R14 -- get old value
				first = false;
			}
			else if (curRP < 0xff40) {
				curIP = cpu.getConsole().flatReadWord(curRP);
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

	private void dumpStack(PrintWriter dumpfull, IMemoryEntry entry, ICpuState cpu, IMemoryDomain domain) {
		Integer sp0 = entry.findSymbol("sp0");
		if (sp0 == null) {
			return;
		}
		sp0 = 0xffff & entry.flatReadWord(sp0 + 4);	// BL *R4, DOLIT
			
		dumpfull.print(" (");
		int curSP = cpu.getRegister(15) & 0xfffe;
		int topSP = Math.min(curSP + 8 * 2, sp0);
		int curVal;
		while (topSP > curSP) {
			topSP -= 2;
			curVal = domain.flatReadWord(topSP);
			dumpfull.print(" ");
			dumpfull.print(HexUtils.toHex4(curVal));
		}
		curVal = cpu.getRegister(1);	 // TOS
		dumpfull.print(" ");
		dumpfull.print(HexUtils.toHex4(curVal));

		dumpfull.print(" )");
		
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

	private void dumpFullMid(ChangeBlock9900 changes,
			MachineOperand9900 mop1, MachineOperand9900 mop2,
			PrintWriter dumpfull) {
		String str;
		if (mop1.type != IMachineOperand.OP_NONE
		        && mop1.dest != IOperand.OP_DEST_KILLED) {
			
			str = getOperandValue(changes, mop1, false);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != IMachineOperand.OP_NONE
		        && mop2.dest != IOperand.OP_DEST_KILLED) {
			str = getOperandValue(changes, mop2, false);
		    if (str != null) {
				dumpfull.print("op2=" + str);
			}
		}
		dumpfull.print(" || ");
	}
	/**
	 * @param changes
	 * @param after
	 * @return
	 */
	private String getOperandValue(ChangeBlock9900 changes, MachineOperand9900 mop, boolean after) {
		for (int i = 0; i < changes.getCount(); i++) {
			IChangeElement el = changes.getElement(i);
			if (el instanceof BaseOperandChangeElement && (!after || el instanceof WriteResult)) {
				BaseOperandChangeElement oel = (BaseOperandChangeElement) el;
				if (oel.state.mop == mop) {
					return mop.valueString(oel.state.ea, after ? oel.state.value : oel.state.prev);
				}
			}
		}
		return null;
	}


	public void dumpFullEnd(ChangeBlock9900 changes, 
			//int origCycleCount, 
			MachineOperand9900 mop1,
			MachineOperand9900 mop2, PrintWriter dumpfull) {
		String str;
		if (mop1.type != IMachineOperand.OP_NONE
		        && (mop1.dest != IOperand.OP_DEST_FALSE
		        		|| mop1.type == MachineOperand9900.OP_INC)) {
			str = getOperandValue(changes, mop1, true);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != IMachineOperand.OP_NONE
				&& (mop2.dest != IOperand.OP_DEST_FALSE
		        		|| mop2.type == MachineOperand9900.OP_INC)) {
		    str = getOperandValue(changes, mop2, true);
		    if (str != null) {
				dumpfull.print("op2=" + str + " ");
			}
		}
		dumpfull.print("st="
		        + Integer.toHexString(cpu.getST() & 0xffff)
		                .toUpperCase() + " wp="
		        + Integer.toHexString(((Cpu9900) cpu).getWP() & 0xffff).toUpperCase());
		
		//int cycles = changes.fetchCycles + changes.executeCycles;
		int cycles = cpu.getCycleCounts().getTotal() - cyclesAtStart;
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
		
		if (changes.inst.getInst() == InstTableCommon.Idata) {
			finish(2, "*** CRASHED ***");
		}
	}


	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#executed(v9t9.common.cpu.ChangeBlock)
	 */
	@Override
	public void executed(ChangeBlock block_) {
		PrintWriter dumpfull = Logging.getLog(dumpSetting);
		if (dumpfull == null) return;
		
		ChangeBlock9900 block = (ChangeBlock9900) block_;
		dumpFullStart(block.cpuState, block.cpu.getConsole(), block.inst, dumpfull);
		dumpFullMid(block, (MachineOperand9900)block.inst.getOp1(), (MachineOperand9900)block.inst.getOp2(), dumpfull);
		dumpFullEnd(block, (MachineOperand9900)block.inst.getOp1(), (MachineOperand9900)block.inst.getOp2(), dumpfull);
	}

}
