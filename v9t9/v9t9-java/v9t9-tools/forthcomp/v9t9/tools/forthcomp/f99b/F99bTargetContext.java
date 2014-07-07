/*
  F99bTargetContext.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.f99b;

import static v9t9.machine.f99b.asm.InstF99b.*;

import java.util.Stack;

import v9t9.common.machine.IBaseMachine;
import v9t9.machine.f99b.asm.InstF99b;
import v9t9.machine.f99b.cpu.CpuF99b;
import v9t9.machine.f99b.cpu.CpuStateF99b;
import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.BaseGromTargetContext;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.RelocEntry;
import v9t9.tools.forthcomp.TargetContext;
import v9t9.tools.forthcomp.RelocEntry.RelocType;
import v9t9.tools.forthcomp.f99b.words.ExitI;
import v9t9.tools.forthcomp.f99b.words.FieldComma;
import v9t9.tools.forthcomp.words.HostLiteral;
import v9t9.tools.forthcomp.words.IPrimitiveWord;
import v9t9.tools.forthcomp.words.TargetColonWord;
import v9t9.tools.forthcomp.words.TargetUserVariable;
import v9t9.tools.forthcomp.words.TargetWord;
import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class F99bTargetContext extends BaseGromTargetContext {

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

	private DictEntry stub16BitU8Lit;
	//private boolean localSupport;

	private DictEntry stub8BitNegLit;

	@SuppressWarnings("unused")
	private int startColonWord;

	private ITargetWord cellWord;

	private ITargetWord cellPlusWord;
	

	/**
	 * @param littleEndian
	 * @param charBits
	 * @param cellBits
	 * @param memorySize
	 */
	public F99bTargetContext(int memorySize) {
		super(false, 8, 16, memorySize);
		
		stub8BitOpcode = defineStub("<<8-bit opcode>>");
		stub16BitOpcode = defineStub("<<16-bit opcode>>");
		stub4BitLit = defineStub("<<4-bit lit>>");
		stub4BitJump = defineStub("<<4-bit jump>>");
		stub8BitJump = defineStub("<<8-bit jump>>");
		stub8BitLit = defineStub("<<8-bit lit>>");
		stub8BitNegLit = defineStub("<<8-bit neg lit>>");
		stub16BitLit = defineStub("<<16-bit lit>>");
		stub16BitU8Lit = defineStub("<<16-bit U8 lit>>");
		stub16BitAddr = defineStub("<<16-bit addr>>");
		stub16BitJump = defineStub("<<16-bit jump>>");
		stubCall = defineStub("<<call>>");
		
	}
	
	static class TargetSQuote extends TargetWord {
		/**
		 * @param entry
		 */
		public TargetSQuote(DictEntry entry) {
			super(entry);
			
			setCompilationSemantics(new ISemantics() {
				
				public void execute(HostContext hostContext, TargetContext targetContext)
						throws AbortException {
					//targetContext.compile(targetContext.require("((s\"))"));
					targetContext.buildCall(targetContext.require("((s\"))"));
				}
			});
			
		}

	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineBuiltins()
	 */
	@Override
	public void defineBuiltins() throws AbortException {
		super.defineBuiltins();
		
		defineInlinePrim("#CELL", IlitX | 2);
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
		definePrim("2swap", Iswap_d);
		definePrim("over", Iover);
		definePrim("2over", Iover_d);
		definePrim("rot", Irot);
		definePrim("2rot", Irot_d);
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
		defineInlinePrim("um/mod", Iudivmod);
		
		defineInlinePrim("nip", Iswap, Idrop);
		
		definePrim("invert", Iinv);
		definePrim("dinvert", Iinv_d);
		definePrim("not", Inot);
		definePrim("dnot", Inot_d);
		definePrim("or", Ior);
		definePrim("dor", Ior_d);
		definePrim("and", Iand);
		definePrim("dand", Iand_d);
		definePrim("xor", Ixor);
		definePrim("dxor", Ixor_d);
		definePrim("nand", Inand);
		definePrim("dnand", Inand_d);
		
		definePrim(">r", ItoR);
		definePrim("2>r", ItoR_d);
		definePrim("r>", IRfrom);
		definePrim("2r>", IRfrom_d);
		definePrim("rdrop", Irdrop);
		definePrim("r@", IatR);
		definePrim("i", IatR);
		definePrim("2r@", IatR_d);
		defineInlinePrim("i'", Irpidx, 1);
		defineInlinePrim("j", Irpidx, 2);
		defineInlinePrim("j'", Irpidx, 3);
		defineInlinePrim("k", Irpidx, 4);
		defineInlinePrim("k'", Irpidx, 5);
		
		defineInlinePrim("sp@", IcontextFrom, CTX_SP);
		defineInlinePrim("sp!", ItoContext, CTX_SP);
		defineInlinePrim("rp@", IcontextFrom, CTX_RP);
		defineInlinePrim("rp!", ItoContext, CTX_RP);
		defineInlinePrim("lp@", IcontextFrom, CTX_LP);
		defineInlinePrim("lp!", ItoContext, CTX_LP);
		
		definePrim("(do)", ItoR_d);
		defineInlinePrim("(loop)", IloopUp);
		defineInlinePrim("(+loop)", IplusLoopUp);
//		defineInlinePrim("(uloop)", IuloopUp);
//		defineInlinePrim("(u+loop)", IuplusLoopUp);
		defineInlinePrim("(?do)", Idup_d, ItoR_d, Isub, I0branchB);
		
		definePrim("execute", Iexecute);

		definePrim("?dup", Iqdup);
		definePrim("2dup", Idup_d);
		//definePrim("(context>)", IcontextFrom);
		//definePrim("(>context)", ItoContext);
		//definePrim("(user)", Iuser);

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

		defineInlinePrim("D0<", IlitX_d, Icmp+CMP_LT);
		defineInlinePrim("D0<=", IlitX_d, Icmp+CMP_LE);
		defineInlinePrim("D0>", IlitX_d, Icmp+CMP_GT);
		defineInlinePrim("D0>=", IlitX_d, Icmp+CMP_GE);
		defineInlinePrim("DU0<", IlitX_d, Icmp+CMP_ULT);
		defineInlinePrim("DU0<=", IlitX_d, Icmp+CMP_ULE);
		defineInlinePrim("DU0>", IlitX_d, Icmp+CMP_UGT);
		defineInlinePrim("DU0>=", IlitX_d, Icmp+CMP_UGE);
		defineInlinePrim("D<", Icmp_d+CMP_LT);
		defineInlinePrim("D<=", Icmp_d+CMP_LE);
		defineInlinePrim("D>", Icmp_d+CMP_GT);
		defineInlinePrim("D>=", Icmp_d+CMP_GE);
		defineInlinePrim("DU<", Icmp_d+CMP_ULT);
		defineInlinePrim("DU<=", Icmp_d+CMP_ULE);
		defineInlinePrim("DU>", Icmp_d+CMP_UGT);
		defineInlinePrim("DU>=", Icmp_d+CMP_UGE);
		
		defineInlinePrim("unloop", Irdrop_d);
		//defineInlinePrim("(unloop)", IRfrom, Irdrop_d, ItoR);
		defineInlinePrim("2rdrop", Irdrop_d);
		defineInlinePrim("2/", I2div);
		defineInlinePrim("2*", I2times);
		
		defineInlinePrim("LSHIFT", Ilsh);
		defineInlinePrim("RSHIFT", Iash);
		defineInlinePrim("URSHIFT", Irsh);
		defineInlinePrim("CRSHIFT", Icsh);
		
		defineInlinePrim("SWPB", IlitX | 8, Icsh);
		
		defineInlinePrim("DLSHIFT", Ilsh_d);
		defineInlinePrim("DRSHIFT", Iash_d);
		defineInlinePrim("DURSHIFT", Irsh_d);
		defineInlinePrim("DCRSHIFT", Icsh_d);
		
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
		defineInlinePrim("(cmove)", Icmove);
		
		defineInlinePrim("(LITERAL)", IlitW);
		defineInlinePrim("(DLITERAL)", IlitD_d);
		
		//defineInlinePrim("(s\")", IcontextFrom, CTX_PC, IlitX | 5, Iadd, Idup, I1plus, Iswap, Icload);
		defineInlinePrim("((s\"))", Irdrop, IatR, Idup, I1plus, Iswap, Icload, Idup, IRfrom, Iadd, I1plus, ItoR);
		
		define("(S\")", new TargetSQuote(defineEntry("(S\")")));
		compileCall((ITargetWord) find("((s\"))"));
		compileOpcode(Iexit);
		
		defineInlinePrim("0<>", I0equ, Inot); 
		defineInlinePrim("<>", Iequ, Inot);
	}
	
	private void definePrim(String string, int opcode) {
		define(string, new F99PrimitiveWord(defineEntry(string), opcode));
		compileOpcode(opcode);
		compileByte(Iexit);
	}

	private void defineInlinePrim(String string, int... opcodes) {
		define(string, new F99InlineWord(defineEntry(string), opcodes));
		for (int i : opcodes)
			compileOpcode(i);
		compileByte(Iexit);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineEntry(java.lang.String)
	 */
	@Override
	public DictEntry defineEntry(String name) {
		DictEntry entry = super.defineEntry(name);
		startColonWord = 0;
		return entry;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineColonWord(java.lang.String)
	 */
	@Override
	public TargetColonWord defineColonWord(String name) {
		TargetColonWord word = super.defineColonWord(name);
		
		startColonWord = getDP();
		
		return word;
	}
	
	@Override
	public void initWordEntry() {
		alignDP();
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#doResolveRelocation(v9t9.forthcomp.RelocEntry)
	 */
	@Override
	protected int doResolveRelocation(RelocEntry reloc) throws AbortException {
		if (reloc.type == RelocType.RELOC_CALL_15S1) {
			int val = reloc.target;
			if ((val & 1) != 0)
				throw new AbortException("Call address is odd: " + HexUtils.toHex4(val));
			val = ((val >> 1) & 0x7fff) | 0x8000;
			return val;
		}
		return super.doResolveRelocation(reloc);
	}
	public void compile(ITargetWord word) throws AbortException {
		word.getEntry().use();
		if (word instanceof IPrimitiveWord)
			word.getCompilationSemantics().execute(hostCtx, this);
		else
			compileCall(word);
	}

//	/* (non-Javadoc)
//	 * @see v9t9.forthcomp.words.TargetContext#compileLoad(int)
//	 */
//	@Override
//	protected void compileLoad(int bytes) {
//		if (bytes == 1)
//			compileOpcode(Icload);
//		else if (bytes == 2)
//			compileOpcode(Iload);
//		else if (bytes == 4)
//			compileOpcode(Iload_d);
//		else
//			assert false;
//	}
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
//		if (HostContext.DEBUG)
//			logfile.println("T>" + Integer.toHexString(getDP())+" C, " + Integer.toHexString(opcode));
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
			if (value < 0) 
				stub8BitNegLit.use();
			compileByte(IlitB);
			compileByte(value);
			return stub8BitLit;
		} else {
			if ((value & 0xff) == value)
				stub16BitU8Lit.use();

			compileByte(IlitW);
			int ptr = alloc(2);
			writeCell(ptr, value & 0xffff);
			return stub16BitLit;
		}
	}
	
	@Override
	public void buildCell(int loc) {
		super.buildCell(loc);
		stub16BitLit.use();
	}
	@Override
	public void buildChar(int val) {
		super.buildChar(val);
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
	public void doExportState(HostContext hostContext, IBaseMachine machine, int baseSP, int baseRP, int baseUP) throws AbortException {
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
	public void doImportState(HostContext hostContext, IBaseMachine machine, int baseSP, int baseRP) {
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
		super.pushFixup(hostContext);
		alloc(1);		// assume short branch
	}

	@Override
	protected int writeJump(HostContext hostContext, int opAddr, int target)
			throws AbortException {
		
		int diff = target - opAddr;
		
		// Opcode is already compiled as 1-byte branch, so diff is relative to offset
		
		// When we jump backward, measure from inst.pc, else from inst.pc + inst.size
		if (diff < -128 - 1 || diff >= 128) {
			throw hostContext.abort("jump too long: " + diff);
			//System.err.println("jump too long: " + diff);
		}
		
		if (diff < -8  || diff >= 8 ) {
			stub8BitJump.use();
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
			
			writeChar(opAddr - 1, newOp | (diff & 0xf));
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetContext#writeLoopJump(int)
	 */
	@Override
	protected void writeLoopJump(int opAddr) throws AbortException {
		writeJumpAlloc(opAddr, true);
	}

	@Override
	protected void writeJumpAlloc(int opAddr, boolean conditional)
			throws AbortException {
		
		int diff = opAddr - getDP();
		
		int baseOpcode = conditional ? I0branchX : IbranchX;
		if (diff < -130 || diff >= 128) {
			stub16BitJump.use();
			if (diff > 0)
				diff -= 3;
			else
				diff -= 2;
			baseOpcode = baseOpcode == IbranchX ? IbranchW : I0branchW;
			compileOpcode(baseOpcode);
			int ptr = alloc(cellSize);
			writeCell(ptr, diff);
		} else if (diff < -8 + 1 || diff >= 8 + 1) {
			stub8BitJump.use();
			if (diff > 0)
				diff -= 2;
			else
				diff--;
			baseOpcode = baseOpcode == IbranchX ? IbranchB : I0branchB;
			compileOpcode(baseOpcode);
			compileByte((diff & 0xff));
		}
		else {
			stub4BitJump.use();
			diff--;
			compileOpcode(baseOpcode | (diff & 0xf));
		}
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
		
		hostContext.define("CTX_SP", new HostLiteral(CTX_SP, false));
		hostContext.define("CTX_SP0", new HostLiteral(CTX_SP0, false));
		hostContext.define("CTX_RP", new HostLiteral(CTX_RP, false));
		hostContext.define("CTX_RP0", new HostLiteral(CTX_RP0, false));
		hostContext.define("CTX_UP", new HostLiteral(CTX_UP, false));
		hostContext.define("CTX_LP", new HostLiteral(CTX_LP, false));
		hostContext.define("CTX_PC", new HostLiteral(CTX_PC, false));

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

	/*
	private int getLocalOffs(int index) {
		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
		int offs = entry.getLocalCount() - index;
		return -offs * 2;
	}

	@Override
	public void ensureLocalSupport(HostContext hostContext) throws AbortException {
		if (lpUser == null) {
			lpUser = (TargetUserVariable) find("LP");
			if (lpUser == null) {
				lpUser = defineUser("LP", 1);
				
				HostContext subContext = new HostContext(this);
				hostContext.copyTo(subContext);
				subContext.getStream().push(
						"false <export\n"+
						": (>LOCALS) LP @    	RP@ LP ! ; \\ caller pushes R> \n" +
						": (LOCALS>) R>  LP @ RP!   R>  LP !  >R ; \n" +
						"export>\n");
				ForthComp comp = new ForthComp(subContext, this);
				comp.parse();
				if (comp.getErrors() > 0)
					throw hostContext.abort("Failed to compile support code");
				hostContext.copyFrom(subContext);
			}
		}
	}
	
	@Override
	public void compileSetupLocals() throws AbortException {

		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
		if (entry.hasLocals())
			throw new AbortException("cannot add more locals now");
		
		compile(require("(>LOCALS)"));
		compileOpcode(ItoR);	// save old LP
		
	}

	@Override
	public void compileCleanupLocals() throws AbortException {
		DictEntry entry = (getLatest()).getEntry();
		if (entry.hasLocals()) {
			compile(require("(LOCALS>)"));
		}
	}
	
	@Override
	public void compileInitLocal(int index) throws AbortException {
		compileOpcode(ItoR);
	}
	
	@Override
	public void compileLocalAddr(int index) {
		compileUser(lpUser);
		compileOpcode(Iload);
		compileLiteral(getLocalOffs(index), false, true);
		compileOpcode(Iadd);
	}
	
	*/

	public boolean isLocalSupportAvailable(HostContext hostContext) throws AbortException {
		return true;
	}
	
	@Override
	public void ensureLocalSupport(HostContext hostContext) throws AbortException {
		// ": (>LOCALS) LP@  	RP@ LP! ; \\ caller pushes R> \n" +
		//			": (LOCALS>) R>  LP@ RP!   R>  LP!  >R ; \n" +
	}
	
	@Override
	public void compileSetupLocals(HostContext hostContext) throws AbortException {

		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
		if (entry.hasLocals())
			throw new AbortException("cannot add more locals now");
		
		compileOpcode(ItoLocals);
		
	}

	@Override
	public void compileCleanupLocals(HostContext hostContext) throws AbortException {
		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
		if (entry.hasLocals()) {
			compileOpcode(IfromLocals);
		}
	}
	
	@Override
	public void compileAllocLocals(int count) throws AbortException {
		compileOpcode(Ilalloc);
		compileByte(count);
	}
	
	@Override
	public void compileLocalAddr(int index) {
		compileOpcode(Ilocal);
		compileByte(index);
	}

	
	@Override
	public void compileFromLocal(int index) throws AbortException {
		compileOpcode(Ilpidx);
		compileByte(index);
	}

	@Override
	public void compileToLocal(int index) throws AbortException {
		compileLocalAddr(index);
		compileByte(Istore);
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
	public void compileDoUser(int offset) {
		if (offset < 256) {
			compileOpcode(Iupidx);
			compileByte(offset);
		} else {
			doCompileLiteral(offset, false, true);
			compileOpcode(Iuser);
		}
		compileOpcode(Iexit);
	}


	public void compileDoRomDefer(int offset) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param hostContext TODO
	 * @throws AbortException 
	 * 
	 */
	public void compileExitI(HostContext hostContext) throws AbortException {
		if (((ITargetWord) getLatest()).getEntry().hasLocals())
			compileCleanupLocals(hostContext);

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
			//compile(require("DOLIT"));
		} else { 
			compileOpcode(IlitD_d);
			loc = alloc(cellSize * cells);
			compileOpcode(Iexit);
			//compile(require("DODLIT"));
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
	public void compileWordParamAddr(ITargetWord word) {
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
	 * @see v9t9.forthcomp.words.TargetContext#compileDoVar()
	 */
	@Override
	protected void compileDoVar() {
		compileOpcode(IbranchX|0);
		compileOpcode(Idovar);
	}
	

	public int buildDoes(HostContext hostContext) throws AbortException {
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

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetContext#compileExit(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	public void compileExit(HostContext hostContext) throws AbortException {
		super.compileExit(hostContext);
		
//		int endDP = getDP();
//		if (startColonWord != 0) {
//			peephole(startColonWord, endDP);
//		}
	}

	/**
	 * @param startColonWord2
	 * @param endDP
	 * @return
	 */
	protected void peephole(int start, int end) {
		if (cellWord == null) 
			cellWord = (ITargetWord) find("cell");
		if (cellPlusWord == null) 
			cellPlusWord = (ITargetWord) find("cell+");
		
		setDP(start);
		while (start < end) {
			start = peepholePattern(start);
		}
	}

	protected void peepByte(int start) {
		writeChar(getDP(), readChar(start));
		incDP(1);
	}
	/**
	 * @param start
	 * @return
	 */
	protected int peepholePattern(int start) {
		int addr = findReloc(start);
		if (addr == 0) {
			// not a call but a lit
			int op = readChar(start);
			if ((op & 0xf0) == InstF99b.I0branchX) {
				// fixup a branch
				peepByte(start++); 
			}
			else if (op == InstF99b.I0branchB) {
				peepByte(start++); 
				peepByte(start++); 
				
			}
			else if (op == InstF99b.I0branchW) {
				peepByte(start++); 
				peepByte(start++); 
				peepByte(start++); 
			}
			else if (op == InstF99b.IbranchX) {
				peepByte(start++); 

			}
			else if (op == InstF99b.IbranchB) {
				peepByte(start++); 
				peepByte(start++); 
				
			}
			else if (op == InstF99b.IbranchW) {
				peepByte(start++); 
				peepByte(start++); 
				peepByte(start++); 
				
			}
			else {
				peepByte(start++); 
			}
			return start;
		}
		
		if (cellWord != null && addr == cellWord.getEntry().getAddr()) {
			writeChar(getDP(), InstF99b.IlitX + 2);
			removeReloc(start++);
			incDP(1);
			return start + 2;
		}
		if (cellPlusWord != null && addr == cellPlusWord.getEntry().getAddr()) {
			writeChar(getDP(), InstF99b.I2plus);
			removeReloc(start);
			incDP(1);
			return start + 2;
		}

		peepByte(start++);
		return start;
	}

	/**
	 * @param i
	 */
	private void incDP(int i) {
		setDP(getDP() + i);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetContext#isNativeDefinition()
	 */
	@Override
	public boolean isNativeDefinition() {
		return true;
	}
}
