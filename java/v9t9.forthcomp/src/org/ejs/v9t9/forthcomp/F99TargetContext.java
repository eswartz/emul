/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.io.PrintStream;
import java.util.Stack;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.v9t9.forthcomp.RelocEntry.RelocType;

import v9t9.emulator.hardware.F99Machine;
import v9t9.emulator.runtime.cpu.CpuF99;
import v9t9.engine.cpu.InstF99;

/**
 * @author ejs
 *
 */
public class F99TargetContext extends TargetContext {

	private int opcodeIndex;
	private int opcodeAddr;
	private static final int opcodeShifts[] = { 9, 6, 0 };
	
	/**
	 * @param littleEndian
	 * @param charBits
	 * @param cellBits
	 * @param memorySize
	 */
	public F99TargetContext(int memorySize) {
		super(false, 8, 16, memorySize);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#defineBuiltins()
	 */
	@Override
	public void defineBuiltins() {
		definePrim(";S", InstF99.Iexit);
		definePrim("@", InstF99.Ifetch);
		definePrim("!", InstF99.Istore);
		definePrim("0", InstF99.Izero);
		definePrim("-1", InstF99.InegOne);
		definePrim("1", InstF99.Ione);
		definePrim("2", InstF99.Itwo);
		definePrim("dup", InstF99.Idup);
		definePrim("drop", InstF99.Idrop);
		definePrim("=", InstF99.Iequ);
		definePrim("0<", InstF99.I0lt);
		definePrim("0=", InstF99.I0equ);
		definePrim("0branch", InstF99.I0branch);
		definePrim("branch", InstF99.Ibranch);
		
	}
	
	private void definePrim(String string, int opcode) {
		define(string, new F99PrimitiveWord(defineEntry(string), opcode));
		compileField(opcode);
		compileField(InstF99.Iexit);
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#defineColonWord(java.lang.String)
	 */
	@Override
	public TargetColonWord defineColonWord(String name) {
		TargetColonWord word = super.defineColonWord(name);
		
		// pre-allocate one word so we don't have to track every other
		// allocator of dictionary space
		alignOpcodeWord();

		return word;
	}
	
	/**
	 * 
	 */
	private void alignOpcodeWord() {
		alignDP();
		opcodeIndex = 0;
		opcodeAddr = alloc(cellSize);
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compile(org.ejs.v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compile(ITargetWord word) {
		if (word instanceof F99PrimitiveWord) {
			int opcode = ((F99PrimitiveWord) word).getOpcode();
			compileField(opcode);
			
			if (InstF99.isAligningPCReference(opcode))
				opcodeIndex = 3;
		} else {
			// must call
			alignOpcodeWord();
			
			int reloc = addRelocation(opcodeAddr, RelocType.RELOC_CALL_15S1, word.getEntry().getContentAddr());
			writeCell(opcodeAddr, reloc);
			
			opcodeIndex = 3;
		}
	}

	private void compileField(int opcode) {
		if (opcodeIndex >= 3) {
			opcodeIndex = 0;
			opcodeAddr = alloc(cellSize);
		}
		if (opcodeIndex == 1 && opcode >= 8)
			opcodeIndex++;
		writeCell(opcodeAddr, readCell(opcodeAddr) | (opcode << opcodeShifts[opcodeIndex]));
		opcodeIndex++;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileDoubleLiteral(int)
	 */
	@Override
	public void compileDoubleLiteral(int value) {
		compileField(InstF99.Iliteral);
		int ptr = alloc(2);
		writeCell(ptr, value >> 16);
		compileField(InstF99.Iliteral);
		ptr = alloc(2);
		writeCell(ptr, value & 0xffff);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileLiteral(int)
	 */
	@Override
	public void compileLiteral(int value) {
		if (opcodeIndex + 1 == 1) {
			if (value >= -4 && value < 4) {
				compileField(InstF99.IfieldLiteral);
				compileField(value & 0x7);
			} else if (value >= -32 && value < 32) {
				compileField(InstF99.Inop);
				compileField(InstF99.IfieldLiteral);
				compileField(value & 0x3f);
			} else {
				compileField(InstF99.Iliteral);
				int ptr = alloc(2);
				writeCell(ptr, value & 0xffff);
			}
		} else if (value >= -32 && value < 32) {
			compileField(InstF99.IfieldLiteral);
			compileField(value & 0x3f);
		} else {
			compileField(InstF99.Iliteral);
			int ptr = alloc(2);
			writeCell(ptr, value & 0xffff);
		}
	}
	
	/**
	 * Export the state to a real machine
	 * @param hostContext
	 * @param machine
	 * @param baseSP
	 * @param baseRP
	 */
	public void exportState(HostContext hostContext, F99Machine machine, int baseSP, int baseRP) {
		exportMemory(machine.getConsole());
		CpuF99 cpu = (CpuF99) machine.getCpu();
		
		Stack<Integer> stack = hostContext.getDataStack();
		cpu.setSP((short) (baseSP - stack.size() * cellSize));
		for (int i = 0; i < stack.size(); i++)
			machine.getConsole().writeWord(cpu.getSP() + i * 2, (short) (int) stack.get(i));
		
		stack = hostContext.getReturnStack();
		cpu.setRSP((short) (baseRP - stack.size() * cellSize));
		for (int i = 0; i < stack.size(); i++)
			machine.getConsole().writeWord(cpu.getRSP() + i * 2, (short) (int) stack.get(i));
	}

	/**
	 * Export the state to a real machine
	 * @param hostContext
	 * @param machine
	 * @param baseSP
	 * @param baseRP
	 */
	public void importState(HostContext hostContext, F99Machine machine, int baseSP, int baseRP) {
		importMemory(machine.getConsole());
		CpuF99 cpu = (CpuF99) machine.getCpu();
		
		Stack<Integer> stack = hostContext.getDataStack();
		stack.clear();
		
		int curSP = cpu.getSP() & 0xffff;
		while (baseSP > curSP) {
			baseSP -= 2;
			stack.push((int) machine.getConsole().readWord(baseSP));
		}
		
		stack = hostContext.getReturnStack();
		stack.clear();
		
		int curRP = cpu.getRSP() & 0xffff;
		while (baseRP > curRP) {
			curRP -= 2;
			stack.push((int) machine.getConsole().readWord(baseRP));
		}

	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#pushFixup()
	 */
	@Override
	public void pushFixup(HostContext hostContext) {
		// TODO: optimize this
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		writeCell(nextDp, 0);
		setDP(nextDp + cellSize);
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#swapFixup()
	 */
	@Override
	public void swapFixup(HostContext hostContext) {
		int d = hostContext.popData();
		int e = hostContext.popData();
		hostContext.pushData(d);
		hostContext.pushData(e);
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#resolveFixup()
	 */
	public void resolveFixup(HostContext hostContext) {
		int nextDp = getDP();
		int diff = nextDp - hostContext.peekData();
		diff -= cellSize;
		writeCell(hostContext.popData(), diff);
		
		opcodeIndex = 3;
	}

	/**
	 * @param out
	 * @param k 
	 * @param from 
	 */
	public void dumpDict(PrintStream out, int from, int to) {
		int perLine = 6;
		int lines = ((to - from) / cellSize + perLine - 1) / perLine;
		int addr = from;
		for (int i = 0; i < lines; i++) {
			out.print(HexUtils.toHex4(addr) + ": ");
			for (int j = 0; j < perLine && addr < to; j++) {
				out.print(HexUtils.toHex4(readCell(addr)) + " ");
				addr += cellSize;
			}
			out.println();
		}
	}
}
