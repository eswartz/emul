/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.BaseInstructionWorkBlock;
import v9t9.engine.cpu.InstF99;
import v9t9.engine.cpu.InstructionWorkBlockF99;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class DumpFullReporterF99 implements InstructionListener {

	private final CpuF99 cpu;
	private final PrintWriter dump;

	/**
	 * 
	 */
	public DumpFullReporterF99(CpuF99 cpu, PrintWriter dump) {
		this.cpu = cpu;
		this.dump = dump;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(BaseInstructionWorkBlock before_, BaseInstructionWorkBlock after_) {
		PrintWriter dumpfull = dump != null ? dump : Executor.getDumpfull();
		if (dumpfull == null) return;
		InstructionWorkBlockF99 before = (InstructionWorkBlockF99) before_;
		InstructionWorkBlockF99 after = (InstructionWorkBlockF99) after_;
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

	private void dumpFullStart(InstructionWorkBlockF99 iblock,
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
		sb.append(HexUtils.toHex4(ins.pc & ~1)).append(": ").append(' ').append(ins);
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
	private void dumpFullMid(InstructionWorkBlockF99 block,
			int spused,
			int rpused,
			StringBuilder sb) {
		sb.append("( ");
		
		Pair<Integer, Integer> fx = InstF99.getStackEffects(block.inst.getInst());
		if (fx != null)
			spused = Math.min(block.inStack.length, Math.max(fx.first, spused));
		
		for (int i = 0; i < spused; i++)
			sb.append(toStr(block.inStack[i])).append(' ');
		
		if (rpused != 0) {
			sb.append("R ");
			
			fx = InstF99.getReturnStackEffects(block.inst.getInst());
			if (fx != null)
				rpused = Math.min(block.inReturnStack.length, Math.max(fx.first, rpused));

			for (int i = 0; i < Math.min(block.inReturnStack.length, rpused); i++)
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
	private void dumpFullEnd(InstructionWorkBlockF99 block, 
			int origCycleCount, 
			int spadded, int rpadded,
			StringBuilder sb,
			PrintWriter dumpfull) {
		Pair<Integer, Integer> fx = InstF99.getStackEffects(block.inst.getInst());
		if (fx != null)
			spadded = Math.min(4, Math.max(fx.second, spadded));
		
		for (int i = 0; i < spadded; i++)
			sb.append(toStr(block.getStackEntry(spadded - i - 1))).append(' ');
		
		if (rpadded != 0) {
			sb.append("R ");
			
			fx = InstF99.getReturnStackEffects(block.inst.getInst());
			if (fx != null)
				rpadded = Math.min(4, Math.max(fx.second, rpadded));

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
		        " sp=" + Integer.toHexString(((CpuStateF99)cpu.getState()).getSP() & 0xffff).toUpperCase()
		        + " rp=" + Integer.toHexString(((CpuStateF99)cpu.getState()).getRP() & 0xffff).toUpperCase()
		        + " sr="
		        + Integer.toHexString(cpu.getST() & 0xffff).toUpperCase()        
		);
		
		int cycles = cpu.getCurrentCycleCount() - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

}
