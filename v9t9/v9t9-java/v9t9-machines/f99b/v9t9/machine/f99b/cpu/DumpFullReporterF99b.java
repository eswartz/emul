/**
 * 
 */
package v9t9.machine.f99b.cpu;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;


import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.Settings;
import v9t9.machine.f99b.asm.InstF99b;
import v9t9.machine.f99b.asm.InstructionWorkBlockF99b;

/**
 * @author ejs
 *
 */
public class DumpFullReporterF99b implements IInstructionListener {

	private final PrintWriter dump;
	private IProperty dumpSetting;

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
	public boolean preExecute(InstructionWorkBlock before) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before_, InstructionWorkBlock after_) {
		PrintWriter dumpfull = dump != null ? dump : Logging.getLog(dumpSetting);
		if (dumpfull == null) return;
		InstructionWorkBlockF99b before = (InstructionWorkBlockF99b) before_;
		InstructionWorkBlockF99b after = (InstructionWorkBlockF99b) after_;
		dumpFullStart(before, before.inst, dumpfull);
		StringBuilder sb = new StringBuilder();
		dumpFullMid(before, 
				(after.sp - before.sp) / 2,
				(after.rp - before.rp) / 2,
				sb);
		dumpFullEnd(after, before.cycles,
				(before.sp - after.sp) / 2,
				(before.rp - after.rp) / 2,
				sb, dumpfull);
	}

	private void dumpFullStart(InstructionWorkBlockF99b iblock,
			RawInstruction ins, PrintWriter dumpfull) {
		IMemoryEntry entry = iblock.domain.getEntryAt(ins.pc);
		String name = null;
		if (entry != null) { 
			name = entry.lookupSymbol((short) ins.pc);
			if (name == null && iblock.showSymbol) {
				Pair<String, Short> info = entry.lookupSymbolNear((short) ins.pc, 0x100);
				if (info != null)
					name = info.first;
			}
		}
		if (name != null)
			dumpfull.println('"' + name + "\" ");
		iblock.showSymbol = false;
		
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
	private void dumpFullMid(InstructionWorkBlockF99b block,
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
	private void dumpFullEnd(InstructionWorkBlockF99b block, 
			int origCycleCount, 
			int spadded, int rpadded,
			StringBuilder sb,
			PrintWriter dumpfull) {
		Pair<Integer, Integer> fx = InstF99b.getStackEffects(block.inst.getInst());
		if (fx != null)
			spadded = Math.min(block.inStack.length, Math.min(4, Math.max(fx.second, spadded)));
		
		for (int i = 0; i < spadded; i++)
			sb.append(toStr(block.inStack[spadded - i - 1])).append(' ');
		
		fx = InstF99b.getReturnStackEffects(block.inst.getInst());
		if (fx != null)
			rpadded = Math.min(block.inReturnStack.length, Math.min(4, Math.max(fx.second, rpadded)));
		
		if (rpadded != 0) {

			sb.append("R ");
			for (int i = 0; i < rpadded; i++)
				sb.append(toStr(block.inReturnStack[rpadded - i - 1])).append(' ');
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
		        " sp=" + Integer.toHexString(block.sp & 0xffff).toUpperCase()
		        + " rp=" + Integer.toHexString(block.rp & 0xffff).toUpperCase()
		        + " sr="
		        + Integer.toHexString(block.st).toUpperCase()        
		);
		
		int cycles = block.cycles - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

}
