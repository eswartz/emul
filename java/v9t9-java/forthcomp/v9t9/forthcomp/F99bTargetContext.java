/**
 * 
 */
package v9t9.forthcomp;

import static v9t9.engine.cpu.InstF99b.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.ejs.coffee.core.utils.HexUtils;
import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.F99InlineWord;
import v9t9.forthcomp.F99PrimitiveWord;
import v9t9.forthcomp.ForthComp;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ITargetWord;
import v9t9.forthcomp.RelocEntry;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.memory.EnhancedRamByteArea;
import v9t9.emulator.runtime.cpu.CpuF99b;
import v9t9.emulator.runtime.cpu.CpuStateF99b;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.forthcomp.RelocEntry.RelocType;
import v9t9.forthcomp.words.ExitI;
import v9t9.forthcomp.words.FieldComma;
import v9t9.forthcomp.words.TargetColonWord;
import v9t9.forthcomp.words.TargetContext;
import v9t9.forthcomp.words.TargetSQuote;
import v9t9.forthcomp.words.TargetUserVariable;
import v9t9.forthcomp.words.TargetValue;

/**
 * @author ejs
 *
 */
public class F99bTargetContext extends TargetContext {

	private List<Integer> leaves;
	private TargetUserVariable lpUser;
	private DictEntry stub4BitLit;
	private DictEntry stub8BitOpcode;
	private DictEntry stub16BitOpcode;
	private DictEntry stub16BitLit;
	private DictEntry stub16BitAddr;
	private DictEntry stub8BitLit;
	private DictEntry stubCall;
	private DictEntry stub16BitJump;
	private DictEntry stub8BitJump;
	private DictEntry stub4BitJump;
	

	/**
	 * @param littleEndian
	 * @param charBits
	 * @param cellBits
	 * @param memorySize
	 */
	public F99bTargetContext(int memorySize) {
		super(false, 8, 16, memorySize);
		leaves = new LinkedList<Integer>();
		
		stub8BitOpcode = defineStub("<<8-bit opcode>>");
		stub16BitOpcode = defineStub("<<16-bit opcode>>");
		stub4BitLit = defineStub("<<4-bit lit>>");
		stub4BitJump = defineStub("<<4-bit jump>>");
		stub8BitJump = defineStub("<<8-bit jump>>");
		stub8BitLit = defineStub("<<8-bit lit>>");
		stub16BitLit = defineStub("<<16-bit lit>>");
		stub16BitAddr = defineStub("<<16-bit addr>>");
		stub16BitJump = defineStub("<<16-bit jump>>");
		stubCall = defineStub("<<call>>");
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineBuiltins()
	 */
	@Override
	public void defineBuiltins() throws AbortException {
		definePrim("cell", IlitX | 2);
		defineInlinePrim("cells", IlitX | 1, Ilsh);
		
		definePrim(";S", Iexit);
		definePrim("@", Iload);
		definePrim("c@", Icload);
		definePrim("d@", Iload_d);
		definePrim("!", Istore);
		definePrim("c!", Icstore);
		definePrim("d!", Istore_d);
		definePrim("+!", IplusStore);
		definePrim("c+!", IcplusStore);
		definePrim("d+!", IplusStore_d);
		
		definePrim("1+", I1plus);
		definePrim("2+", I2plus);
		
		definePrim("1-", I1minus);
		definePrim("2-", I2minus);

		
		definePrim("dup", Idup);
		definePrim("drop", Idrop);
		definePrim("swap", Iswap);
		definePrim("over", Iover);
		definePrim("rot", Irot);
		definePrim("0=", I0equ);
		definePrim("D0=", I0equ_d);
		definePrim("=", Iequ);
		definePrim("D=", Iequ_d);
		definePrim("0branch", I0branchB);
		definePrim("branch", IbranchB);
		definePrim("negate", Ineg);
		definePrim("dnegate", Ineg_d);
		definePrim("+", Iadd);
		definePrim("d+", Iadd_d);
		definePrim("-", Isub);
		definePrim("d-", Isub_d);
		definePrim("um*", Iumul);
		definePrim("um/mod", Iudivmod);
		
		definePrim("invert", Iinv);
		definePrim("not", Inot);
		definePrim("or", Ior);
		definePrim("and", Iand);
		definePrim("xor", Ixor);
		
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
		defineInlinePrim("(loop)", IloopUp);
		defineInlinePrim("(+loop)", IplusLoopUp);
		defineInlinePrim("(u+loop)", IuplusLoopUp);
		defineInlinePrim("(?do)", Idup_d, ItoR_d, Isub, I0branchB);
		
		definePrim("execute", Iexecute);

		definePrim("?dup", Iqdup);
		definePrim("2dup", Idup_d);
		definePrim("(context>)", IcontextFrom);
		definePrim("(>context)", ItoContext);
		definePrim("(user)", Iuser);

		defineInlinePrim("0<", IlitX, Icmp+CMP_LT);
		defineInlinePrim("0<=", IlitX, Icmp+CMP_LE);
		defineInlinePrim("0>", IlitX, Icmp+CMP_GT);
		defineInlinePrim("0>=", IlitX, Icmp+CMP_GE);
		defineInlinePrim("0U<", IlitX, Icmp+CMP_ULT);
		defineInlinePrim("0U<=", IlitX, Icmp+CMP_ULE);
		defineInlinePrim("0U>", IlitX, Icmp+CMP_UGT);
		defineInlinePrim("0U>=", IlitX, Icmp+CMP_UGE);
		defineInlinePrim("<", Icmp+CMP_LT);
		defineInlinePrim("<=", Icmp+CMP_LE);
		defineInlinePrim(">", Icmp+CMP_GT);
		defineInlinePrim(">=", Icmp+CMP_GE);
		defineInlinePrim("U<", Icmp+CMP_ULT);
		defineInlinePrim("U<=", Icmp+CMP_ULE);
		defineInlinePrim("U>", Icmp+CMP_UGT);
		defineInlinePrim("U>=", Icmp+CMP_UGE);

		defineInlinePrim("0D<", IlitX_d, Icmp+CMP_LT);
		defineInlinePrim("0D<=", IlitX_d, Icmp+CMP_LE);
		defineInlinePrim("0D>", IlitX_d, Icmp+CMP_GT);
		defineInlinePrim("0D>=", IlitX_d, Icmp+CMP_GE);
		defineInlinePrim("0DU<", IlitX_d, Icmp+CMP_ULT);
		defineInlinePrim("0DU<=", IlitX_d, Icmp+CMP_ULE);
		defineInlinePrim("0DU>", IlitX_d, Icmp+CMP_UGT);
		defineInlinePrim("0DU>=", IlitX_d, Icmp+CMP_UGE);
		defineInlinePrim("D<", Icmp_d+CMP_LT);
		defineInlinePrim("D<=", Icmp_d+CMP_LE);
		defineInlinePrim("D>", Icmp_d+CMP_GT);
		defineInlinePrim("D>=", Icmp_d+CMP_GE);
		defineInlinePrim("DU<", Icmp_d+CMP_ULT);
		defineInlinePrim("DU<=", Icmp_d+CMP_ULE);
		defineInlinePrim("DU>", Icmp_d+CMP_UGT);
		defineInlinePrim("DU>=", Icmp_d+CMP_UGE);
		
		defineInlinePrim("unloop", Irdrop_d);
		defineInlinePrim("2rdrop", Irdrop_d);
		defineInlinePrim("2/", I2div);
		defineInlinePrim("2*", I2times);
		
		defineInlinePrim("LSH", Ilsh);
		defineInlinePrim("RSH", Iash);
		defineInlinePrim("URSH", Irsh);
		defineInlinePrim("CSH", Icsh);
		
		defineInlinePrim("SWPB", IlitX | 8, Icsh);
		
		defineInlinePrim("DLSH", Ilsh_d);
		defineInlinePrim("DRSH", Iash_d);
		defineInlinePrim("DURSH", Irsh_d);
		defineInlinePrim("DCSH", Icsh_d);
		
		defineInlinePrim("*", Iumul, Idrop);
		
		defineInlinePrim("2drop", Idrop_d);
		defineInlinePrim("d>q", Idup, IlitX, Icmp + CMP_LT, Idup);
		defineInlinePrim("dum/mod", Iudivmod_d);
		
		defineInlinePrim("s>d", Idup, IlitX, Icmp+CMP_LT);
		
		//defineInlinePrim("DOVAR", IcontextFrom, CTX_PC, I2plus, Iexit);
		//defineInlinePrim("DOVAR", IbranchX|0, Idovar);
		//defineInlinePrim("DOLIT", IlitW, 0, 0, Iexit);
		
		defineInlinePrim("true", IlitX | 0xf);
		defineInlinePrim("false", IlitX);

		
		defineInlinePrim("(fill)", Ifill);
		defineInlinePrim("(cfill)", Icfill);
		defineInlinePrim("(move)", Imove);
		defineInlinePrim("(cmove)", Icmove);
		
		defineInlinePrim("(LITERAL)", IlitW);
		defineInlinePrim("(DLITERAL)", IlitD_d);
		
		//defineInlinePrim("(s\")", IcontextFrom, CTX_PC, IlitX | 5, Iadd, Idup, I1plus, Iswap, Icload);
		defineInlinePrim("((s\"))", IatR, Idup, I1plus, Iswap, Icload, Idup, IRfrom, Iadd, I1plus, ItoR);
		
		define("(S\")", new TargetSQuote(defineEntry("(S\")")));
		
		definePrim("(SYSCALL)", Isyscall);
		
	}
	
	private void definePrim(String string, int opcode) {
		define(string, new F99PrimitiveWord(defineEntry(string), opcode));
		compileByte(opcode);
		compileByte(Iexit);
	}

	private void defineInlinePrim(String string, int... opcodes) {
		define(string, new F99InlineWord(defineEntry(string), opcodes));
		for (int i : opcodes)
			compileByte(i);
		compileByte(Iexit);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineEntry(java.lang.String)
	 */
	@Override
	public DictEntry defineEntry(String name) {
		DictEntry entry = super.defineEntry(name);
		return entry;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineColonWord(java.lang.String)
	 */
	@Override
	public TargetColonWord defineColonWord(String name) {
		TargetColonWord word = super.defineColonWord(name);
		
		leaves.clear();
		
		return word;
	}
	/**
	 * Call addresses must be aligned
	 */
	public void initCode() {
		alignDP();
	}

	public void alignBranch() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#doResolveRelocation(v9t9.forthcomp.RelocEntry)
	 */
	@Override
	protected int doResolveRelocation(RelocEntry reloc) throws AbortException {
		int val = reloc.target;	// TODO
		if (reloc.type == RelocType.RELOC_CALL_15S1) {
			if ((val & 1) != 0)
				throw new AbortException("Call address is odd: " + HexUtils.toHex4(val));
			val = ((val >> 1) & 0x7fff) | 0x8000;
		}
		return val;
	}
	public void compile(ITargetWord word) {
		word.getEntry().use();
		compileCall(word);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileLoad(int)
	 */
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
	/**
	 * @param opcode
	 */
	public void compileOpcode(int opcode) {
		if (opcode >= 256) {
			
			stub16BitOpcode.use();
			compileByte(opcode >> 8);
			compileByte(opcode & 0xff);
			
		} else {
			stub8BitOpcode.use();
			compileByte(opcode);
		}
	}

	public void compileByte(int opcode) {
		writeChar(alloc(1), opcode);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileDoubleLiteral(int)
	 */
	@Override
	public void compileDoubleLiteral(int valueLo, int valueHi, boolean isUnsigned, boolean optimize) {
		int value = (valueLo & 0xffff) | (valueHi << 16); 
		if (optimize && value >= -8 && value < (isUnsigned ? 16 : 8)) {
			stub4BitLit.use();
			compileOpcode(IlitX_d | (value & 0xf));
		} else if (optimize && value >= -128 && value < (isUnsigned ? 256 : 128)) {
			stub8BitLit.use();
			compileOpcode(IlitB_d);
			compileByte(value);
		} else {
			stub16BitLit.use();
			stub16BitLit.use();
			compileOpcode(IlitD_d);
			int ptr = alloc(4);
			writeCell(ptr, value & 0xffff);
			writeCell(ptr + 2, value >> 16);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileLiteral(int)
	 */
	@Override
	public void compileLiteral(int value, boolean isUnsigned, boolean optimize) {
		doCompileLiteral(value, isUnsigned, optimize).use();
	}

	private DictEntry doCompileLiteral(int value, boolean isUnsigned, boolean optimize) {
		if (optimize && value >= -8 && value < (isUnsigned ? 16 : 8)) {
			compileByte(IlitX | (value & 0xf));
			return stub4BitLit;
		} else if (optimize && value >= -128 && value < (isUnsigned ? 256 : 128)) {
			compileByte(IlitB);
			compileByte(value);
			return stub8BitLit;
		} else {
			compileByte(IlitW);
			int ptr = alloc(2);
			writeCell(ptr, value & 0xffff);
			return stub16BitLit;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileAddr(int)
	 */
	@Override
	public void compileCell(int loc) {
		doCompileCell(loc);
		stub16BitLit.use();
	}
	
	private void doCompileCell(int loc) {
		int ptr = alloc(2);
		writeCell(ptr, loc);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileChar(int)
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
		CpuStateF99b cpu = (CpuStateF99b) machine.getCpu().getState();
		
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
		CpuF99b cpu = (CpuF99b) machine.getCpu();
		
		Stack<Integer> stack = hostContext.getDataStack();
		stack.clear();
		
		int curSP = ((CpuStateF99b)cpu.getState()).getSP() & 0xffff;
		while (baseSP > 0 && baseSP > curSP) {
			baseSP -= 2;
			stack.push((int) machine.getConsole().readWord(baseSP));
		}
		
		stack = hostContext.getReturnStack();
		stack.clear();
		
		int curRP = ((CpuStateF99b)cpu.getState()).getRP() & 0xffff;
		while (curRP > 0 && baseRP > curRP) {
			curRP -= 2;
			stack.push((int) machine.getConsole().readWord(baseRP));
		}

	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#pushFixup()
	 */
	@Override
	public void pushFixup(HostContext hostContext) {
		// a fixup needs the memory loc of the offset to update
		// as well as the original PC of the referring instruction
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		alloc(1);		// assume short branch
		
		hostContext.markFixup(nextDp);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#pushHere(v9t9.forthcomp.HostContext)
	 */
	@Override
	public int pushHere(HostContext hostContext) {
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		hostContext.markFixup(nextDp);
		return nextDp;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#swapFixup()
	 */
	@Override
	public void swapFixup(HostContext hostContext) {
		int d0 = hostContext.popData();
		int e0 = hostContext.popData();
		hostContext.pushData(d0);
		hostContext.pushData(e0);
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#resolveFixup()
	 */
	public void resolveFixup(HostContext hostContext) throws AbortException {
		int nextDp = getDP();
		int opAddr = hostContext.popData();
		int diff = nextDp - opAddr;
		
		writeJumpOffs(hostContext, opAddr, diff);
		
		hostContext.resolveFixup(opAddr, nextDp);

	}


	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#resolveFixup()
	 */
	public void compileBack(HostContext hostContext, boolean conditional) throws AbortException {
		int nextDp = getDP();
		int opAddr = hostContext.popData();
		int diff = opAddr - nextDp;
		
		writeJumpOffsAlloc(diff, conditional ? I0branchX : IbranchX);
	}

	private int writeJumpOffs(HostContext hostContext, int opAddr, int diff)
			throws AbortException {
		
		// Opcode is already compiled as 1-byte branch, so diff is relative to offset
		
		// When we jump backward, measure from inst.pc, else from inst.pc + inst.size
		if (diff < -128 - 1 || diff >= 128)
			throw hostContext.abort("jump too long: " + diff);
		
		if (diff < -8 - 1 || diff >= 8) {
			stub8BitJump.use();
			if (diff < 0)
				diff+=2;		// for branch inst
			else
				diff--;
			
			//System.out.println("@writeJumpOffs: " +opAddr + ": " + readChar(opAddr));
			writeChar(opAddr, (diff & 0xff));
			return 1;
		}
		else {
			stub4BitJump.use();
			int newOp = readChar(opAddr - 1);
			if (newOp == IbranchB) newOp = IbranchX;
			else if (newOp == I0branchB) newOp = I0branchX;
			else throw hostContext.abort("suspicious code sequence: " + Integer.toHexString(newOp));
			
			if (diff < 0)
				diff++;	// branch inst
			writeChar(opAddr - 1, newOp | (diff & 0xf));
			return 0;
		}
	}


	private void writeJumpOffsAlloc(int diff, int baseOpcode)
			throws AbortException {
		if (diff < -130 || diff >= 128) {
			stub16BitJump.use();
			if (diff >= 0)
				diff--;		// for branch inst
			baseOpcode = baseOpcode == IbranchX ? IbranchW : I0branchW;
			compileOpcode(baseOpcode);
			compileCell(diff);
		} else if (diff < -8 || diff >= 9) {
			stub8BitJump.use();
			if (diff >= 0)
				diff-=2;
			baseOpcode = baseOpcode == IbranchX ? IbranchB : I0branchB;
			compileOpcode(baseOpcode);
			compileChar((diff & 0xff));
		}
		else {
			stub4BitJump.use();
			if (diff >= 0)
				diff--;	// branch inst
			compileOpcode(baseOpcode | (diff & 0xf));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#pushLeave(v9t9.forthcomp.HostContext)
	 */
	@Override
	public void pushLeave(HostContext hostContext) {
		// add fixup to a list
		pushFixup(hostContext);
		leaves.add(hostContext.popData());
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#loopCompile(v9t9.forthcomp.HostContext, v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void loopCompile(HostContext hostCtx, ITargetWord loopCaller)
			throws AbortException {
		//compile(loopCaller);
		loopCaller.getCompilationSemantics().execute(hostCtx, this);
		
		boolean isQDo = hostCtx.popData() != 0;
		
		int opAddr = hostCtx.popData();
		int diff = opAddr - getDP();
		
		writeJumpOffsAlloc(diff, I0branchX);
		
		if (isQDo) {
			// then comes here
			resolveFixup(hostCtx);
		}
		
		for (int i = 0; i < leaves.size(); i++) {
			hostCtx.pushData(leaves.get(i));
			resolveFixup(hostCtx);
		}
		leaves.clear();
		
		ITargetWord unloop = (ITargetWord) require("unloop");
		
		unloop.getCompilationSemantics().execute(hostCtx, this);
		
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineCompilerWords(v9t9.forthcomp.HostContext)
	 */
	@Override
	public void defineCompilerWords(HostContext hostContext) {
		//define("HERE", defineForward("HERE", "<<built-in>>"));
		//define("BASE", defineForward("BASE", "<<built-in>>"));
		
		hostContext.define("FIELD,", new FieldComma());
		hostContext.define("EXITI", new ExitI());
	}

	public void compileUser(TargetUserVariable var) {
		int index = var.getIndex();
		if (index < 256) {
			compileOpcode(Iupidx);
			compileByte(index);
		} else {
			doCompileLiteral(index, false, true);
			compileOpcode(Iuser);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileCleanupLocals()
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
	 * @see v9t9.forthcomp.TargetContext#compileSetupLocals()
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
	 * @see v9t9.forthcomp.TargetContext#compileInitLocal(int)
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
	 * @see v9t9.forthcomp.TargetContext#compileLocalAddr(int)
	 */
	@Override
	public void compileLocalAddr(int index) {
		compileUser(lpUser);
		compileOpcode(Iload);
		compileLiteral(getLocalOffs(index), false, true);
		compileOpcode(Iadd);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileLocalAddr(int)
	 */
	@Override
	public void compileFromLocal(int index) throws AbortException {
		compileLocalAddr(index);
		compileByte(Iload);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileToLocal(int)
	 */
	@Override
	public void compileToLocal(int index) throws AbortException {
		compileLocalAddr(index);
		compileByte(Istore);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileCleanupLocals()
	 */
	@Override
	public void compileCleanupLocals() throws AbortException {
		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
		if (entry.hasLocals()) {
			compile((ITargetWord) require("(LOCALS>)"));
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileDoConstant(int, int)
	 */
	@Override
	public void compileDoConstant(int value, int cells) throws AbortException {
		if (cells == 1)
			compileLiteral(value, false, true);
		else if (cells == 2)
			compileDoubleLiteral(value & 0xffff, value >> 16, false, true);
		else
			throw new UnsupportedOperationException();
		compileOpcode(Iexit);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileDoUser(int)
	 */
	@Override
	public void compileDoUser(int index) {
		if (index < 256) {
			compileOpcode(Iupidx);
			compileByte(index);
		} else {
			doCompileLiteral(index, false, true);
			compileOpcode(Iuser);
		}
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
	 * @see v9t9.forthcomp.TargetContext#compileDoValue(int, int)
	 */
	@Override
	public int compilePushValue(int cells, int value) throws AbortException {
		int loc;
		if (cells == 1) {
			compileOpcode(IlitW);
			loc = alloc(cellSize);
			compileOpcode(Iexit);
			//compile((ITargetWord) require("DOLIT"));
		} else { 
			compileOpcode(IlitD_d);
			loc = alloc(cellSize * cells);
			compileOpcode(Iexit);
			//compile((ITargetWord) require("DODLIT"));
		}
		
		
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
	 * @see v9t9.forthcomp.TargetContext#compileWordParamAddr(v9t9.forthcomp.TargetValue)
	 */
	@Override
	public void compileWordParamAddr(TargetValue word) {
		stub16BitLit.use();
		doCompileLiteral(((ITargetWord)word).getEntry().getParamAddr(), true, true);		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileWordXt(v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compileTick(ITargetWord word) {
		stub16BitAddr.use();

		compileByte(IlitW);
		int ptr = alloc(cellSize);

		int reloc = addRelocation(ptr, 
				RelocType.RELOC_ABS_ADDR_16, 
				word.getEntry().getContentAddr());

		writeCell(ptr, reloc);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileCall(v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compileCall(ITargetWord word) {
		stubCall.use();
		
		int pc = alloc(cellSize);
		
		int reloc = addRelocation(pc, 
				RelocType.RELOC_CALL_15S1, 
				word.getEntry().getContentAddr());
		writeCell(pc, reloc);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#createMemory()
	 */
	@Override
	public MemoryDomain createMemory() {
		MemoryDomain console = new MemoryDomain("CONSOLE");
		EnhancedRamByteArea bigRamArea = new EnhancedRamByteArea(0, 0x10000); 
		MemoryEntry bigRamEntry = new MemoryEntry("RAM", console, 0, MemoryDomain.PHYSMEMORYSIZE, 
				bigRamArea);
		console.mapEntry(bigRamEntry);
		return console;
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileComma(v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compilePostpone(ITargetWord word) throws AbortException {
		compileTick(word);
		compile((ITargetWord) require("LITERAL"));
		compile((ITargetWord) require("compile,"));
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileDoVar()
	 */
	@Override
	protected void compileDoVar() {
		compileOpcode(IbranchX|0);
		compileOpcode(Idovar);
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileDoDoes(v9t9.forthcomp.HostContext)
	 */
	@Override
	public int compileDoDoes(HostContext hostContext) throws AbortException {
		// must be call'able
		alignDP();
		int addr = getDP();
		compileOpcode(IRfrom);
		return addr;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileDoes(int)
	 */
	@Override
	public void compileDoes(HostContext hostContext, DictEntry entry, int targetDP) throws AbortException {
		/*
		// paramAddr is after EXIT
		int dataSize = (getDP() - entry.getParamAddr());
		if (dataSize >= 8) {
			throw hostContext.abort("cannot invoke DOES> on " + entry.getName() + " since its data is too large"); 
		}
		writeChar(entry.getParamAddr() - 1, IbranchX | dataSize);
		compileOpcode(IbranchW);
		
		int reloc = addRelocation(dp, 
				RelocType.RELOC_ABS_ADDR_16, 
				targetDP,
				entry.getName());
		compileCell(reloc);
		//compileCell(targetDP);
*/
		addRelocation(entry.getContentAddr(), 
				RelocType.RELOC_CALL_15S1, 
				targetDP);
		//compileCell(reloc);
	}
	
}
