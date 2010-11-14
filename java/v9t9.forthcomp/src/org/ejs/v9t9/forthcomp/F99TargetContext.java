/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import static v9t9.engine.cpu.InstF99.CMP_GE;
import static v9t9.engine.cpu.InstF99.CMP_GT;
import static v9t9.engine.cpu.InstF99.CMP_LE;
import static v9t9.engine.cpu.InstF99.CMP_LT;
import static v9t9.engine.cpu.InstF99.CMP_UGE;
import static v9t9.engine.cpu.InstF99.CMP_UGT;
import static v9t9.engine.cpu.InstF99.CMP_ULE;
import static v9t9.engine.cpu.InstF99.CMP_ULT;
import static v9t9.engine.cpu.InstF99.CTX_PC;
import static v9t9.engine.cpu.InstF99.CTX_RP;
import static v9t9.engine.cpu.InstF99.I0branch;
import static v9t9.engine.cpu.InstF99.I0cmp;
import static v9t9.engine.cpu.InstF99.I0cmp_d;
import static v9t9.engine.cpu.InstF99.I0equ;
import static v9t9.engine.cpu.InstF99.I0equ_d;
import static v9t9.engine.cpu.InstF99.IRfrom;
import static v9t9.engine.cpu.InstF99.IRfrom_d;
import static v9t9.engine.cpu.InstF99.Iadd;
import static v9t9.engine.cpu.InstF99.IatR;
import static v9t9.engine.cpu.InstF99.Ibinop;
import static v9t9.engine.cpu.InstF99.Ibinop_d;
import static v9t9.engine.cpu.InstF99.Ibranch;
import static v9t9.engine.cpu.InstF99.Icload;
import static v9t9.engine.cpu.InstF99.Icmp;
import static v9t9.engine.cpu.InstF99.Icmp_d;
import static v9t9.engine.cpu.InstF99.IcontextFrom;
import static v9t9.engine.cpu.InstF99.Icstore;
import static v9t9.engine.cpu.InstF99.Idrop;
import static v9t9.engine.cpu.InstF99.Idup;
import static v9t9.engine.cpu.InstF99.Idup_d;
import static v9t9.engine.cpu.InstF99.Iequ;
import static v9t9.engine.cpu.InstF99.Iequ_d;
import static v9t9.engine.cpu.InstF99.Iexecute;
import static v9t9.engine.cpu.InstF99.Iexit;
import static v9t9.engine.cpu.InstF99.Iexiti;
import static v9t9.engine.cpu.InstF99.Iext;
import static v9t9.engine.cpu.InstF99.IfieldLit;
import static v9t9.engine.cpu.InstF99.IfieldLit_d;
import static v9t9.engine.cpu.InstF99.Ilit;
import static v9t9.engine.cpu.InstF99.Ilit_d;
import static v9t9.engine.cpu.InstF99.Iload;
import static v9t9.engine.cpu.InstF99.Iload_d;
import static v9t9.engine.cpu.InstF99.Iloop;
import static v9t9.engine.cpu.InstF99.Iover;
import static v9t9.engine.cpu.InstF99.IplusLoop;
import static v9t9.engine.cpu.InstF99.IplusStore;
import static v9t9.engine.cpu.InstF99.IplusStore_d;
import static v9t9.engine.cpu.InstF99.Iqdup;
import static v9t9.engine.cpu.InstF99.Irdrop;
import static v9t9.engine.cpu.InstF99.Irot;
import static v9t9.engine.cpu.InstF99.Irpidx;
import static v9t9.engine.cpu.InstF99.Istore;
import static v9t9.engine.cpu.InstF99.Istore_d;
import static v9t9.engine.cpu.InstF99.Iswap;
import static v9t9.engine.cpu.InstF99.ItoContext;
import static v9t9.engine.cpu.InstF99.ItoR;
import static v9t9.engine.cpu.InstF99.ItoR_d;
import static v9t9.engine.cpu.InstF99.Iudivmod;
import static v9t9.engine.cpu.InstF99.Iumul;
import static v9t9.engine.cpu.InstF99.Iunaryop;
import static v9t9.engine.cpu.InstF99.Iunaryop_d;
import static v9t9.engine.cpu.InstF99.IuplusLoop;
import static v9t9.engine.cpu.InstF99.Iuser;
import static v9t9.engine.cpu.InstF99.OP_1MINUS;
import static v9t9.engine.cpu.InstF99.OP_1PLUS;
import static v9t9.engine.cpu.InstF99.OP_2DIV;
import static v9t9.engine.cpu.InstF99.OP_2MINUS;
import static v9t9.engine.cpu.InstF99.OP_2PLUS;
import static v9t9.engine.cpu.InstF99.OP_2TIMES;
import static v9t9.engine.cpu.InstF99.OP_ADD;
import static v9t9.engine.cpu.InstF99.OP_AND;
import static v9t9.engine.cpu.InstF99.OP_ASH;
import static v9t9.engine.cpu.InstF99.OP_CSH;
import static v9t9.engine.cpu.InstF99.OP_INV;
import static v9t9.engine.cpu.InstF99.OP_LSH;
import static v9t9.engine.cpu.InstF99.OP_NEG;
import static v9t9.engine.cpu.InstF99.OP_NOT;
import static v9t9.engine.cpu.InstF99.OP_OR;
import static v9t9.engine.cpu.InstF99.OP_RSH;
import static v9t9.engine.cpu.InstF99.OP_SUB;
import static v9t9.engine.cpu.InstF99.OP_XOR;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.ejs.coffee.core.utils.HexUtils;
import org.ejs.coffee.core.utils.Pair;
import org.ejs.v9t9.forthcomp.RelocEntry.RelocType;
import org.ejs.v9t9.forthcomp.words.Comma;
import org.ejs.v9t9.forthcomp.words.DLiteral;
import org.ejs.v9t9.forthcomp.words.ExitI;
import org.ejs.v9t9.forthcomp.words.FieldComma;
import org.ejs.v9t9.forthcomp.words.Literal;
import org.ejs.v9t9.forthcomp.words.TargetColonWord;
import org.ejs.v9t9.forthcomp.words.TargetContext;
import org.ejs.v9t9.forthcomp.words.TargetUserVariable;
import org.ejs.v9t9.forthcomp.words.TargetValue;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.memory.EnhancedRamArea;
import v9t9.emulator.runtime.cpu.CpuF99;
import v9t9.emulator.runtime.cpu.CpuStateF99;
import v9t9.engine.cpu.InstF99;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class F99TargetContext extends TargetContext {

	private int opcodeIndex;
	private int opcodeAddr;
	private int lastOpcodeAddr;
	private static final int opcodeShifts[] = { 10, 5, 0 };
	private List<Integer> leaves;
	private TargetUserVariable lpUser;
	private DictEntry stub5BitLit;
	private DictEntry stub5BitOpcode;
	private DictEntry stub10BitOpcode;
	private DictEntry stub16BitLit;
	private DictEntry stub16BitAddr;
	private DictEntry stub8BitLit;
	private DictEntry stubCall;
	private DictEntry stub16BitJump;
	private DictEntry stub5BitJump;
	private DictEntry stub5BitCodeAlign;
	

	/**
	 * @param littleEndian
	 * @param charBits
	 * @param cellBits
	 * @param memorySize
	 */
	public F99TargetContext(int memorySize) {
		super(false, 8, 16, memorySize);
		leaves = new LinkedList<Integer>();
		
		stub5BitCodeAlign = defineStub("<<5-bit code align>>");
		stub5BitOpcode = defineStub("<<5-bit opcode>>");
		stub10BitOpcode = defineStub("<<10-bit opcode>>");
		stub5BitLit = defineStub("<<5-bit lit>>");
		stub5BitJump = defineStub("<<5-bit jump>>");
		stub8BitLit = defineStub("<<8-bit lit>>");
		stub16BitLit = defineStub("<<16-bit lit>>");
		stub16BitAddr = defineStub("<<16-bit addr>>");
		stub16BitJump = defineStub("<<16-bit jump>>");
		stubCall = defineStub("<<call>>");
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#defineBuiltins()
	 */
	@Override
	public void defineBuiltins() {
		definePrim(";S", Iexit);
		definePrim("@", Iload);
		definePrim("c@", Icload);
		definePrim("d@", Iload_d);
		definePrim("!", Istore);
		definePrim("c!", Icstore);
		definePrim("d!", Istore_d);
		definePrim("+!", IplusStore);
		definePrim("d+!", IplusStore_d);
		
		defineInlinePrim("1+", Iunaryop, OP_1PLUS);
		defineInlinePrim("2+", Iunaryop, OP_2PLUS);
		
		defineInlinePrim("1-", Iunaryop, OP_1MINUS);
		defineInlinePrim("2-", Iunaryop, OP_2MINUS);

		
		definePrim("dup", Idup);
		definePrim("drop", Idrop);
		definePrim("swap", Iswap);
		definePrim("over", Iover);
		definePrim("rot", Irot);
		definePrim("0=", I0equ);
		definePrim("D0=", I0equ_d);
		definePrim("=", Iequ);
		definePrim("D=", Iequ_d);
		definePrim("0branch", I0branch);
		definePrim("branch", Ibranch);
		defineInlinePrim("negate", Iunaryop, OP_NEG);
		defineInlinePrim("dnegate", Iunaryop_d, OP_NEG);
		defineInlinePrim("invert", Iunaryop, OP_INV);
		defineInlinePrim("not", Iunaryop, OP_NOT);
		definePrim("+", Iadd);
		defineInlinePrim("d+", Ibinop_d, OP_ADD);
		defineInlinePrim("-", Ibinop, OP_SUB);
		defineInlinePrim("d-", Ibinop_d, OP_SUB);
		definePrim("um*", Iumul);
		definePrim("um/mod", Iudivmod);
		defineInlinePrim("or", Ibinop, OP_OR);
		defineInlinePrim("and", Ibinop, OP_AND);
		defineInlinePrim("xor", Ibinop, OP_XOR);
		definePrim(">r", ItoR);
		definePrim("2>r", ItoR_d);
		definePrim("r>", IRfrom);
		definePrim("2r>", IRfrom_d);
		definePrim("rdrop", Irdrop);
		definePrim("r@", IatR);
		definePrim("i", IatR);
		defineInlinePrim("j", Irpidx, 2);
		
		defineInlinePrim("rp@", IcontextFrom, CTX_RP);
		defineInlinePrim("rp!", ItoContext, CTX_RP);
		
		definePrim("(do)", ItoR_d);
		definePrim("(loop)", Iloop);
		definePrim("(+loop)", IplusLoop);
		definePrim("(u+loop)", IuplusLoop);
		defineInlinePrim("(?do)", Idup_d, ItoR_d, Ibinop, OP_SUB, I0branch);
		
		definePrim("execute", Iexecute);

		definePrim("?dup", Iqdup);
		definePrim("2dup", Idup_d);
		definePrim("(context>)", IcontextFrom);
		definePrim("(>context)", ItoContext);
		definePrim("(user)", Iuser);

		defineInlinePrim("0<", I0cmp, CMP_LT);
		defineInlinePrim("0<=", I0cmp, CMP_LE);
		defineInlinePrim("0>", I0cmp, CMP_GT);
		defineInlinePrim("0>=", I0cmp, CMP_GE);
		defineInlinePrim("0U<", I0cmp, CMP_ULT);
		defineInlinePrim("0U<=", I0cmp, CMP_ULE);
		defineInlinePrim("0U>", I0cmp, CMP_UGT);
		defineInlinePrim("0U>=", I0cmp, CMP_UGE);
		defineInlinePrim("<", Icmp, CMP_LT);
		defineInlinePrim("<=", Icmp, CMP_LE);
		defineInlinePrim(">", Icmp, CMP_GT);
		defineInlinePrim(">=", Icmp, CMP_GE);
		defineInlinePrim("U<", Icmp, CMP_ULT);
		defineInlinePrim("U<=", Icmp, CMP_ULE);
		defineInlinePrim("U>", Icmp, CMP_UGT);
		defineInlinePrim("U>=", Icmp, CMP_UGE);

		defineInlinePrim("0D<", I0cmp_d, CMP_LT);
		defineInlinePrim("0D<=", I0cmp_d, CMP_LE);
		defineInlinePrim("0D>", I0cmp_d, CMP_GT);
		defineInlinePrim("0D>=", I0cmp_d, CMP_GE);
		defineInlinePrim("0DU<", I0cmp_d, CMP_ULT);
		defineInlinePrim("0DU<=", I0cmp_d, CMP_ULE);
		defineInlinePrim("0DU>", I0cmp_d, CMP_UGT);
		defineInlinePrim("0DU>=", I0cmp_d, CMP_UGE);
		defineInlinePrim("D<", Icmp_d, CMP_LT);
		defineInlinePrim("D<=", Icmp_d, CMP_LE);
		defineInlinePrim("D>", Icmp_d, CMP_GT);
		defineInlinePrim("D>=", Icmp_d, CMP_GE);
		defineInlinePrim("DU<", Icmp_d, CMP_ULT);
		defineInlinePrim("DU<=", Icmp_d, CMP_ULE);
		defineInlinePrim("DU>", Icmp_d, CMP_UGT);
		defineInlinePrim("DU>=", Icmp_d, CMP_UGE);
		
		defineInlinePrim("unloop", Irdrop, Irdrop);
		defineInlinePrim("2rdrop", Irdrop, Irdrop);
		defineInlinePrim("2/", Iunaryop, OP_2DIV);
		defineInlinePrim("2*", Iunaryop, OP_2TIMES);
		
		defineInlinePrim("LSH", Ibinop, OP_LSH);
		defineInlinePrim("RSH", Ibinop, OP_ASH);
		defineInlinePrim("URSH", Ibinop, OP_RSH);
		defineInlinePrim("CSH", Ibinop, OP_CSH);
		
		defineInlinePrim("SWPB", IfieldLit, 8, Ibinop, OP_CSH);
		
		defineInlinePrim("DLSH", Ibinop_d, OP_LSH);
		defineInlinePrim("DRSH", Ibinop_d, OP_ASH);
		defineInlinePrim("DURSH", Ibinop_d, OP_RSH);
		defineInlinePrim("DCSH", Ibinop_d, OP_CSH);
		
		defineInlinePrim("*", Iumul, Idrop);
		defineInlinePrim("s>d", Idup, I0cmp, CMP_LT);
		
		//defineInlinePrim("DOVAR", IcontextFrom, CTX_PC, Iexit);
		defineInlinePrim("DOLIT", Ilit, Iexit);
		
		defineInlinePrim("true", IfieldLit, -1);
		defineInlinePrim("false", IfieldLit, 0);
	}
	
	private void definePrim(String string, int opcode) {
		define(string, new F99PrimitiveWord(defineEntry(string), opcode));
		compileField(opcode);
		compileField(Iexit);
		alignBranch();
	}

	private void defineInlinePrim(String string, int... opcodes) {
		define(string, new F99InlineWord(defineEntry(string), opcodes));
		for (int i : opcodes)
			compileField(i);
		compileField(Iexit);
		alignBranch();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#defineEntry(java.lang.String)
	 */
	@Override
	public DictEntry defineEntry(String name) {
		DictEntry entry = super.defineEntry(name);
		opcodeAddr = lastOpcodeAddr = getDP();
		return entry;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#defineColonWord(java.lang.String)
	 */
	@Override
	public TargetColonWord defineColonWord(String name) {
		TargetColonWord word = super.defineColonWord(name);
		
		leaves.clear();
		
		return word;
	}
	/**
	 * 
	 */
	public void initCode() {
		opcodeIndex = 3;
		alignBranch();
	}

	/**
	 * 
	 */
	public void alignBranch() {
		alignDP();
		if (opcodeIndex != 0) {
			if (opcodeIndex < 3)
				stub5BitCodeAlign.use();
			if (opcodeIndex < 2)
				stub5BitCodeAlign.use();
			opcodeIndex = 0;
			opcodeAddr = alloc(cellSize);
			lastOpcodeAddr = opcodeAddr;
		}
	}


	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.words.TargetContext#doResolveRelocation(org.ejs.v9t9.forthcomp.RelocEntry)
	 */
	@Override
	protected int doResolveRelocation(RelocEntry reloc) throws AbortException {
		int val = reloc.target;
		if (reloc.type == RelocType.RELOC_CALL_15S1) {
			if ((val & 1) != 0)
				throw new AbortException("Call address is odd: " + HexUtils.toHex4(val));
			val = ((val >> 1) & 0x7fff) | 0x8000;
		}
		return val;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compile(org.ejs.v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compile(ITargetWord word) {
		word.getEntry().use();
		compileCall(word);
	}

	@Override
	protected void compileLoad(int bytes) {
		if (bytes == 1)
			compileOpcode(Icload);
		else if (bytes == 2)
			compileOpcode(Iload);
		else if (bytes == 4)
			compileOpcode(Iload_d);
		else
			assert false;
	}
	
	public void compileCall(ITargetWord word) {
		// must call
		alignBranch();
		stubCall.use();
		
		int reloc = addRelocation(opcodeAddr, 
				RelocType.RELOC_CALL_15S1, 
				word.getEntry().getContentAddr(),
				word.getEntry().getName());
		writeCell(opcodeAddr, reloc);
		
		opcodeAddr = alloc(cellSize);
	}

	/**
	 * @param opcode
	 */
	public void compileOpcode(int opcode) {
		if (opcode >= InstF99._Iext) {
			
			if (opcodeIndex == 2 && 
					// cannot read more than one field from the next word, and the EXT + opcode takes 1
					(InstF99.opcodeHasFieldArgument(opcode) 
					// also, for BRANCH, compiler words expect to write to HERE, but this will
					// mismatch the interpreter's idea of the "next data word" 
					|| InstF99.isAligningPCReference(opcode))
					)
				alignBranch();
			stub10BitOpcode.use();
			compileField(Iext);
			lastOpcodeAddr = opcodeAddr;
			compileField(opcode - InstF99._Iext);
			
		} else {
			stub5BitOpcode.use();
			compileField(opcode);
			lastOpcodeAddr = opcodeAddr;
		}
		
		if (InstF99.isAligningPCReference(opcode))
			opcodeIndex = 3;		
	}

	public void compileField(int opcode) {
		opcode &= 0x1f;
		if (opcodeIndex >= 3) {
			opcodeIndex = 0;
			opcodeAddr = alloc(cellSize);
		}
		//System.out.println(HexUtils.toHex4(opcodeAddr)+"#" + opcodeIndex+": " + opcode);
		writeCell(opcodeAddr, readCell(opcodeAddr) | (opcode << opcodeShifts[opcodeIndex]));
		opcodeIndex++;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileDoubleLiteral(int)
	 */
	@Override
	public void compileDoubleLiteral(int valueLo, int valueHi, boolean isUnsigned, boolean optimize) {
		int value = (valueLo & 0xffff) | (valueHi << 16);
		if (!optimize || (value >= -32 && value < (isUnsigned ? 32 : 16) )) {
			stub16BitLit.use();
			compileOpcode(IfieldLit_d);
			compileField(value);
		} else {
			// the literal is relative to the opcode, not the 
			// EXT, so we must align
			if (opcodeIndex == 2)
				opcodeIndex = 3;
			stub16BitLit.use();
			stub16BitLit.use();
			compileOpcode(Ilit_d);
			int ptr = alloc(4);
			writeCell(ptr, value & 0xffff);
			writeCell(ptr + 2, value >> 16);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileLiteral(int)
	 */
	@Override
	public void compileLiteral(int value, boolean isUnsigned, boolean optimize) {
		doCompileLiteral(value, isUnsigned, optimize).use();
	}

	private DictEntry doCompileLiteral(int value, boolean isUnsigned, boolean optimize) {
		if (opcodeIndex + 1 == 1) {
			if (optimize && value >= -32 && value < (isUnsigned ? 32 : 16)) {
				compileField(IfieldLit);
				compileField(value);
				return stub5BitLit;
			} else {
				compileField(Ilit);
				int ptr = alloc(2);
				writeCell(ptr, value & 0xffff);
				return stub16BitLit;
			}
		} else if (optimize && value >= -32 && value < (isUnsigned ? 32 : 16)) {
			compileField(IfieldLit);
			compileField(value);
			return stub5BitLit;
		} else {
			compileField(Ilit);
			int ptr = alloc(2);
			writeCell(ptr, value & 0xffff);
			return stub16BitLit;
		}
	}
	
	@Override
	public void compileCell(int loc) {
		doCompileCell(loc);
		stub16BitAddr.use();
	}

	private void doCompileCell(int loc) {
		int ptr = alloc(2);
		writeCell(ptr, loc);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileChar(int)
	 */
	@Override
	public void compileChar(int val) {
		int ptr = alloc(1);
		writeChar(ptr, val);
		stub8BitLit.use();
	}
	
	/**
	 * Export the state to a real machine
	 * @param hostContext
	 * @param machine
	 * @param baseSP
	 * @param baseRP
	 * @throws AbortException 
	 */
	public void doExportState(HostContext hostContext, Machine machine, int baseSP, int baseRP, int baseUP) throws AbortException {
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
		
		cpu.setBaseUP((short) baseUP);
		cpu.setUP((short) baseUP);
	}

	/**
	 * Export the state to a real machine
	 * @param hostContext
	 * @param machine
	 * @param baseSP
	 * @param baseRP
	 */
	public void doImportState(HostContext hostContext, Machine machine, int baseSP, int baseRP) {
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
	public void pushFixup(HostContext hostContext) {
		// a fixup needs the memory loc of the offset to update
		// as well as the original PC of the referring instruction
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		hostContext.pushData(lastOpcodeAddr);
		writeCell(nextDp, 0);
		setDP(nextDp + cellSize);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#pushHere(org.ejs.v9t9.forthcomp.HostContext)
	 */
	@Override
	public int pushHere(HostContext hostContext) {
		// TODO: optimize this
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		alignBranch();
		return nextDp;
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#swapFixup()
	 */
	@Override
	public void swapFixup(HostContext hostContext) {
		int d0 = hostContext.popData();
		int d1 = hostContext.popData();
		int e0 = hostContext.popData();
		int e1 = hostContext.popData();
		hostContext.pushData(d1);
		hostContext.pushData(d0);
		hostContext.pushData(e1);
		hostContext.pushData(e0);
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#resolveFixup()
	 */
	public void resolveFixup(HostContext hostContext) throws AbortException {
		int nextDp = getDP();
		int opAddr = hostContext.popData();
		int memAddr = hostContext.popData();
		int diff = nextDp - opAddr;
		
		if (diff < -16 || diff > 15)
			stub16BitJump.use();
		else
			stub5BitJump.use();
		
		writeCell(memAddr, diff);
		
		opcodeIndex = 3;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#resolveFixup()
	 */
	public void compileBack(HostContext hostContext, boolean conditional) {
		compileOpcode(conditional ? I0branch : Ibranch);
		
		int nextDp = getDP();
		int opAddr = hostContext.popData();
		int diff = opAddr - nextDp;
		
		if (diff < -16 || diff > 15)
			stub16BitJump.use();
		else
			stub5BitJump.use();

		doCompileCell(diff);
		opcodeIndex = 3;
	}


	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#pushLeave(org.ejs.v9t9.forthcomp.HostContext)
	 */
	@Override
	public void pushLeave(HostContext hostContext) {
		// add fixup to a list
		pushFixup(hostContext);
		leaves.add(hostContext.popData());
		leaves.add(hostContext.popData());		
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#loopCompile(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void loopCompile(HostContext hostCtx, ITargetWord loopCaller)
			throws AbortException {
		loopCaller.getCompilationSemantics().execute(hostCtx, this);
		
		boolean isQDo = hostCtx.popData() != 0;
		
		int opAddr = hostCtx.popData();
		int diff = opAddr - lastOpcodeAddr;
		
		if (diff < -16 || diff > 15)
			stub16BitJump.use();
		else
			stub5BitJump.use();


		doCompileCell(diff);
		
		if (isQDo) {
			// then comes here
			resolveFixup(hostCtx);
		}
		
		for (int i = 0; i < leaves.size(); i += 2) {
			hostCtx.pushData(leaves.get(i + 1));
			hostCtx.pushData(leaves.get(i));
			resolveFixup(hostCtx);
		}
		leaves.clear();
		
		ITargetWord unloop = (ITargetWord) require("unloop");
		
		unloop.getCompilationSemantics().execute(hostCtx, this);
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#defineCompilerWords(org.ejs.v9t9.forthcomp.HostContext)
	 */
	@Override
	public void defineCompilerWords(HostContext hostContext) {
		hostContext.define("FIELD,", new FieldComma());
		hostContext.define(",", new Comma());
		hostContext.define("LITERAL", new Literal(true));
		hostContext.define("DLITERAL", new DLiteral(true));
		hostContext.define("(LITERAL)", new Literal(false));
		hostContext.define("(DLITERAL)", new DLiteral(false));

		hostContext.define("EXITI", new ExitI());

	}

	/**
	 * @param string
	 * @return
	 */
	public Pair<Integer, Integer> writeLengthPrefixedString(String string) throws AbortException {
		int length = string.length();
		if (length > 255)
			throw new AbortException("String constant is too long");
		
		int dp = alloc(length + 1);
		
		writeChar(dp, length);
		stub8BitLit.use();
		
		for (int i = 0; i < length; i++) {
			writeChar(dp + 1 + i, string.charAt(i));
			stub8BitLit.use();
		}
		
		return new Pair<Integer, Integer>(dp, length + 1);
	}
	

	public void compileUser(TargetUserVariable var) {
		compileLiteral(var.getIndex(), false, true);
		compileOpcode(Iuser);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileCleanupLocals()
	 */
	@Override
	public void ensureLocalSupport(HostContext hostContext) throws AbortException {
		if (lpUser == null) {
			lpUser = (TargetUserVariable) find("LP");
			if (lpUser == null) {
				lpUser = defineUser("LP", 1);
				
				HostContext subContext = new HostContext(this);
				subContext.getStream().push(
						"false <export\n"+
						": (>LOCALS) LP @    	RP@ LP ! ; \\ caller pushes R> \n" +
						": (LOCALS>) R>  LP @ RP!   R>  LP !  >R ; \n" +
						"export>\n");
				ForthComp comp = new ForthComp(subContext, this);
				comp.parse();
				if (comp.getErrors() > 0)
					throw hostContext.abort("Failed to compile support code");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileSetupLocals()
	 */
	@Override
	public void compileSetupLocals() throws AbortException {

		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
		if (entry.hasLocals())
			throw new AbortException("cannot add more locals now");
		
		compile((ITargetWord) require("(>LOCALS)"));
		compileOpcode(ItoR);	// save old LP	
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileInitLocal(int)
	 */
	@Override
	public void compileInitLocal(int index) throws AbortException {
		compileOpcode(ItoR);
	}
	
	private int getLocalOffs(int index) {
		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
		int offs = entry.getLocalCount() - index;
		return -offs * 2;
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileLocalAddr(int)
	 */
	@Override
	public void compileLocalAddr(int index) {
		compileUser(lpUser);
		compileOpcode(Iload);
		compileLiteral(getLocalOffs(index), false, true);
		compileOpcode(Iadd);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileLocalAddr(int)
	 */
	@Override
	public void compileFromLocal(int index) throws AbortException {
		compileLocalAddr(index);
		compileField(Iload);
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileToLocal(int)
	 */
	@Override
	public void compileToLocal(int index) throws AbortException {
		compileLocalAddr(index);
		compileField(Istore);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileCleanupLocals()
	 */
	@Override
	public void compileCleanupLocals() throws AbortException {
		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
		if (entry.hasLocals()) {
			compile((ITargetWord) require("(LOCALS>)"));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileDoConstant(int, int)
	 */
	@Override
	public void compileDoConstant(int value, int cells) throws AbortException {
		compileOpcode(Ilit);
		compilePushValue(cells, value);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileDoUser(int)
	 */
	@Override
	public void compileDoUser(int index) throws AbortException {
		doCompileLiteral(index, false, true);
		compileOpcode(Iuser);
		compileOpcode(Iexit);
	}


	/**
	 * @throws AbortException 
	 * 
	 */
	public void compileExitI() throws AbortException {
		if (((ITargetWord) getLatest()).getEntry().hasLocals())
			compileCleanupLocals();

		compileOpcode(Iexiti);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileDoValue(int, int)
	 */
	@Override
	public int compilePushValue(int cells, int value) throws AbortException {
		compileOpcode(Ilit);
		
		alignBranch();
		int loc = alloc(cells * cellSize);
		
		if (cells == 1) {
			writeCell(loc, value);
			stub16BitLit.use();
		} else if (cells == 2) {
			writeCell(loc, value & 0xffff);
			writeCell(loc + 2, value >> 16);
			stub16BitLit.use();
			stub16BitLit.use();
		}
		else
			throw new AbortException("unhandled size: " +cells);
		
		return loc;

	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileWordParamAddr(org.ejs.v9t9.forthcomp.TargetValue)
	 */
	@Override
	public void compileWordParamAddr(TargetValue word) {
		stub16BitLit.use();
		doCompileLiteral(((ITargetWord)word).getEntry().getParamAddr(), true, true);		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#compileWordXt(org.ejs.v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compileTick(ITargetWord word) {
		stub16BitLit.use();
		doCompileLiteral(((ITargetWord)word).getEntry().getContentAddr(), true, true);		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.TargetContext#createMemory()
	 */
	@Override
	public MemoryDomain createMemory() {
		MemoryDomain console = new MemoryDomain("CONSOLE");
		EnhancedRamArea bigRamArea = new EnhancedRamArea(0, 0x10000); 
		MemoryEntry bigRamEntry = new MemoryEntry("RAM", console, 0, MemoryDomain.PHYSMEMORYSIZE, 
				bigRamArea);
		console.mapEntry(bigRamEntry);
		return console;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.words.TargetContext#compilePostpone(org.ejs.v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compilePostpone(ITargetWord word) throws AbortException {
		compileTick(word);
		compileOpcode(Ilit);
		compile((ITargetWord) require("compile,"));
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.words.TargetContext#compileDoVar()
	 */
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.words.TargetContext#compileDoVar()
	 */
	@Override
	protected void compileDoVar() {
		 compileOpcode(IcontextFrom);
		 compileOpcode(CTX_PC);
		 compileOpcode(Iexit);
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.words.TargetContext#compileDoDoes(org.ejs.v9t9.forthcomp.HostContext)
	 */
	@Override
	public int compileDoDoes(HostContext hostContext) throws AbortException {
		throw hostContext.abort("DOES> not implemented");
	}
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.words.TargetContext#compileDoes(org.ejs.v9t9.forthcomp.HostContext, org.ejs.v9t9.forthcomp.DictEntry, int)
	 */
	@Override
	public void compileDoes(HostContext hostContext, DictEntry dictEntry,
			int targetDP) throws AbortException {
		throw hostContext.abort("DOES> not implemented");
	}
}
