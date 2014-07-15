/*
  TI99TargetContext.java

  (c) 2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.ti99;

import static v9t9.machine.ti99.cpu.Inst9900.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.machine.IBaseMachine;
import v9t9.engine.memory.MemoryDomain;
import v9t9.machine.ti99.asm.InstructionFactory9900;
import v9t9.machine.ti99.asm.RawInstructionFactory9900;
import v9t9.tools.asm.LLInstruction;
import v9t9.tools.asm.inst9900.AsmInstructionFactory9900;
import v9t9.tools.asm.operand.ll.LLAddrOperand;
import v9t9.tools.asm.operand.ll.LLForwardOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;
import v9t9.tools.asm.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.operand.ll.LLRegisterOperand;
import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.BaseGromTargetContext;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.F9900GromDictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;
import v9t9.tools.forthcomp.RelocEntry;
import v9t9.tools.forthcomp.RelocEntry.RelocType;
import v9t9.tools.forthcomp.words.IPrimitiveWord;
import v9t9.tools.forthcomp.words.TargetColonWord;
import v9t9.tools.forthcomp.words.TargetUserVariable;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;
import ejs.base.utils.TextUtils;

/**
 * We use a direct-threaded model.  Execution branches to code using B.
 * Colon words use IP (R14) to store the pointer to the next XT.
 * 
 * @author ejs
 *
 */
public class TI99TargetContext extends BaseGromTargetContext  {
	/**
	 * 
	 */
	private static final String DORDEFER = "DORDEFER";
//	private final static boolean relBranches = true;
//	private final static boolean inlineNext = true;
	
	public static final int REG_TOS = 1;
	public static final int REG_TMP = 0;
	public static final int REG_R2 = 2;
	public static final int REG_R3 = 3;
	
	public static final int REG_DOCOL = 4;
	public static final int REG_DOCON = 5;
	public static final int REG_DOUSER = 6;
	public static final int REG_DOVAR = 7;
	public static final int REG_DODOES = 8;
	
	public static final int REG_UP = 10;
	// RT = 11
	// CRUBASE = 12
	public static final int REG_RP = 13;
	public static final int REG_IP = 14;
	public static final int REG_SP = 15;
	
	
	private enum StockInstruction {
		/** execute next word in threaded code */
		NEXT,
		/** save TOS on stack */
		PUSH_TOS,
		/** pop from stack to TOS */
		POP_TOS,
		/** pop two entries from stack, losing the top */
		POP2_TOS, 
		
	}
	
	private AsmInstructionFactory9900 asmInstrFactory;
	private InstructionFactory9900 instrFactory;

	private final LLOperand TOS = reg(REG_TOS);
	private final LLOperand TMP = reg(REG_TMP);
//	private final LLOperand R2 = reg(REG_R2);
//	private final LLOperand R3 = reg(REG_R3);

//	private int interpLoop;


	public TI99TargetContext(int memorySize) {
		super(false, 8, 16, memorySize);
		
		asmInstrFactory = new AsmInstructionFactory9900();
		instrFactory = new InstructionFactory9900();
		rawInstructionFactory = new RawInstructionFactory9900();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.BaseGromTargetContext#setGrom(v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public void setGrom(MemoryDomain grom) {
		super.setGrom(grom);

		setGP(grom.getSize());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.IGromTargetContext#finishDict()
	 */
	@Override
	public void finishDict() {
		// move content to start
		int gp = getGP();
		int len = grom.getSize() - gp;
		for (int i = 0; i < len; i++) {
			grom.writeByte(i, grom.readByte(gp + i));
		}
		setGP(len);
	}

	private void writeInstruction(int instCode, LLOperand... ops) {
		LLInstruction llinst = createInstruction(instCode, ops);
		alignDP();
		llinst.setPc(getDP());
		compileInstr(llinst);
	}
	private LLInstruction createInstruction(int instCode, LLOperand... ops) {
		LLInstruction llinst = new LLInstruction(asmInstrFactory);
		llinst.setInst(instCode);
		if (ops.length > 0) {
			llinst.setOp1(ops[0]);
			if (ops.length > 1) {
				llinst.setOp2(ops[1]);
				if (ops.length > 2) {
					llinst.setOp3(ops[2]);
				}
			}
		}
		return llinst;
	}
	protected RawInstruction createInstr(LLInstruction llinst) {
		RawInstruction rawInstr;
		try {
			rawInstr = asmInstrFactory.createRawInstruction(llinst);
		} catch (ResolveException e) {
			throw new RuntimeException(e.getMessage());
		}
		return rawInstr;
	}

	protected void compileInstr(LLInstruction llinst) {
		llinst.setPc(getDP());
		compileInstr(createInstr(llinst));
	}
	protected void compileInstr(StockInstruction stock) {
		for (LLInstruction llinst : createInstructions(stock)) {
			llinst.setPc(getDP());
			compileInstr(createInstr(llinst));
		}
	}
	protected void compileInstr(RawInstruction rawInstr) {
		byte[] bytes = instrFactory.encodeInstruction(rawInstr);
		int dp = rawInstr.getPc();
		logfile.println("T>" + HexUtils.toHex4(dp) +" " + rawInstr);
		alloc(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
			writeChar(dp + i, bytes[i]);
		}
	}

	/**
	 * @param instsAndOpcodes
	 * @return
	 */
	protected LLInstruction[] defineInstrs(Object... instsAndOpcodes) throws AbortException {
		List<LLInstruction> llInsts = new ArrayList<LLInstruction>(1);
		Map<String, Integer> labelAddrs = new HashMap<String, Integer>();
		Map<String, List<LLInstruction>> fwds = new HashMap<String, List<LLInstruction>>();
		int addr = getDP();
		for (int i = 0; i < instsAndOpcodes.length; ) {
			Object obj = instsAndOpcodes[i++];
			if (obj instanceof String) {
				// defining label
				labelAddrs.put((String) obj, addr);
				List<LLInstruction> fwd = fwds.remove(obj);
				if (fwd != null) {
					// resolve refs
					for (LLInstruction fll : fwd) {
						fll.setOp1(new LLPCRelativeOperand(null, addr - fll.getPc()));
					}
				}
				continue;
			}
			if (obj instanceof StockInstruction) {
				for (LLInstruction llinst : createInstructions((StockInstruction) obj)) {
					llInsts.add(llinst);
					try {
						addr += asmInstrFactory.createRawInstruction(llinst).getSize();
					} catch (ResolveException e) {
						e.printStackTrace();
					}
				}
				continue;
			}
			
			int inst = (Integer) obj;
			LLInstruction llInstruction = new LLInstruction(instrFactory);
			llInstruction.setPc(addr);
			llInstruction.setInst(inst);

			int opaddr = addr + 2;
			for (int opidx = 1; opidx <= 3; opidx++) {
				if (i >= instsAndOpcodes.length || !isOperand(instsAndOpcodes[i]))
					break;
				Object opobj = instsAndOpcodes[i++];
				if (opobj instanceof String) {
					// label resolution
					String label = ((String) opobj).substring(1);
					if (!labelAddrs.containsKey(label)) {
						List<LLInstruction> fwd = fwds.get(label);
						if (fwd == null) {
							fwd = new ArrayList<LLInstruction>(1);
							fwds.put(label, fwd);
						}
						fwd.add(llInstruction);
						opobj = new LLPCRelativeOperand(null, -1);
					} else {					
						opobj = new LLImmedOperand(labelAddrs.get(label));
					}
				}
				LLOperand op = resolve(opaddr, (LLOperand) opobj);
				if (op instanceof LLImmedOperand || op instanceof LLAddrOperand)
					opaddr += 2;
				switch (opidx) {
				case 1:
					llInstruction.setOp1(op); break;
				case 2:
					llInstruction.setOp2(op); break;
				case 3:
					llInstruction.setOp3(op); break;
				}
			}
			llInsts.add(llInstruction);
			
			try {
				addr += asmInstrFactory.createRawInstruction(llInstruction).getSize();
			} catch (ResolveException e) {
				e.printStackTrace();
			}
		}
		
		if (!fwds.isEmpty()) {
			throw new AbortException("undefined labels: " + TextUtils.catenateStrings(fwds.keySet(), " "));
		}
		return llInsts.toArray(new LLInstruction[llInsts.size()]);
	}

	/**
	 * @param addr
	 * @param opobj
	 * @return
	 */
	private LLOperand resolve(int addr, LLOperand op) {
		if (op instanceof LLForwardOperand) {
			if (op.getOriginal() instanceof LLImmedOperand) {
				LLImmedOperand orig = (LLImmedOperand) op.getOriginal();
				addRelocation(addr, RelocType.RELOC_ABS_ADDR_16, orig.getImmediate());
				return orig;
			}
			else if (op.getOriginal() instanceof LLAddrOperand) {
				LLAddrOperand orig = (LLAddrOperand) op.getOriginal();
				addRelocation(addr, RelocType.RELOC_ABS_ADDR_16, orig.getAddress());
				return orig;
			}
		}
		return op;
	}

	/**
	 * @param obj
	 * @return
	 */
	private Collection<LLInstruction> createInstructions(
			StockInstruction obj) {
		Collection<LLInstruction> llInsts = new ArrayList<LLInstruction>(2);
		switch ((StockInstruction) obj) {
		case NEXT:
			// get the next XT
			llInsts.add(createInstruction(Imov, regInc(REG_IP), TMP));
			// B to it
			llInsts.add(createInstruction(Ib, regInd(REG_TMP)));
			break;
			
		case PUSH_TOS:
			llInsts.add(createInstruction(Idect, reg(REG_SP)));
			llInsts.add(createInstruction(Imov, TOS, regInd(REG_SP)));
			break;

		case POP_TOS:
			llInsts.add(createInstruction(Imov, regInc(REG_SP), TOS));
			break;
			
		case POP2_TOS:
			llInsts.add(createInstruction(Iinct, reg(REG_SP)));
			llInsts.add(createInstruction(Imov, regInc(REG_SP), TOS));
			break;
			
		default:
			throw new UnsupportedOperationException(obj.toString());
		}
		return llInsts;
	}

	/**
	 * @param object
	 * @return
	 */
	private boolean isOperand(Object object) {
		return object instanceof LLOperand || 
				(object instanceof String && ((String) object).startsWith(">"));
	}


	protected IPrimitiveWord layoutPrimitiveWord(LLInstruction[] llInsts,
			IWord word) {
		for (LLInstruction instr : llInsts) {
			compileInstr(instr);
		}
		compileInstr(StockInstruction.NEXT);
		return (IPrimitiveWord) word;
	}

	private LLOperand immed(int value) {
		return new LLImmedOperand(value);
	}

	private LLOperand xtaddr(String word) {
		return new LLForwardOperand(addr(require(word).getEntry().getContentAddr()), 2);
	}
	
	private LLOperand addr(int value) {
		return new LLAddrOperand(null, value);
	}

	private LLOperand reg(int reg) {
		return new LLRegisterOperand(reg);
	}

	private LLOperand regInc(int reg) {
		return new LLRegIncOperand(reg);
	}

	private LLOperand regInd(int reg) {
		return new LLRegIndOperand(reg);
	}

	private LLOperand regOffs(int reg, int offset) {
		if (offset == 0)
			return regInd(reg);
		return new LLRegOffsOperand(null, reg, offset);
	}

	private void pushTOS() {
		writeInstruction(Idect, reg(REG_SP));
		writeInstruction(Imov, TOS, regInd(REG_SP));
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.TargetContext#defineColonPrims()
	 */
	@Override
	public void defineColonPrims() throws AbortException {
		super.defineColonPrims();
		

	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineBuiltins()
	 */
	@Override
	public void definePrims() throws AbortException {
		super.definePrims();

	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.TargetContext#defineCompilerWords(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	public void defineCompilerWords(HostContext hostContext) {
		super.defineCompilerWords(hostContext);
	}

	/**
	 * Call addresses must be aligned
	 */
	public void initWordEntry() {
		alignDP();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.TargetContext#compileExit(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	public void compileExit(HostContext hostContext) throws AbortException {
		super.compileExit(hostContext);
		throw hostContext.abort("not implemented");
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.ITargetContext#compileEndCode(java.lang.String)
	 */
	@Override
	public void compileEndCode() {
		compileInstr(StockInstruction.NEXT);
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetContext#defineColonWord(java.lang.String)
	 */
	@Override
	public TargetColonWord defineColonWord(String name) {
		TargetColonWord word = super.defineColonWord(name);
		
		logfile.println("T>" + HexUtils.toHex4(getDP()) + " docol");
		//writeCell(alloc(cellSize), doCol.getEntry().getContentAddr());
		
		writeInstruction(Ibl, regInd(REG_DOCOL));
		
		return word;
	}

	public void compile(ITargetWord word) {
		word.getEntry().use();
		compileCall(word);
	}

	public void compileByte(int opcode) {
		if (HostContext.DEBUG)
			logfile.println("T>" + HexUtils.toHex4(getDP())+" C, " + HexUtils.toHex4(opcode));
		writeChar(alloc(1), opcode);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileDoubleLiteral(int)
	 */
	@Override
	public void compileDoubleLiteral(int valueLo, int valueHi, boolean isUnsigned, boolean optimize) {
		compileLiteral(valueLo, isUnsigned, optimize);
		compileLiteral(valueHi, isUnsigned, optimize);
	}
	
	@Override
	public void compileLiteral(int value, boolean isUnsigned, boolean optimize) {
		pushTOS();
		doCompileLiteral(value, isUnsigned, optimize);
	}
	
	private void doCompileLiteral(int value, boolean isUnsigned, boolean optimize) {
		writeInstruction(Ili, TOS, immed(value));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileAddr(int)
	 */
	@Override
	public void buildCell(int loc) {
		logfile.println("T>" + HexUtils.toHex4(dp) +" = " + HexUtils.toHex4(loc));
		int ptr = alloc(cellSize);
		writeCell(ptr, loc);
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
		ICpuState cpu = machine.getCpu().getState();
		
		//cpu.setBaseSP((short) baseSP);
		
		Stack<Integer> stack = hostContext.getDataStack();
		cpu.setRegister(REG_SP, (short) (baseSP - stack.size() * cellSize));
		for (int i = 0; i < stack.size(); i++)
			machine.getConsole().writeWord(cpu.getRegister(REG_SP) + i * 2, (short) (int) stack.get(stack.size() - i - 1));
		
		//cpu.setBaseRP((short) baseRP);
		
		stack = hostContext.getReturnStack();
		cpu.setRegister(REG_RP, (short) (baseRP - stack.size() * cellSize));
		for (int i = 0; i < stack.size(); i++)
			machine.getConsole().writeWord(cpu.getRegister(REG_RP) + i * 2, (short) (int) stack.get(stack.size() - i - 1));
		
		//cpu.setBaseUP((short) baseUP);
		//cpu.setUP((short) baseUP);
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
		ICpu cpu = machine.getCpu();
		
		Stack<Integer> stack = hostContext.getDataStack();
		stack.clear();
		
		int curSP = cpu.getState().getRegister(REG_SP) & 0xffff;
		while (baseSP > 0 && baseSP > curSP) {
			baseSP -= 2;
			stack.push((int) machine.getConsole().readWord(baseSP));
		}
		
		stack = hostContext.getReturnStack();
		stack.clear();
		
		int curRP = cpu.getState().getRegister(REG_RP) & 0xffff;
		while (curRP > 0 && baseRP > curRP) {
			curRP -= 2;
			stack.push((int) machine.getConsole().readWord(baseRP));
		}

	}
	@Override
	public void pushFixup(HostContext hostContext) {
		super.pushFixup(hostContext);
		alloc(cellSize);		// assume short
	}
	
	private int calcJump(int opAddr, int target) {
		return target - (opAddr + cellSize);
	}

	
	protected int writeJump(int opAddr, int target)
			throws AbortException {
		
		logfile.println("T>" + HexUtils.toHex4(opAddr) +" = " + HexUtils.toHex4(target));
		
		// Opcode is already compiled as 2-byte branch, so diff is relative to offset
		writeCell(opAddr, calcJump(opAddr, target));
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetContext#writeLoopJump(int)
	 */
	@Override
	protected void writeLoopJump(int opAddr) throws AbortException {
		buildCell(calcJump(getDP(), opAddr));
	}


	protected void writeJumpAlloc(int opAddr, boolean conditional)
			throws AbortException {

		buildCall((conditional ? require("0BRANCH") : require("BRANCH")));
		buildCell(calcJump(getDP(), opAddr));
	}

	public void compileUser(TargetUserVariable var) {
		int index = var.getIndex();
		pushTOS();
		writeInstruction(Imov, regOffs(REG_UP, index * cellSize), TOS);
	}
	
	public boolean isLocalSupportAvailable(HostContext hostContext) throws AbortException {
		return false;
	}
	
	@Override
	public void ensureLocalSupport(HostContext hostContext) throws AbortException {
	}
	
	@Override
	public void compileSetupLocals(HostContext hostContext) throws AbortException {

//		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
//		if (entry.hasLocals())
//			throw new AbortException("cannot add more locals now");
//		
//		compileOpcode(ItoLocals);
		
	}

	@Override
	public void compileCleanupLocals(HostContext hostContext) throws AbortException {
//		DictEntry entry = ((ITargetWord) getLatest()).getEntry();
//		if (entry.hasLocals()) {
//			compileOpcode(IfromLocals);
//		}
	}
	
	@Override
	public void compileAllocLocals(int count) throws AbortException {
//		compileOpcode(Ilalloc);
//		compileChar(count);
	}
	
	@Override
	public void compileLocalAddr(int index) {
//		compileOpcode(Ilocal);
//		compileChar(index);
	}

	
	@Override
	public void compileFromLocal(int index) throws AbortException {
//		compileOpcode(Ilpidx);
//		compileChar(index);
	}

	@Override
	public void compileToLocal(int index) throws AbortException {
//		compileLocalAddr(index);
//		compileByte(Istore);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetContext#doResolveRelocation(v9t9.tools.forthcomp.RelocEntry)
	 */
	@Override
	protected int doResolveRelocation(RelocEntry reloc) throws AbortException {
		if (reloc.type == RelocType.RELOC_CONSTANT)
			return readCell(reloc.target + getCellSize());

		return super.doResolveRelocation(reloc);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileDoConstant(int, int)
	 */
	@Override
	public void compileDoConstant(int value, int cells) throws AbortException {
		if (cells == 1) {
			writeInstruction(Ibl, regInd(REG_DOCON));
			buildCell(value);
		} else if (cells == 2) {
			ITargetWord dcon = require("DODCON");
			writeInstruction(Ibl, addr(dcon.getEntry().getContentAddr()));
			buildCell(value & 0xffff);
			buildCell(value >> 16);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileDoUser(int)
	 */
	@Override
	public void compileDoUser(int offset) throws AbortException {
//		pushTOS();
//		writeInstruction(Imov, regOffs(REG_UP, offset), TOS);
//		writeInstruction(Ijmp, threadOp);
		writeInstruction(Ibl, regInd(REG_DOUSER));
		buildCell(offset);
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.ITargetContext#compileDoDefer(int)
	 */
	@Override
	public void compileDoRomDefer(int offset) {
		writeInstruction(Ibl, resolve(getDP() + 2, xtaddr(DORDEFER)));
		buildCell(offset);
	}

	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileDoValue(int, int)
	 */
	@Override
	public int compilePushValue(int cells, int value) throws AbortException {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileWordParamAddr(v9t9.forthcomp.TargetValue)
	 */
	@Override
	public void compileWordParamAddr(ITargetWord word) {
		doCompileLiteral(((ITargetWord)word).getEntry().getParamAddr(), true, true);		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileWordXt(v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compileTick(ITargetWord word) {
		int ptr = alloc(cellSize);

		int reloc = addRelocation(ptr, 
				RelocType.RELOC_ABS_ADDR_16, 
				word.getEntry().getContentAddr());

		writeCell(ptr, reloc);
		
		logfile.println("T>" + HexUtils.toHex4(ptr) + " XT " + word.getName());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileCall(v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void compileCall(ITargetWord word) {
		int pc = alloc(cellSize);
		
		int reloc = addRelocation(pc, 
				RelocType.RELOC_ABS_ADDR_16, 
				word.getEntry().getContentAddr());
		
		writeInstruction(Ibl, addr(reloc));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileDoVar()
	 */
	@Override
	protected void compileDoVar() {
		writeInstruction(Ibl, regInd(REG_DOVAR));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetContext#buildDoDoes(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	public int buildDoes(HostContext hostContext) throws AbortException {
		// host word exits now
		buildCall(require(";S"));
		
		// in target, we will BL *DODOES
		int addr = getDP();

		// code
		writeInstruction(Ibl, regInd(REG_DOCOL));
		
		return addr;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileDoes(int)
	 */
	@Override
	public void compileDoes(HostContext hostContext, DictEntry entry, int targetDP) throws AbortException {
		// replace the BL *DOVAR instruction with
		// BL *DODOES  followed by the DOES> XT
		int from = entry.getParamAddr();
		int to = from + cellSize;
		int len = getDP() - entry.getParamAddr();
		
		logfile.println("DODOES " + entry + ": moving " + HexUtils.toHex4(from) + "-" + HexUtils.toHex4(from + len) + " forward one cell");
		alloc(cellSize);	// note: clears
		
		for (int offs = len - cellSize; offs >= 0; offs -= cellSize) {
			int cell = readCell(memory, from + offs);
			writeCell(memory, to + offs, cell);
		}

		for (int offs = len - cellSize; offs >= 0; offs -= cellSize) {
			RelocEntry reloc = relocEntries.get(from + offs);
			if (reloc != null) {
				relocEntries.put(to + offs, reloc);
				relocEntries.remove(from + offs);
			}
		}

		logfile.println("T>" + HexUtils.toHex4(entry.getParamAddr()) + " = " + HexUtils.toHex4(targetDP)); 

		writeCell(entry.getParamAddr(), targetDP);
		
		entry.setCodeSize(entry.getCodeSize() + cellSize);
//		dp -= cellSize * 2;	// step back to overwrite code from #compileDoVar()
//		writeInstruction(Ibl, regInd(REG_DODOES));	
//		int oldCell = readCell(dp);
//		buildCell(targetDP);
//		buildCell(oldCell);
//		alloc(cellSize);
		writeCell(entry.getContentAddr(), 0x0690 + REG_DODOES);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.TargetContext#buildPushString(v9t9.tools.forthcomp.HostContext, java.lang.String)
	 */
	@Override
	public Pair<Integer, Integer> buildPushString(HostContext hostContext, String string)
			throws AbortException {
		Pair<Integer, Integer> info = super.buildPushString(hostContext, string);
		alignDP();
		return info;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.TargetContext#isLikelyAddress(int)
	 */
	@Override
	protected boolean isLikelyAddress(int value) {
		return value >= 0x4000 && value <= 0xFF00;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.BaseGromTargetContext#createGromDictEntry(int, int, java.lang.String)
	 */
	@Override
	protected DictEntry createGromDictEntry(int size, int entryAddr, String name) {
		// name, link (=>xt), byte
		int dictSize = name.length() + cellSize + 1;
		gp -= dictSize;
		DictEntry entry = new F9900GromDictEntry(dictSize, entryAddr, name, gp);
		return entry;
	}
}
