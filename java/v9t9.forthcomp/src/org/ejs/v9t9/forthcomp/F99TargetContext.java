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
import v9t9.emulator.runtime.cpu.CpuStateF99;
import v9t9.engine.cpu.InstF99;

/**
 * @author ejs
 *
 */
public class F99TargetContext extends TargetContext {

	private int opcodeIndex;
	private int opcodeAddr;
	private static final int opcodeShifts[] = { 10, 5, 0 };
	
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
		definePrim("@", InstF99.Iload);
		definePrim("@", InstF99.Iload);
		definePrim("!", InstF99.Istore);
		definePrim("!", InstF99.Istore);
		definePrim("1+", InstF99.I1plus);
		definePrim("2+", InstF99.I2plus);
		definePrim("dup", InstF99.Idup);
		definePrim("drop", InstF99.Idrop);
		definePrim("swap", InstF99.Iswap);
		definePrim("0<", InstF99.I0lt);
		definePrim("0=", InstF99.I0equ);
		definePrim("d0=", InstF99.I0equ_d);
		definePrim("0branch", InstF99.I0branch);
		definePrim("branch", InstF99.Ibranch);
		definePrim("negate", InstF99.Ineg);
		definePrim("dnegate", InstF99.Ineg_d);
		definePrim("invert", InstF99.Iinvert);
		definePrim("+", InstF99.Iadd);
		definePrim("d+", InstF99.Iadd_d);
		definePrim("-", InstF99.Isub);
		defineInlinePrim("d-", InstF99.Ineg_d, InstF99.Iadd_d);
		definePrim("um*", InstF99.Iumul);
		definePrim("um/mod", InstF99.Iudivmod);
		definePrim("or", InstF99.Ior);
		definePrim("and", InstF99.Iand);
		definePrim("xor", InstF99.Ixor);
		definePrim(">r", InstF99.ItoR);
		definePrim("2>r", InstF99.ItoR_d);
		definePrim("r>", InstF99.IRfrom);
		definePrim("2r>", InstF99.IRfrom_d);
		//definePrim("r@", InstF99.IatR);
		//definePrim("i", InstF99.IatR);
		definePrim("rdrop", InstF99.Irdrop);
		definePrim("i", InstF99.Ii);
		definePrim("(do)", InstF99.ItoR_d);
		definePrim("(loop)", InstF99.Iloop);
		defineInlinePrim("(?do)", InstF99.Idup_d, InstF99.ItoR_d, InstF99.Isub, InstF99.I0equ);

		definePrim("2dup", InstF99.Idup_d);
		definePrim("(context>)", InstF99.IcontextFrom);
		definePrim("(>context)", InstF99.ItoContext);
		
		defineInlinePrim("unloop", InstF99.Irdrop, InstF99.Irdrop);
		defineInlinePrim("2rdrop", InstF99.Irdrop, InstF99.Irdrop);
		defineInlinePrim("2/", InstF99.Iash, 1);
		defineInlinePrim("=", InstF99.Isub, InstF99.I0equ);
		defineInlinePrim("d=", InstF99.Isub_d, InstF99.I0equ_d);
		defineInlinePrim("1-", InstF99.IfieldLit, 1, InstF99.Isub);
		defineInlinePrim("2-", InstF99.IfieldLit, 2, InstF99.Isub);
		defineInlinePrim("*", InstF99.Iumul, InstF99.Idrop);
		defineInlinePrim("s>d", InstF99.Idup, InstF99.I0lt);
		//defineInlinePrim("d=", InstF99.Ineg_d, InstF99.Iadd_d, InstF99.Ior, InstF99.I0equ);
		

	}
	
	private void definePrim(String string, int opcode) {
		define(string, new F99PrimitiveWord(defineEntry(string), opcode));
		compileField(opcode);
		compileField(InstF99.Iexit);
	}

	private void defineInlinePrim(String string, int... opcodes) {
		define(string, new F99InlineWord(defineEntry(string), opcodes));
		for (int i : opcodes)
			compileField(i);
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
			compileOpcode(opcode);
			
		} else if (word instanceof F99InlineWord) {
			int[] opcodes = ((F99InlineWord) word).getOpcodes();
			for (int opcode : opcodes)
				compileOpcode(opcode);
		} else {
			// must call
			alignOpcodeWord();
			
			int reloc = addRelocation(opcodeAddr, 
					RelocType.RELOC_CALL_15S1, 
					word.getEntry().getContentAddr(),
					word.getEntry().getName());
			writeCell(opcodeAddr, reloc);
			
			opcodeIndex = 3;
		}
	}

	/**
	 * @param opcode
	 */
	private void compileOpcode(int opcode) {
		if (opcode >= InstF99._Iext) {
			compileField(InstF99.Iext);
			compileField(opcode - InstF99._Iext);
			
		} else {
			compileField(opcode);
		}
		
		if (InstF99.isAligningPCReference(opcode))
			opcodeIndex = 3;		
	}

	private void compileField(int opcode) {
		if (opcodeIndex >= 3) {
			opcodeIndex = 0;
			opcodeAddr = alloc(cellSize);
		}
		writeCell(opcodeAddr, readCell(opcodeAddr) | (opcode << opcodeShifts[opcodeIndex]));
		opcodeIndex++;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileDoubleLiteral(int)
	 */
	@Override
	public void compileDoubleLiteral(int value) {
		if (value >= -32 && value < 16) {
			compileOpcode(InstF99.IfieldLit_d);
			compileField(value);
		} else {
			compileOpcode(InstF99.Ilit_d);
			int ptr = alloc(4);
			writeCell(ptr, value & 0xffff);
			writeCell(ptr + 2, value >> 16);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileLiteral(int)
	 */
	@Override
	public void compileLiteral(int value) {
		if (opcodeIndex + 1 == 1) {
			if (value >= -32 && value < 16) {
				compileField(InstF99.IfieldLit);
				compileField(value & 0x1f);
			} else if (value >= -32 && value < 16) {
				compileField(InstF99.Inop);
				compileField(InstF99.IfieldLit);
				compileField(value & 0x1f);
			} else {
				compileField(InstF99.Ilit);
				int ptr = alloc(2);
				writeCell(ptr, value & 0xffff);
			}
		} else if (value >= -32 && value < 16) {
			compileField(InstF99.IfieldLit);
			compileField(value & 0x1f);
		} else {
			compileField(InstF99.Ilit);
			int ptr = alloc(2);
			writeCell(ptr, value & 0xffff);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileAddr(int)
	 */
	@Override
	public void compileAddr(int loc) {
		int ptr = alloc(2);
		writeCell(ptr, loc);
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
		CpuStateF99 cpu = (CpuStateF99) machine.getCpu().getState();
		
		cpu.setBaseSP((short) baseSP);
		
		Stack<Integer> stack = hostContext.getDataStack();
		cpu.setSP((short) (baseSP - stack.size() * cellSize));
		for (int i = 0; i < stack.size(); i++)
			machine.getConsole().writeWord(cpu.getSP() + i * 2, (short) (int) stack.get(stack.size() - i - 1));
		
		cpu.setBaseRP((short) baseRP);
		
		stack = hostContext.getReturnStack();
		cpu.setRP((short) (baseRP - stack.size() * cellSize));
		for (int i = 0; i < stack.size(); i++)
			machine.getConsole().writeWord(cpu.getRP() + i * 2, (short) (int) stack.get(stack.size() - i - 1));
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
		
		int curSP = ((CpuStateF99)cpu.getState()).getSP() & 0xffff;
		while (baseSP > 0 && baseSP > curSP) {
			baseSP -= 2;
			stack.push((int) machine.getConsole().readWord(baseSP));
		}
		
		stack = hostContext.getReturnStack();
		stack.clear();
		
		int curRP = ((CpuStateF99)cpu.getState()).getRP() & 0xffff;
		while (curRP > 0 && baseRP > curRP) {
			curRP -= 2;
			stack.push((int) machine.getConsole().readWord(baseRP));
		}

	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#pushFixup()
	 */
	@Override
	public int pushFixup(HostContext hostContext) {
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		writeCell(nextDp, 0);
		setDP(nextDp + cellSize);

		return nextDp;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#pushHere(org.ejs.v9t9.forthcomp.HostContext)
	 */
	@Override
	public int pushHere(HostContext hostContext) {
		// TODO: optimize this
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		opcodeIndex = 3;
		return nextDp;
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
		int perLine = 8;
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
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#loopCompile(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void loopCompile(HostContext hostCtx, ITargetWord loopCaller)
			throws AbortException {
		compile(loopCaller);
		int diff = hostCtx.popData() - getDP() - cellSize;
		compileAddr(diff);
		
		for (int ptr : hostCtx.leaves()) {
			hostCtx.pushData(ptr);
			resolveFixup(hostCtx);
		}
		hostCtx.leaves().clear();
		
		ITargetWord unloop = (ITargetWord) find("unloop");
		if (unloop == null)
			throw new AbortException("no unloop word found");
		
		compile(unloop);
		
	}
}
