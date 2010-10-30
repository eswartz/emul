/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.BaseInstructionWorkBlock;
import v9t9.engine.cpu.InstF99b;
import v9t9.engine.cpu.InstructionWorkBlockF99b;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class DumpFullReporterF99b implements InstructionListener {

	private final CpuF99b cpu;
	private final PrintWriter dump;

	/**
	 * 
	 */
	public DumpFullReporterF99b(CpuF99b cpu, PrintWriter dump) {
		this.cpu = cpu;
		this.dump = dump;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(BaseInstructionWorkBlock before_, BaseInstructionWorkBlock after_) {
		PrintWriter dumpfull = dump != null ? dump : Executor.getDumpfull();
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
		MemoryEntry entry = iblock.domain.getEntryAt(ins.pc);
		String name = null;
		if (entry != null) { 
			name = entry.lookupSymbol((short) ins.pc);
			if (name == null && iblock.showSymbol) {
				name = entry.lookupSymbolNear((short) ins.pc);
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
			spadded = Math.min(4, Math.max(fx.second, spadded));
		
		for (int i = 0; i < spadded; i++)
			sb.append(toStr(block.getStackEntry(spadded - i - 1))).append(' ');
		
		fx = InstF99b.getReturnStackEffects(block.inst.getInst());
		if (fx != null)
			rpadded = Math.min(4, Math.max(fx.second, rpadded));
		
		if (rpadded != 0) {

			sb.append("R ");
			for (int i = 0; i < rpadded; i++)
				sb.append(toStr(block.getReturnStackEntry(rpadded - i - 1))).append(' ');
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
		        " sp=" + Integer.toHexString(((CpuStateF99b)cpu.getState()).getSP() & 0xffff).toUpperCase()
		        + " rp=" + Integer.toHexString(((CpuStateF99b)cpu.getState()).getRP() & 0xffff).toUpperCase()
		        + " sr="
		        + Integer.toHexString(cpu.getStatus().getIntMask()).toUpperCase()        
		);
		
		int cycles = cpu.getCurrentCycleCount() - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

}
