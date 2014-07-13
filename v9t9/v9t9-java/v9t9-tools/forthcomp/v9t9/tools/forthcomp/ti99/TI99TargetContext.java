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

import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.machine.IBaseMachine;
import v9t9.machine.ti99.asm.InstructionFactory9900;
import v9t9.machine.ti99.asm.RawInstructionFactory9900;
import v9t9.machine.ti99.memory.mmio.Forth9900ConsoleMmioArea;
import v9t9.tools.asm.LLInstruction;
import v9t9.tools.asm.inst9900.AsmInstructionFactory9900;
import v9t9.tools.asm.operand.ll.LLAddrOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;
import v9t9.tools.asm.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.operand.ll.LLRegisterOperand;
import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;
import v9t9.tools.forthcomp.RelocEntry;
import v9t9.tools.forthcomp.RelocEntry.RelocType;
import v9t9.tools.forthcomp.TargetContext;
import v9t9.tools.forthcomp.words.BaseStdWord;
import v9t9.tools.forthcomp.words.IPrimitiveWord;
import v9t9.tools.forthcomp.words.TargetColonWord;
import v9t9.tools.forthcomp.words.TargetUserVariable;
import v9t9.tools.forthcomp.words.TargetWord;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;
import ejs.base.utils.TextUtils;

/**
 * We use a direct-threaded model.  Execution branches to code using BL.
 * Primitive code returns using B *R11.  Colon words use IP (R14) to store the
 * pointer to the next XT.
 * 
 * @author ejs
 *
 */
public class TI99TargetContext extends TargetContext  {
	private final static boolean relBranches = true;
	private final static boolean inlineNext = true;
	
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

//	private LLImmedOperand threadOp;
	
//	private IPrimitiveWord doCol;
	private IPrimitiveWord doCon;
	private IPrimitiveWord doDcon;
	private IPrimitiveWord doUser;
	private IPrimitiveWord doRomDefer;
	
	private final LLOperand TOS = reg(REG_TOS);
	private final LLOperand TMP = reg(REG_TMP);
	private final LLOperand R2 = reg(REG_R2);
	private final LLOperand R3 = reg(REG_R3);

	private IPrimitiveWord doVar;
	private int interpLoop;
	private IPrimitiveWord doDoes;

	private int romDeferOffsetAddr;

	public TI99TargetContext(int memorySize) {
		super(false, 8, 16, memorySize);
		
		asmInstrFactory = new AsmInstructionFactory9900();
		instrFactory = new InstructionFactory9900();
		rawInstructionFactory = new RawInstructionFactory9900();
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
				LLOperand op = (LLOperand) opobj;
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
	 * @param obj
	 * @return
	 */
	private Collection<LLInstruction> createInstructions(
			StockInstruction obj) {
		Collection<LLInstruction> llInsts = new ArrayList<LLInstruction>(2);
		switch ((StockInstruction) obj) {
		case NEXT:
			if (inlineNext) {
				// get the next XT
				llInsts.add(createInstruction(Imov, regInc(REG_IP), TMP));
				// B to it
				llInsts.add(createInstruction(Ib, regInd(REG_TMP)));
			} else {
				if (interpLoop > 0) {
					llInsts.add(createInstruction(Ijmp, immed(interpLoop)));
				}
			}
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

//	private IPrimitiveWord defineConstantPrim(String name, final int value) throws AbortException {
//		IWord word = define(name, new TI99PrimitiveWord(defineEntry(name), true));
//		LLInstruction[] llInsts;
//		if (value == 0) {
//			llInsts = defineInstrs(
//				StockInstruction.PUSH_TOS,
//				Iclr, TOS
//				);
//		} else if (value == -1) {
//			llInsts = defineInstrs(
//					StockInstruction.PUSH_TOS,
//					Iseto, TOS
//					);
//		} else {
//			llInsts = defineInstrs(
//					StockInstruction.PUSH_TOS,
//					Ili, TOS, immed(value)
//					);
//		}
//		((TI99PrimitiveWord) word).setInsts(llInsts);
//		word.setExecutionSemantics(new ISemantics() {
//			
//			@Override
//			public void execute(HostContext hostContext, TargetContext targetContext)
//					throws AbortException {
//				hostContext.pushData(value);
//			}
//		});
//		return layoutPrimitiveWord(llInsts, word);
//	}

	private IPrimitiveWord definePrim(String name, Object... instsAndOpcodes) throws AbortException {
		IWord word = define(name, new TI99PrimitiveWord(defineEntry(name), true));
		LLInstruction[] llInsts = defineInstrs(instsAndOpcodes);
		((TI99PrimitiveWord) word).setInsts(llInsts);
		return layoutPrimitiveWord(llInsts, word);
	}

	private IPrimitiveWord defineInlinePrim(String name, Object... instsAndOpcodes) throws AbortException  {
		IWord word = define(name, new TI99PrimitiveWord(defineEntry(name), true));
		LLInstruction[] llInsts = defineInstrs(instsAndOpcodes);
		((TI99PrimitiveWord) word).setInsts(llInsts);
		return layoutPrimitiveWord(llInsts, word);
	}
	private IWord defineAlias(String name, String other) throws AbortException  {
		ITargetWord word = require(other);
		defineEntry(name);
		writeInstruction(Ijmp, immed(word.getEntry().getContentAddr()));
		define(name, word);
		return word;
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

	private LLOperand addr(int value) {
		return new LLAddrOperand(null, value);
	}

	private LLOperand addr(ITargetWord word) {
		return new LLAddrOperand(null, word.getEntry().getContentAddr());
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

//	private void popTOS() {
//		writeInstruction(Imov, regInc(REG_SP), TOS);
//	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.TargetContext#defineColonPrims()
	 */
	@Override
	public void defineColonPrims() throws AbortException {
		super.defineColonPrims();
		

		IPrimitiveWord docolPrim = definePrim("DOCOL",
				// save off the IP
				Idect, reg(REG_RP),
				Imov, reg(REG_IP), regInd(REG_RP),
				// get the new one from the XTs following the caller 
				Imov, reg(11), reg(REG_IP));
		
		if (!inlineNext) {
			interpLoop = getDP();
			defineSymbol(interpLoop, "@NEXT");

			// debug
			writeInstruction(Imov, reg(REG_SP), reg(REG_SP));
			
			// get the next XT
			writeInstruction(Imov, regInc(REG_IP), TMP);
			// B to it (everyone jumps back to interpLoop)
			writeInstruction(Ib, regInd(REG_TMP));
		}
		
		definePrim(";S",
				Imov, regInc(REG_RP), reg(REG_IP)
				);

		definePrim("DOLIT",
				StockInstruction.PUSH_TOS,
				
				Imov, regInc(REG_IP), TOS	// get word
				);

		definePrim("DODLIT",
				Iai, reg(REG_SP), immed(-cellSize * 2),
				Imov, TOS, regOffs(REG_SP, cellSize),
				
				Imov, regInc(REG_IP), regInd(REG_SP),	// get first word
				Imov, regInc(REG_IP), TOS		// and second word
				);

		///////// CREATE'd words use R11 for their data
		
		doVar = definePrim("DOVAR",
				StockInstruction.PUSH_TOS,
				Imov, reg(11), TOS	 	// addr
				);
		
		doCon = definePrim("DOCON",
				StockInstruction.PUSH_TOS,
				
				Imov, regInc(11), TOS
				);
		doDcon = definePrim("DODCON",
				Iai, reg(REG_SP), immed(-cellSize * 2),
				Imov, TOS, regOffs(REG_SP, cellSize),
				
				Imov, regInc(11), regInd(REG_SP),	// get first word
				Imov, regInc(11), TOS		// and second word
				);
				
		doUser = definePrim("DOUSER",
				StockInstruction.PUSH_TOS,
				
				Imov, regInc(11), TOS,	// offset follows
				Ia, reg(REG_UP), TOS	// add user base

				);

		// ROM deferral: points to an offset inside ROMDEFERS
		doRomDefer = definePrim("DORDEFER",
				StockInstruction.PUSH_TOS,
				
				Imov, regInc(11), R2,	// offset follows
				Imov, regOffs(REG_R2, -1), R2,
				
				StockInstruction.POP_TOS,
				Ib, regInd(REG_R2)
				);
		
		romDeferOffsetAddr = doRomDefer.getEntry().getContentAddr() + 4 + 2 + 2;
		addRelocation(romDeferOffsetAddr, RelocType.RELOC_CONSTANT, 
				((ITargetWord)romDeferTableWord).getEntry().getContentAddr());

		definePrim("(TO)",
				Imov, regOffs(REG_TOS, getCellSize()), TMP,
				Ici, TMP, immed(doRomDefer.getEntry().getContentAddr()),
				
			"0", 
				Ijne, ">0",	// error
				
				// RDEFER
				Imov, addr(romDeferOffsetAddr), TMP,	// addr of table
				Ia, regOffs(REG_TOS, getCellSize() * 2), TMP,
				Imov, regInc(REG_SP), regInd(REG_TMP),
				
				StockInstruction.POP_TOS
				);
				
		doDoes = definePrim("DODOES",
				StockInstruction.PUSH_TOS,

				Imov, regInc(11), TMP,			// XT of DOES> target
				Imov, reg(11), TOS,				// PFA

				Ib, regInd(REG_TMP)

				);

		/////////////////
		
		definePrim("(RESET)",
				Ilimi, immed(1),
				
//				Ili, reg(12), immed(0xc0),
//				Isbz, immed(0),	// Interrupt mode
//				Isbo, immed(1), // Enable external interrupts
//				Isbo, immed(2), // Enable VDP interrupts
//				Isbz, immed(3), // Disable clock interrupts
				

//			    $1      constant    M_INT_BKPT
//			    $2      constant    M_INT_EXT
//			    $4      constant    M_INT_VDP
//			    $8      constant    M_INT_KBD
//			    
				
				Ili, TMP, immed(0xF00),	// external + VDP
				Imovb, TMP, addr(Forth9900ConsoleMmioArea.CRU_INTS),
				Ili, TMP, immed(0x8120),	// turn on VDP interrupts (write R1_INT to VDP R1)
				Imov, TMP, addr(Forth9900ConsoleMmioArea.VDPWA),
				
				Ili, reg(REG_DOCOL), immed(docolPrim.getEntry().getContentAddr()),
				Ili, reg(REG_DOUSER), immed(doUser.getEntry().getContentAddr()),
				Ili, reg(REG_DOCON), immed(doCon.getEntry().getContentAddr()),
				Ili, reg(REG_DOVAR), immed(doVar.getEntry().getContentAddr()),
				Ili, reg(REG_DODOES), immed(doDoes.getEntry().getContentAddr()),
				// FFC0 is StdWP, FFE0 is IntWP; make tiny stacks before that inside FaultStacks
				Ili, reg(REG_SP), immed(0xffc0),
				Ili, reg(REG_RP), immed(0xffb0),
				Ili, reg(REG_UP), immed(0xffa0),
				Ili, reg(11), immed(Forth9900ConsoleMmioArea.COLD),
				Ib, regInd(REG_DOCOL)	// normally BL, but we set up R11 for this
				);
		
		
		definePrim("(REGS)", // ( SP0 UP0 RP0 -- )
				Imov, regInd(REG_RP), TMP,		// return addr
				Imov, TOS, reg(REG_RP),
				Imov, regInc(REG_SP), reg(REG_UP),
				Imov, regInc(REG_SP), reg(REG_SP),
				Idect, reg(REG_RP),
				Imov, TMP, regInd(REG_RP));
		
		definePrim("0BRANCH",
				// get target
				Imov, regInc(REG_IP), TMP,
				// test val 
				Imov, TOS, TOS,
				Ijeq, ">0",
				// not 0
				Imov, regInc(REG_SP), TOS,
				
				StockInstruction.NEXT,
			"0",
				Imov, regInc(REG_SP), TOS,
				relBranches ? Ia : Imov, TMP, reg(REG_IP)
				);
				
		definePrim("BRANCH", 
				relBranches ? Ia : Imov, regInc(REG_IP), reg(REG_IP)
				);
		
		definePrim("(S\")",
				//rdrop r@ dup 1+ swap c@ dup r> + 1+  >r
				Iai, reg(REG_SP), immed(-cellSize * 2),
				Imov, TOS, regOffs(REG_SP, cellSize),
				// get addr
				Imov, reg(REG_IP), TMP,
				// get length
				Imovb, regInc(REG_TMP), TOS,
				Isrl, TOS, immed(8),
				// save caddr
				Imov, TMP, regInd(REG_SP),
				// align
				Ia, TOS, TMP,
				Iinc, TMP,
				Iandi, TMP, immed(-2),
				
				Imov, TMP, reg(REG_IP)
				);
				
				
		definePrim("HANG", 
				Iidle
				);
		definePrim("(EMITCHAR)", 
				InstTableCommon.Iemitchar, immed(REG_TOS),
				StockInstruction.POP_TOS
				);
		definePrim("~SUCCESS~", 
				"0", Ijmp, ">0"
					);
		definePrim("~FAILURE~", 
				"0", Ijmp, ">0"
					);

	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineBuiltins()
	 */
	@Override
	public void definePrims() throws AbortException {
		super.definePrims();
		
		//doCol = docolPrim; // rest of words act normally 
		
//		defineConstantPrim("0", 0);
//		defineConstantPrim("1", 1);
//		defineConstantPrim("2", 2);
//		defineConstantPrim("-1", -1);
		
//		definePrim("@", 
//				Imov, regInd(REG_TOS), TOS);
//		definePrim("C@", 
//				Imovb, regInd(REG_TOS), TOS,
//				Isrl, TOS, immed(8)
//				);
//		definePrim("!", 
//				Imov, regInc(REG_SP), regInd(REG_TOS),
//				StockInstruction.POP_TOS
//				);
//		definePrim("C!", 
//				Imovb, regOffs(REG_SP, 1), regInd(REG_TOS),
//				Iinct, reg(REG_SP),
//				StockInstruction.POP_TOS
//				);
				
//		definePrim("d@", Iload_d);
//		definePrim("d!", Istore_d);
//		definePrim("+!", 
//				Ia, regInc(REG_SP), regInd(REG_TOS),
//				StockInstruction.POP_TOS
//				);
//				
//		definePrim("C+!", 
//				Iab, regOffs(REG_SP, 1), regInd(REG_TOS),
//				Iinct, reg(REG_SP),
//				StockInstruction.POP_TOS
//				);

//		definePrim("d+!", IplusStore_d);
//		
//		definePrim("1+",
//				Iinc, TOS
//				);
//		definePrim("2+",
//				Iinct, TOS
//				);
//		definePrim("1-",
//				Idec, TOS
//				);
//		definePrim("2-",
//				Idect, TOS
//				);
		
//		definePrim("DUP", 
//				StockInstruction.PUSH_TOS
//				);
//		
//		definePrim("DROP", 
//				StockInstruction.POP_TOS
//				);
//		definePrim("2DROP",
//				Iinct, reg(REG_SP),
//				StockInstruction.POP_TOS
//				);

//		definePrim("SWAP", 
//				Imov, TOS, TMP,
//				Imov, regInd(REG_SP), TOS,
//				Imov, TMP, regInd(REG_SP)
//				);
				
//		definePrim("2SWAP", Iswap_d);
//		definePrim("OVER", 
//				StockInstruction.PUSH_TOS,
//				Imov, regOffs(REG_SP, cellSize), TOS
//				);
//		
////		definePrim("2OVER", Iover_d);
//		definePrim("ROT", // ( a b c -- b c a )
//				Imov, regOffs(REG_SP, cellSize), TMP, // a
//				Imov, regInd(REG_SP), regOffs(REG_SP, cellSize),	// b
//				Imov, TOS, regInd(REG_SP),	// c
//				Imov, TMP, TOS	// a
//				);
////		definePrim("2ROT", Irot_d);
//		
//		definePrim("PICK",		// ( xu ... x1 x0 u -- xu ... x1 x0 xu )
//				// Remove u. Copy the xu to the top of the stack. An ambiguous condition exists if there are 
//				// less than u+2 items on the stack before PICK is executed. 
//				Ia, TOS, TOS,
//				Ia, reg(REG_SP), TOS,
//				Imov, regInd(REG_TOS), TOS
//				);
//		definePrim("RPICK",		// ( u -- xu ) ( R: xu ... x1 x0 -- xu ... x1 x0 )
//				// Remove u. Copy the xu to the top of the stack. An ambiguous condition exists if there are 
//				// less than u+2 items on the stack before PICK is executed. 
//				Ia, TOS, TOS,
//				Ia, reg(REG_RP), TOS,
//				Imov, regInd(REG_TOS), TOS
//				);
//				

//		
//		definePrim("0=", 
//				Imov, TOS, TOS,
//				Iseto, TOS,
//				Ijeq, ">0",
//				Iclr, TOS,
//			"0"
//				);
//
////		definePrim("D0=", I0equ_d);
//		defineInlinePrim("=", 
//				Ic, regInc(REG_SP), TOS,
//				Iclr, TOS,
//				Ijne, ">0",
//				Iseto, TOS,
//			"0"
//				);
//
//		
//		definePrim("D=",
//				Imov, TOS, TMP,
//				Iseto, TOS,
//				Ic, regOffs(REG_SP, cellSize*2), regInd(REG_SP), // low word more likely to differ
//				Ijne, ">1",
//				Ic, regOffs(REG_SP, cellSize), TMP,
//				Ijeq, ">2",
//			"1",
//				Iclr, TOS,
//			"2",
//				Iai, reg(REG_SP), immed(cellSize * 3)
//				);

//		definePrim("NEGATE", 
//				Ineg, TOS
//				);
//		
//		definePrim("+", 
//				Ia, regInc(REG_SP), TOS
//				);
//		definePrim("D+", // ( lo2 hi2 lo1 hi1 -- lo hi )
//				Imov, TOS, R2,
//				Imov, regInc(REG_SP), TMP,
//				StockInstruction.POP_TOS,
//				Ia, R2, TOS,
//				Ia, TMP, regInd(REG_SP),
//				Ijnc, ">1",
//				Iinc, TOS,
//			"1"
//				);
//		definePrim("-", 
//				Is, TOS, regInd(REG_SP),
//				StockInstruction.POP_TOS
//				);
//		definePrim("D-", Isub_d);
//		
//		defineInlinePrim("*", 
//				Imov, regInc(REG_SP), R2,
//				Impy, TOS, R2,
//				Imov, R3, TOS
//				);
//		defineInlinePrim("U*", 
//				Imov, regInc(REG_SP), R2,
//				Impy, TOS, R2,
//				Imov, R3, TOS
//				);
//
//		/*
//		 * ( u1 u2 -- ud )
//		 * 
//		 * Multiply u1 by u2, giving the unsigned double-cell product ud. All
//		 * values and arithmetic are unsigned.
//		 */
//		defineInlinePrim("UM*", 
//				Imov, regInd(REG_SP), R2,
//				Impy, TOS, R2,
//				Imov, R2, TOS,
//				Imov, R3, regInd(REG_SP)
//				);
//
//		/*
//		 * ( ud u1 -- u2 u3 )
//		 * 
//		 * Divide ud by u1, giving the quotient u3 and the remainder u2. All
//		 * values and arithmetic are unsigned. An ambiguous condition exists if
//		 * u1 is zero or if the quotient lies outside the range of a single-cell
//		 * unsigned integer.
//		 */
//		defineInlinePrim("UM/MOD", 
//				Imov, regInc(REG_SP), R2,
//				Imov, regInd(REG_SP), R3,
//				Idiv, TOS, R2,
//				Imov, R2, TOS,
//				Imov, R3, regInd(REG_SP)
//				);
		
//		
//		defineInlinePrim("NIP", Iswap, Idrop);
//		
//		definePrim("INVERT", 
//				Iinv, TOS
//				);
////		definePrim("DINVERT", Iinv_d);
////		definePrim("NOT", Inot);
////		definePrim("DNOT", Inot_d);
//		definePrim("OR", 
//				Isoc, regInc(REG_SP), TOS
//				);
//		
////		definePrim("DOR", Ior_d);
//		definePrim("AND",
//				Iinv, TOS,
//				Iszc, TOS, regInd(REG_SP),
//				StockInstruction.POP_TOS
//				);
////		definePrim("DAND", Iand_d);
//		definePrim("XOR",
//				Ixor, regInc(REG_SP), TOS
//				);
////		definePrim("DXOR", Ixor_d);
//		definePrim("NAND", 
//				Iszc, regInc(REG_SP), TOS
//				);

//		definePrim("DNAND", Inand_d);
//		
		
//		definePrim("DNEGATE",
//				Iinv, TOS,
//				Iinv, regInd(REG_SP),
//				Iinc, regInd(REG_SP),
//				Ijnc, ">0",
//				Iinc, TOS,
//			"0"
//				);
//		
//		int dnegateEntry = require("DNEGATE").getEntry().getContentAddr();
//		definePrim("DABS",
//				Imov, TOS, TOS,
//				Ijlt, immed(dnegateEntry)
//				);
//		
//		definePrim(">R",
//				Idect, reg(REG_RP),
//				Imov, TOS, regInd(REG_RP),
//				StockInstruction.POP_TOS
//				);
//				
//		definePrim("2>R", 
//				Iai, reg(REG_RP), immed(-cellSize * 2),
//				Imov, regInc(REG_SP), regOffs(REG_RP, cellSize),
//				Imov, TOS, regOffs(REG_RP, 0),
//				StockInstruction.POP_TOS
//				);
//		definePrim("R>",
//				StockInstruction.PUSH_TOS,
//				Imov, regInc(REG_RP), reg(REG_TOS)
//				);
//				
//		definePrim("2R>", 
//				Iai, reg(REG_SP), immed(-cellSize * 2),
//				Imov, TOS, regOffs(REG_SP, cellSize),
//
//				Imov, regInc(REG_RP), regOffs(REG_SP, 0),
//				Imov, regInc(REG_RP), TOS
//				);
//		
//		definePrim("RDROP",
//				Iinct, reg(REG_RP)
//				);
//		
//		definePrim("2RDROP",
//				Iai, reg(REG_RP), immed(cellSize * 2)
//				);
//		
//		definePrim("R@", 
//				StockInstruction.PUSH_TOS,
//				Imov, regInd(REG_RP), TOS
//				);
//		
//		definePrim("2R@", 
//				Iai, reg(REG_SP), immed(-cellSize * 2),
//				Imov, TOS, regOffs(REG_SP, cellSize),
//
//				Imov, regOffs(REG_RP, 0), TOS,
//				Imov, regOffs(REG_RP, cellSize), regInd(REG_SP)
//				);
//
//		defineAlias("I", "R@");
//		defineInlinePrim("I'", Irpidx, 1);
//		defineInlinePrim("J",
//				StockInstruction.PUSH_TOS,
//				Imov, regOffs(REG_RP, cellSize * 2), TOS
//				);
				
//		defineInlinePrim("J'", Irpidx, 3);
//		defineInlinePrim("K", Irpidx, 4);
//		defineInlinePrim("K'", Irpidx, 5);
//		
//		defineInlinePrim("SP@",
//				StockInstruction.PUSH_TOS,
//				Imov, reg(REG_SP), TOS
//				);
//		defineInlinePrim("SP!",
//				Imov, regInc(REG_SP), reg(REG_SP)
//				);
//		defineInlinePrim("RP@",
//				StockInstruction.PUSH_TOS,
//				Imov, reg(REG_RP), TOS
//				);
//		defineInlinePrim("RP!",
//				Imov, regInc(REG_SP), reg(REG_RP)
//				);
//		defineInlinePrim("LP@", IcontextFrom, CTX_LP);
//		defineInlinePrim("LP!", ItoContext, CTX_LP);
//		
		 
//		definePrim("(LOOP)", // ( R: lim next -- lim next+1 ) ( S: -- ) + jump
//				Imov, regInc(REG_IP), TMP,
//				
//				/*
//				 * Add one to the loop index. If the loop index is then equal to
//				 * the loop limit, discard the loop parameters and continue
//				 * execution immediately following the loop. Otherwise continue
//				 * execution at the beginning of the loop.
//				 */
//				Iinc, regInd(REG_RP), // next
//				Ic, regInd(REG_RP), regOffs(REG_RP, cellSize),
//				Ijeq, ">1",
//				
//				Ia, TMP, reg(REG_IP),
//			"1"
//				);
//
//		definePrim("(+LOOP)", // ( R: lim cur -- lim next ) ( S: change -- ) + jump
//				/*
//				 * Add n to the loop index. If the loop index did not cross the
//				 * boundary between the loop limit minus one and the loop limit,
//				 * continue execution at the beginning of the loop. Otherwise,
//				 * discard the current loop control parameters and continue
//				 * execution immediately following the loop.
//				 */
//				
//				Imov, regInc(REG_IP), TMP,				// jump
//				Ia, TMP, reg(REG_IP),
//
//				Imov, regOffs(REG_RP, cellSize), R2,	// lim
//				Ijne, ">nonzero",
//
//				Imov, TOS, TOS,							// forward?
//				Ijlt, ">nonzero",
//				
//				// zero: handle via carry
//				Ia, TOS, regInd(REG_RP),
//				Ijnc, ">exit",
//
//				//Iseto, TOS,
//				Is, TMP, reg(REG_IP),
//
//				Ijmp, ">exit",						// done
//				
//			"nonzero",
//				Ia, TOS, regInd(REG_RP),
//				
//				Imov, TOS, TOS,							// forward?
//				//Iclr, TOS,
//				Ijlt, ">neg",
//				
//				// lim < cur
//				Ic, regInd(REG_RP), R2,					// next ? lim
//				
//				Ijl, ">exit",
//				
//				Is, TMP, reg(REG_IP),
//
//				Ijmp, ">exit",						// done
//				
//			"neg",
//				Ic, regInd(REG_RP), R2,					// next ? lim
//				
//				Ijgt, ">exit",
//				
//				Is, TMP, reg(REG_IP),
//				
//			"exit",
//				StockInstruction.POP_TOS
//				);

		DictEntry qdoEntry = defineEntry("(?DO)");
		TargetWord qdo = new TargetWord(qdoEntry) {
			{
				setCompilationSemantics(new ISemantics() {
					
					@Override
					public void execute(HostContext hostContext, TargetContext targetContext)
							throws AbortException {
						targetContext.parse("2DUP");
						targetContext.parse("2>R");
						targetContext.parse("-");
						targetContext.buildCall(require("0BRANCH"));
						
						// TODO: this won't work when host-executing
					}
				});
			}
			
		};
		define("(?DO)", qdo);
		
		definePrim("2DUP",
				Imov, TOS, regOffs(REG_SP, -cellSize),
				Imov, regInd(REG_SP), regOffs(REG_SP, -cellSize * 2),
				Iai, reg(REG_SP), immed(-cellSize * 2)
				);
				
		
		definePrim("UNLOOP", 
				Iai, reg(REG_RP), immed(cellSize * 2)	
				//Ic, regInc(REG_RP), regInc(REG_RP)		// space saving, but 2x slower!
				);
				
		definePrim("?DUP", 
				Imov, TOS, TOS,
				Ijeq, ">0",
				StockInstruction.PUSH_TOS,
			"0"
				);
		
		definePrim("0<", 
				Imov, TOS, TOS,
				Iseto, TOS,
				Ijlt, ">0",
				Iclr, TOS,
			"0"
				);
		definePrim("0>", 
				Imov, TOS, TOS,
				Iseto, TOS,
				Ijgt, ">0",
				Iclr, TOS,
			"0"
				);
				
		definePrim("<=", 
				Ic, regInc(REG_SP), TOS,
				Iclr, TOS,
				Ijgt, ">0",
				Iseto, TOS,
			"0"
				);
		definePrim("U<", 
				Ic, regInc(REG_SP), TOS,
				Iseto, TOS,
				Ijl, ">0",
				Iclr, TOS,
			"0"
				);
		definePrim("<", 
				Ic, regInc(REG_SP), TOS,
				Iseto, TOS,
				Ijlt, ">0",
				Iclr, TOS,
			"0"
				);
		definePrim("U>", 
				Ic, regInc(REG_SP), TOS,
				Iseto, TOS,
				Ijh, ">0",
				Iclr, TOS,
			"0"
				);
		definePrim(">", 
				Ic, regInc(REG_SP), TOS,
				Iseto, TOS,
				Ijgt, ">0",
				Iclr, TOS,
			"0"
				);
		
		defineInlinePrim("2/",
				Isra, TOS, immed(1)
				);
		defineInlinePrim("2*", 
				Isla, TOS, immed(1)
				);
//		
		definePrim("LSHIFT", 
				Imov, TOS, reg(0),
				StockInstruction.POP_TOS,
				Isla, TOS, immed(0)
				);
		definePrim("RSHIFT", 
				Imov, TOS, reg(0),
				StockInstruction.POP_TOS,
				Isra, TOS, immed(0)
				);
		definePrim("URSHIFT", 
				Imov, TOS, reg(0),
				StockInstruction.POP_TOS,
				Isrl, TOS, immed(0)
				);
		definePrim("CRSHIFT", 
				Imov, TOS, reg(0),
				StockInstruction.POP_TOS,
				Isrc, TOS, immed(0)
				);
		defineInlinePrim("SWPB", 
				Iswpb, TOS
				);

		definePrim("EXECUTE",
				//Idect, reg(REG_RP),
				//Imov, reg(REG_IP), regInd(REG_RP),
				Imov, TOS, TMP,
				StockInstruction.POP_TOS,
				Ibl, regInd(REG_TMP)
				//Imov, regInc(REG_RP), reg(REG_IP)
				);

		defineInlinePrim("TRUE", 
				StockInstruction.PUSH_TOS,
				Iseto, TOS
				);
		defineInlinePrim("FALSE", 
				StockInstruction.PUSH_TOS,
				Iclr, TOS
				);
		
		defineInlinePrim("INTS-CHECK", 
				Ilimi, immed(1),
				Ilimi, immed(0)
				);
		
		// neat, but not enough uses to justify (one cell longer than FORTH)
//		definePrim("WITHIN",   // ( test low high -- flag )
//
//				Imov, regInc(REG_SP), R3,
//				Is, R3, TOS, 	// range: high - low
//				Imov, regInc(REG_SP), TMP, 	// test
//				Is, R3, TMP,
//				Ic, TMP, TOS,
//				
//				Iseto, TOS,		// assume in range
//				Ijl, ">1",
//				
//				Iclr, TOS,		// oops, not
//				
//			"1"
//				);
		
		

//		/** Move memory backward (src -> dst)
//		( src dst # -- )
//		*/  
//		definePrim("CMOVE", 
//				Imov, TOS, R2,		// # bytes
//				Imov, regInd(REG_SP), TMP,	// dst
//				StockInstruction.POP2_TOS,	// src = TOS
//				Imov, R2, R2,			// 0 bytes?
//				Ijeq, ">1",
//			"2",
//				Imovb, regInc(REG_TOS), regInc(REG_TMP),
//				Idec, R2,
//				Ijne, ">2",
//			"1",
//				StockInstruction.POP_TOS
//				);
//		/** Move memory forward (dst -> src)
//		( src dst # -- )
//		*/  
//		definePrim("CMOVE>", 
//				Imov, TOS, R2,		// # bytes
//				Imov, regInd(REG_SP), TMP,	// dst
//				StockInstruction.POP2_TOS,	// src = TOS
//				Imov, R2, R2,			// 0 bytes?
//				Ijeq, ">1",
//				Ia, R2, TOS,
//				Ia, R2, TMP,
//			"2",
//				Idec, TOS,
//				Idec, TMP,
//				Imovb, regInd(REG_TOS), regInd(REG_TMP),
//				Idec, R2,
//				Ijne, ">2",
//			"1",
//				StockInstruction.POP_TOS
//				);


		/** Move memory from one area to another
		 * ( src target len srcincr targetincr  )
		*/  
		definePrim("(CMOVE)",
											// TOS: targetincr
				Imov, regInc(REG_SP), reg(11),	// R11: srcincr
				Imov, regInc(REG_SP), R2,	// R2: len
				Imov, regInc(REG_SP), R3,	// R3: target
				Imov, regInc(REG_SP), TMP,	// TMP: source
				
				Imov, TOS, TOS, // TOS: targetincr
				Ijgt, ">t+",
				
				Ia, R2, R3,
				Idec, R3,		// TODO: assuming TOS == -1
				
			"t+",
			
				Imov, reg(11), reg(11), 
				Ijgt, ">f+",
				
				Ia, R2, TMP,
				Idec, TMP,		// TODO: assuming TOS == -1
				
			"f+",
				
				Imov, R2, R2,			// 0 bytes?
				Ijeq, ">1",
			"2",
				Imovb, regInd(REG_TMP), regInd(REG_R3),
				Ia, TOS, R3,
				Ia, reg(11), TMP,
				Idec, R2,
				Ijne, ">2",
			"1",
				
				StockInstruction.POP_TOS
				);

		/** Fill memory
		 * ( addr n ch targetincr )
		*/  
		definePrim("(CFILL)",
											// TOS: targetincr
				Imov, regInc(REG_SP), TMP,	// TMP: ch
				Iswpb, TMP,

				Imov, regInc(REG_SP), R2,	// R2: len
				Imov, regInc(REG_SP), R3,	// R3: target
				
				Imov, R2, R2,			// 0 bytes?
				Ijeq, ">1",
			"2",
				Imovb, TMP, regInd(REG_R3),
				Ia, TOS, R3,
				Idec, R2,
				Ijne, ">2",
			"1",
			
				StockInstruction.POP_TOS
				);

		
		LLOperand lengthAndFlags = reg(12);  // should be saved
		LLOperand charTemp = reg(4);	// must be saved
		int REG_charPtr = 11;
		LLOperand charPtr = reg(REG_charPtr);	// not saved
		LLOperand counter = reg(5);	// must be saved

		definePrim("(lfind)",
		        // find word in dictionary        ( c-addr lfa -- c-addr 0 | xt 1==immed | xt -1 )     
		        // lfa is nfa - #cell

				// save clobbered regs
				Iai, reg(REG_RP), immed(-cellSize * 3),
				Imov, charTemp, regOffs(REG_RP, cellSize * 2),
				Imov, counter, regOffs(REG_RP, cellSize),
				Imov, lengthAndFlags, regOffs(REG_RP, 0),
				
				Imov, TOS, TOS, 	// TOS=LFA
				Ijeq, ">failed",			// fail

				// search list 

				Iclr, reg(REG_R3),
				Iclr, charTemp,
		        
		        Imov, regInd(REG_SP), charPtr,
		        
		        Imovb, regInd(REG_charPtr), TMP, // [ expected byte | length ]
		        Imovb, TMP, lengthAndFlags, 
		        Iswpb, lengthAndFlags,
		        Imovb, TMP, lengthAndFlags, 
		        Iori, lengthAndFlags, immed(0x8000),		// not smudged
		        
		        Ijmp, ">wordStart",

		"wordLoop",
		    	Imov, regInd(REG_TOS), TOS, 	// get LFA to new one...
		    	Ijeq, ">failed",                // if end... (unlikely)

		"wordStart",
				// TOS = LFA, followed by [len] "name"
		
				// check length & flags
				Imovb, regOffs(REG_TOS, 2), reg(REG_R3),  
				Iandi, reg(REG_R3), immed(~0x4000), 	// keep all but immediate bit 
		        Isb, lengthAndFlags, reg(REG_R3),				// subtract both smudge & length -- should be 0x00 or 0x40
				Ijne, ">wordLoop",
				
		        Imov, TOS, TMP,                    // new NFA to check
        		Iai, TMP, immed(3),
        		
		        Imov, charPtr, reg(REG_R2),
		        Iinc, reg(REG_R2),
		        Imov, lengthAndFlags, counter,
		        Iandi, counter, immed(0xFF),
		        
		   "charLoop",
		   		Imovb, regInc(REG_TMP), reg(REG_R3), 
		   		Imovb, regInc(REG_R2), charTemp,
		   		
		   		Icb, reg(REG_R3), charTemp,
		   		Ijeq, ">match",                     // exact match?

		   		// see if they might be letters that differ in case only
		   		Ixor, reg(REG_R3), charTemp,
		        Ici, charTemp, immed(0x2000),              // differ in case bit?
		        Ijne, ">wordLoop",
		        
		        // okay, exclude matches in punctuation 
		        Iszc, charTemp, reg(REG_R3),                            // turn off case in matching char
		        Iai, reg(REG_R3), immed(-0x4100),
		        Ici, reg(REG_R3), immed(0x5A00 - 0x4100),
		        Ijhe, ">wordLoop",
		        
		"match",
		        Idec, counter,
		        Ijgt, ">charLoop",

		        // convert to XT
		        Imovb, regOffs(REG_TOS, cellSize), lengthAndFlags,		// save NFA flag bits
		        Imov, TMP, TOS,
		        
		        // align
		        Iinc, TOS,
		        Iandi, TOS, immed(-cellSize),		
		        
		        Imov, TOS, regInd(REG_SP),  // overwrite caddr with xt
		        
		        // assume not immediate
		        Iseto, TOS,
		        
		        Iandi, lengthAndFlags, immed(0x4000),	// immediate?
		        Ijeq, ">reset",
		        
		        // immediate
		        Ineg, TOS,
		        
		        Ijmp, ">reset",
		    
		"failed",    
		    	Iclr, TOS,                 // failed
		    	
		"reset",
				// restore
				Imov, regInc(REG_RP), lengthAndFlags,
				Imov, regInc(REG_RP), counter,
				Imov, regInc(REG_RP), charTemp
		);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.TargetContext#defineCompilerWords(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	public void defineCompilerWords(HostContext hostContext) {
		super.defineCompilerWords(hostContext);
		
		hostContext.define("(int1-redirect)", 
			new BaseStdWord() {
				
				@Override
				public boolean isImmediate() {
					return false;
				}
				
				@Override
				public void execute(HostContext hostContext, TargetContext targetContext)
						throws AbortException {
					// turn off ints
					writeInstruction(Ilimi, immed(0));
					// switch to bank 1
					writeInstruction(Iseto, addr(4));
					// in normal ROM, jumps to interrupt
					writeInstruction(Ijmp, immed(targetContext.getDP()));		// not used
					// restore bank
					writeInstruction(Iseto, addr(0));
					// return from interrupt
					writeInstruction(Irtwp);
					// the shared ROM will have instructions here
				}
			}
		);
	}

	/**
	 * Call addresses must be aligned
	 */
	public void initWordEntry() {
		alignDP();
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
			writeInstruction(Ibl, addr(doDcon));
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
		writeInstruction(Ibl, addr(doRomDefer));
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
}
