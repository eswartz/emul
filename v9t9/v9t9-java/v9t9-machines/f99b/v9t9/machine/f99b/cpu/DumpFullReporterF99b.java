/*
  DumpFullReporterF99b.java

  (c) 2010-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.cpu;

import java.io.PrintWriter;

import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.Settings;
import v9t9.machine.f99b.asm.ChangeBlockF99b;
import v9t9.machine.f99b.asm.InstF99b;
import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class DumpFullReporterF99b implements IInstructionListener {

	private final PrintWriter dump;
	private IProperty dumpSetting;
	private String prevName;

	/**
	 * @param cpu 
	 * 
	 */
	public DumpFullReporterF99b(CpuF99b cpu, PrintWriter dump) {
		this.dump = dump;
		dumpSetting = Settings.get(cpu, ICpu.settingDumpFullInstructions);
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#preExecute(v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public boolean preExecute(ChangeBlock block) {
		return true;
	}
	
	public void executed(ChangeBlock block_) {
		PrintWriter dumpfull = dump != null ? dump : Logging.getLog(dumpSetting);
		if (dumpfull == null) return;
		
		ChangeBlockF99b block = (ChangeBlockF99b) block_;
		
		dumpFullStart(block, block.inst, dumpfull);
		StringBuilder sb = new StringBuilder();
		dumpFullMid(block,
				(block.cpu.getSP() - block.preExecute.sp) / 2,
				(block.cpu.getRP() - block.preExecute.rp) / 2,
				sb);
		dumpFullEnd(block, block.cpu.getCycleCounts().getTotal() - block.preExecute.cycles,
				(block.preExecute.sp - block.cpu.getSP()) / 2,
				(block.preExecute.rp - block.cpu.getRP()) / 2,
				sb, dumpfull);
	}

	private void dumpFullStart(ChangeBlockF99b block,
			RawInstruction ins, PrintWriter dumpfull) {
		IMemoryEntry entry = block.cpu.getConsole().getEntryAt(ins.pc);
		String name = null;
		int offs = 0;
		if (entry != null) { 
			name = entry.lookupSymbol((short) ins.pc);
			if (name == null) {
				Pair<String, Short> info = entry.lookupSymbolNear((short) ins.pc, 0x100);
				if (info != null) {
					if (!info.first.equals(prevName)) {
						name = info.first;
						offs = ins.pc - info.second;
					}
				}
			}
		}
		if (name != null) {
			dumpfull.print('"' + name + "\"");
			if (offs != 0)
				dumpfull.print(" + " + offs);
			dumpfull.println();
			prevName = name; 
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(HexUtils.toHex4(ins.pc)).append(": ").append(' ').append(ins);
		while (sb.length() % 8 == 0)
			sb.append(' ');
		int len = sb.length();
		while (len < 24) {
			sb.append('\t');
			len += 8;
		}
		sb.append('\t');
		dumpfull.print(sb.toString());
	}
	private void dumpFullMid(ChangeBlockF99b block,
			int spused,
			int rpused,
			StringBuilder sb) {
		sb.append("( ");
		
		Pair<Integer, Integer> fx = InstF99b.getStackEffects(block.inst.getInst());
		if (fx != null)
			spused = Math.min(block.inStack.length, Math.max(fx.first, spused));
		
		for (int i = 0; i < spused; i++)
			sb.append(toStr(block.inStack[i])).append(' ');
		
		fx = InstF99b.getReturnStackEffects(block.inst.getInst());
		if (fx != null)
			rpused = Math.min(block.inReturnStack.length, Math.max(fx.first, rpused));
		
		if (rpused != 0) {
			
			sb.append("R ");
			for (int i = 0; i < rpused; i++)
				sb.append(toStr(block.inReturnStack[i])).append(' ');
		}
		
		sb.append(" -- ");
	}
	/**
	 * @param stackEntry
	 * @return
	 */
	private String toStr(short stackEntry) {
		return HexUtils.toHex4(stackEntry); // + " [" + stackEntry + "]";
	}
	private void dumpFullEnd(ChangeBlockF99b block, 
			int cycles, 
			int spadded, int rpadded,
			StringBuilder sb,
			PrintWriter dumpfull) {
		Pair<Integer, Integer> fx = InstF99b.getStackEffects(block.inst.getInst());
		if (fx != null)
			spadded = Math.min(4, Math.max(fx.second, spadded));
		
		IMemoryDomain memory = block.cpu.getConsole();
				
		for (int i = 0; i < spadded; i++)
			sb.append(toStr(memory.readWord(block.cpu.getSP() + i*2))).append(' ');
		
		fx = InstF99b.getReturnStackEffects(block.inst.getInst());
		if (fx != null)
			rpadded = Math.min(4, Math.max(fx.second, rpadded));
		
		if (rpadded != 0) {

			sb.append("R ");
			for (int i = 0; i < rpadded; i++)
				sb.append(toStr(memory.readWord(block.cpu.getRP() + i*2))).append(' ');
		}
		sb.append(")");

		while (sb.length() % 8 != 0)
			sb.append(' ');
		int len = sb.length();
		while (len < 32) {
			sb.append('\t');
			len += 8;
		}
		
		dumpfull.print(sb.toString());

		dumpfull.print(   
		        " sp=" + Integer.toHexString(block.cpu.getSP() & 0xffff).toUpperCase()
		        + " rp=" + Integer.toHexString(block.cpu.getRP() & 0xffff).toUpperCase()
		        + " sr="
		        + Integer.toHexString(block.cpu.getST()).toUpperCase()        
		);
		
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

}
