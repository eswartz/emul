/*
  TI99TargetContext.java

  (c) 2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.ti99;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.sun.org.apache.bcel.internal.generic.SIPUSH;

import static v9t9.machine.ti99.cpu.Inst9900.*;
import v9t9.common.asm.ILabelDetector;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.ICpuState;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.machine.f99b.memory.EnhancedRamByteArea;
import v9t9.machine.ti99.asm.InstructionFactory9900;
import v9t9.machine.ti99.asm.RawInstructionFactory9900;
import v9t9.machine.ti99.cpu.Cpu9900;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.tools.asm.LLInstruction;
import v9t9.tools.asm.inst9900.AsmInstructionFactory9900;
import v9t9.tools.asm.inst9900.Assembler9900;
import v9t9.tools.asm.operand.hl.RegOffsOperand;
import v9t9.tools.asm.operand.ll.LLAddrOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOffsetOperand;
import v9t9.tools.asm.operand.ll.LLOperand;
import v9t9.tools.asm.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.operand.ll.LLRegisterOperand;
import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.GromDictEntry;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.IGromTargetContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;
import v9t9.tools.forthcomp.RelocEntry;
import v9t9.tools.forthcomp.RelocEntry.RelocType;
import v9t9.tools.forthcomp.f99b.words.ExitI;
import v9t9.tools.forthcomp.f99b.words.FieldComma;
import v9t9.tools.forthcomp.words.HostLiteral;
import v9t9.tools.forthcomp.words.INativeCodeWord;
import v9t9.tools.forthcomp.words.IPrimitiveWord;
import v9t9.tools.forthcomp.words.TargetColonWord;
import v9t9.tools.forthcomp.words.TargetContext;
import v9t9.tools.forthcomp.words.TargetInlineColonWord;
import v9t9.tools.forthcomp.words.TargetSQuote;
import v9t9.tools.forthcomp.words.TargetUserVariable;
import v9t9.tools.forthcomp.words.TargetValue;
import v9t9.tools.forthcomp.words.TargetWord;
import ejs.base.utils.HexUtils;
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

	public static final int REG_TOS = 1;
	public static final int REG_T1 = 2;
	public static final int REG_T2 = 3;
	
	public static final int REG_DOCOL = 4;
	public static final int REG_DOCON = 5;
	public static final int REG_DOUSER = 6;
	public static final int REG_DOVAR = 7;
	public static final int REG_DODOES = 8;
	
	public static final int REG_RTOS = 9;
	public static final int REG_UP = 10;
	// RT = 11
	// CRUBASE = 12
	public static final int REG_RP = 13;
	public static final int REG_IP = 14;
	public static final int REG_SP = 15;
	
	
	private enum StockInstruction {
		PUSH_TOS,
		PUSH_TOS_ALLOC_2,
		PUSH_RTOS,
		POP_RTOS,
		POP_2TOS,
		POP_TOS,
		
	}
	
	private AsmInstructionFactory9900 asmInstrFactory;
	private InstructionFactory9900 instrFactory;

//	private LLImmedOperand threadOp;
	
	private IPrimitiveWord doCol;
	private IPrimitiveWord doCon;
	private IPrimitiveWord doDcon;
	private IPrimitiveWord doUser;
	
	private final LLOperand TOS = reg(REG_TOS);
	private final LLOperand T1 = reg(REG_T1);
	private final LLOperand T2 = reg(REG_T2);
	private IPrimitiveWord doVar;
	private int interpLoop;
	private IPrimitiveWord doDoes;
	
	public TI99TargetContext(int memorySize) {
		super(false, 8, 16, memorySize);
		
		asmInstrFactory = new AsmInstructionFactory9900();
		instrFactory = new InstructionFactory9900();
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
				switch ((StockInstruction) obj) {
				case PUSH_TOS:
					llInsts.add(createInstruction(Idect, reg(REG_SP)));
					llInsts.add(createInstruction(Imov, TOS, regInd(REG_SP)));
					addr += 4;
					break;
	
				case POP_TOS:
					llInsts.add(createInstruction(Imov, regInc(REG_SP), TOS));
					addr += 2;
					break;
					
				case PUSH_TOS_ALLOC_2:
					llInsts.add(createInstruction(Iai, reg(REG_SP), immed(-4)));	// make room for two
					llInsts.add(createInstruction(Imov, TOS, regOffs(REG_SP, -2)));	// save TOS
					addr += 8;
					break;
					
				case PUSH_RTOS:
					llInsts.add(createInstruction(Idect, reg(REG_RP)));
					llInsts.add(createInstruction(Imov, TOS, regInd(REG_RP)));
					addr += 4;
					break;
					
				case POP_RTOS:
					llInsts.add(createInstruction(Imov, regInc(REG_RP), TOS));
					addr += 2;
					break;
					
				default:
					throw new UnsupportedOperationException(obj.toString());
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
	 * @param object
	 * @return
	 */
	private boolean isOperand(Object object) {
		return object instanceof LLOperand || 
				(object instanceof String && ((String) object).startsWith(">"));
	}

	private IPrimitiveWord definePrim(String string, Object... instsAndOpcodes) throws AbortException {
		LLInstruction[] llInsts = defineInstrs(instsAndOpcodes);
		IWord word = define(string, new TI99PrimitiveWord(defineEntry(string), llInsts, true));
		return layoutPrimitiveWord(llInsts, word);
	}

	private IPrimitiveWord defineInlinePrim(String string, Object... instsAndOpcodes) throws AbortException  {
		LLInstruction[] llInsts = defineInstrs(instsAndOpcodes);
		IWord word = define(string, new TI99PrimitiveWord(defineEntry(string), llInsts, true)); 
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
		if (doCol != null) {
			//writeInstruction(Ijmp, immed(interpLoop));
			writeInstruction(Ib, regInd(11));
		}
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

	private void popTOS() {
		writeInstruction(Imov, regInc(REG_SP), TOS);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineBuiltins()
	 */
	@Override
	public void defineBuiltins() throws AbortException {
		IPrimitiveWord docolPrim = definePrim("DOCOL",
				// save off the IP
				Idect, reg(REG_RP),
				Imov, reg(REG_IP), regInd(REG_RP),
				// get the new one from the XTs following the caller 
				Imov, reg(11), reg(REG_IP));
		
		interpLoop = getDP();
		
		// get the next XT
		writeInstruction(Imov, regInc(REG_IP), T1);
		// BL to it (for prims, they can RT; for others, they just jump back to interpLoop)
		writeInstruction(Ibl, regInd(REG_T1));
		
		writeInstruction(Ijmp, immed(interpLoop));
				
//		threadOp = new LLImmedOperand(loop);
		
		definePrim(";S",
				Imov, reg(REG_RTOS), reg(REG_IP),
				
				StockInstruction.POP_RTOS,
				
				Ijmp, immed(interpLoop)
				);

		definePrim("DOLIT",
				StockInstruction.PUSH_TOS,
				
				Imov, regInc(REG_IP), TOS,	// get word
				
				Ijmp, immed(interpLoop)
				);

		doVar = definePrim("DOVAR",
				StockInstruction.PUSH_TOS,
				Imov, reg(11), TOS,
				
				Ijmp, immed(interpLoop)
				);
		
		definePrim("DODLIT",
				StockInstruction.PUSH_TOS_ALLOC_2,
				
				Imov, regInc(11), regInd(REG_SP),	// get first word
				Imov, regInc(11), TOS,		// and second word

				Ijmp, immed(interpLoop)
				);
		
		doCon = definePrim("DOCON",
				StockInstruction.PUSH_TOS,
				
				Imov, regInc(11), TOS,	// get word
				
				Ijmp, immed(interpLoop)
				);
		doDcon = definePrim("DODCON",
				StockInstruction.PUSH_TOS_ALLOC_2,
				
				Imov, regInc(11), regInd(REG_SP),	// get first word
				Imov, regInc(11), TOS,		// and second word
				
				Ijmp, immed(interpLoop)
				);
				
		doUser = definePrim("DOUSER",
				StockInstruction.PUSH_TOS,
				
				Imov, regInc(11), TOS,	// offset follows
				Ia, reg(REG_UP), TOS,	// add user base
				
				Ijmp, immed(interpLoop)		

				);

		doDoes = definePrim("DODOES",
				StockInstruction.PUSH_TOS,

				Imov, reg(11), TOS,			// addr
				Iinct, reg(11),

				Ijmp, immed(interpLoop)		

				);

		definePrim("(RESET)",
				Ili, reg(REG_DOCOL), immed(docolPrim.getEntry().getContentAddr()),
				Ili, reg(REG_DOUSER), immed(doUser.getEntry().getContentAddr()),
				Ili, reg(REG_DOCON), immed(doCon.getEntry().getContentAddr()),
				Ili, reg(REG_DOVAR), immed(doVar.getEntry().getContentAddr()),
				Ili, reg(REG_DODOES), immed(doDoes.getEntry().getContentAddr()),
				// FFC0 is StdWP, FFE0 is IntWP; make tiny stacks before that inside FaultStacks
				Ili, reg(REG_SP), immed(0xffc0),
				Ili, reg(REG_RP), immed(0xffb0),
				Ili, reg(REG_UP), immed(0xffa0),
				Ili, reg(11), immed(0x400), 
				Ib, regInd(REG_DOCOL)	// normally BL, but we set up R11 for this
				);
		
		
		definePrim("(REGS)", // ( SP0 UP0 RP0 -- )
				Imov, regInd(REG_RP), T1,
				Imov, TOS, reg(REG_RP),
				Imov, regInc(REG_SP), reg(REG_UP),
				Imov, regInc(REG_SP), reg(REG_SP),
				Imov, T1, reg(REG_RTOS),
				Ib, regInd(11));
		

		doCol = docolPrim; // rest of words act normally 
		
		
		definePrim("@", 
				Imov, regInd(REG_TOS), TOS);
		definePrim("C@", 
				Imovb, regInd(REG_TOS), TOS,
				Isrl, TOS, immed(8)
				);
		definePrim("!", 
				Imov, regInc(REG_SP), regInd(REG_TOS),
				
				StockInstruction.POP_TOS
				);
		definePrim("C!", 
				Imov, regInc(REG_SP), T1,
				Iswpb, T1, 
				Imovb, T1, regInd(REG_TOS),
				
				StockInstruction.POP_TOS
				);
				
//		definePrim("d@", Iload_d);
//		definePrim("d!", Istore_d);
		definePrim("+!", 
				Ia, regInc(REG_SP), regInd(REG_TOS),
				StockInstruction.POP_TOS
				);
				
		definePrim("C+!", 
				Imov, regInc(REG_SP), T1,
				Iswpb, T1,
				Iab, T1, regInd(REG_TOS),
				
				StockInstruction.POP_TOS
				);

//		definePrim("d+!", IplusStore_d);
//		
		definePrim("1+",
				Iinc, TOS
				);
		definePrim("2+",
				Iinct, TOS
				);
		definePrim("1-",
				Idec, TOS
				);
		definePrim("2-",
				Idect, TOS
				);
		
		definePrim("DUP", 
				StockInstruction.PUSH_TOS
				);
		
		definePrim("DROP", 
				StockInstruction.POP_TOS
				);

		definePrim("SWAP", 
				Imov, TOS, T1,
				Imov, regInd(REG_SP), TOS,
				Imov, T1, regInd(REG_SP)
				);
				
//		definePrim("2SWAP", Iswap_d);
		definePrim("OVER", 
				StockInstruction.PUSH_TOS,
				Imov, regOffs(REG_SP, 2), TOS
				);
		
//		definePrim("2OVER", Iover_d);
		definePrim("ROT", // ( a b c -- b c a )
				Imov, regOffs(REG_SP, 2), T1, // a
				Imov, regInd(REG_SP), regOffs(REG_SP, 2),	// b
				Imov, TOS, regInd(REG_SP),	// c
				Imov, T1, TOS	// a
				);
//		definePrim("2ROT", Irot_d);
//		definePrim("0=", I0equ);
//		definePrim("D0=", I0equ_d);
//		definePrim("=", Iequ);
//		definePrim("D=", Iequ_d);
		definePrim("0BRANCH",
				// get offset
				Imov, regInc(11), T1,
				// test val 
				Imov, TOS, TOS,
				Ijeq, ">0",
				// not 0
				Imov, regInc(REG_SP), TOS,
				Ib, regInd(11),
			"0",
				Imov, regInc(REG_SP), TOS,
				Ia, T1, reg(REG_IP)
				);
				
		definePrim("BRANCH", 
				Ia, TOS, reg(REG_IP),
				StockInstruction.POP_TOS
				);
		
		definePrim("NEGATE", 
				Ineg, TOS
				);
		
//		definePrim("DNEGATE", Ineg_d);
		definePrim("+", 
				Ia, regInc(REG_SP), TOS
				);
//		definePrim("D+", Iadd_d);
		definePrim("-", 
				Is, TOS, regInd(REG_SP),
				StockInstruction.POP_TOS
				);
//		definePrim("D-", Isub_d);
//		definePrim("UM*", Iumul);
//		defineInlinePrim("UM/MOD", Iudivmod);
//		
//		defineInlinePrim("NIP", Iswap, Idrop);
//		
		definePrim("INVERT", 
				Iinv, TOS
				);
//		definePrim("DINVERT", Iinv_d);
//		definePrim("NOT", Inot);
//		definePrim("DNOT", Inot_d);
		definePrim("OR", 
				Isoc, regInc(REG_SP), TOS
				);
		
//		definePrim("DOR", Ior_d);
		definePrim("AND", 
				Iszc, regInc(REG_SP), TOS
				);
//		definePrim("DAND", Iand_d);
		definePrim("XOR",
				Ixor, regInc(REG_SP), TOS
				);
//		definePrim("DXOR", Ixor_d);
//		definePrim("NAND", Inand);
//		definePrim("DNAND", Inand_d);
//		
		definePrim(">R", 
				StockInstruction.PUSH_RTOS
				);
				
		definePrim("2>R", 
				Iai, reg(REG_RP), immed(-4),
				Imov, regInc(REG_SP), regOffs(REG_RP, 2),
				Imov, regInc(REG_TOS), regOffs(REG_RP, 0),
				StockInstruction.POP_TOS
				);
		definePrim("R>", 
				StockInstruction.POP_RTOS
				);
				
		definePrim("2R>", 
				Iai, reg(REG_SP), immed(-4),
				Imov, regInc(REG_RP), regOffs(REG_SP, 2),
				Imov, regInc(REG_RTOS), regOffs(REG_SP, 0),
				StockInstruction.POP_RTOS
				);
		
		definePrim("RDROP",
				StockInstruction.POP_RTOS
				);
		
		definePrim("R@", 
				StockInstruction.PUSH_TOS,
				Imov, reg(REG_RTOS), TOS
				);
		defineAlias("I", "R@");
//		defineInlinePrim("I'", Irpidx, 1);
		defineInlinePrim("J",
				StockInstruction.PUSH_TOS,
				Imov, regOffs(REG_RP, 2), TOS
				);
				
//		defineInlinePrim("J'", Irpidx, 3);
//		defineInlinePrim("K", Irpidx, 4);
//		defineInlinePrim("K'", Irpidx, 5);
//		
//		defineInlinePrim("SP@", IcontextFrom, CTX_SP);
//		defineInlinePrim("SP!", ItoContext, CTX_SP);
//		defineInlinePrim("RP@", IcontextFrom, CTX_RP);
//		defineInlinePrim("RP!", ItoContext, CTX_RP);
//		defineInlinePrim("LP@", IcontextFrom, CTX_LP);
//		defineInlinePrim("LP!", ItoContext, CTX_LP);
//		
		 
		defineAlias("(DO)", "2>R");		
		
		definePrim("(LOOP)", // ( R: lim next -- lim next+1 ) ( S: -- done? )
				StockInstruction.PUSH_TOS,
				Iclr, TOS,
				
				Iinc, regInd(REG_RP), // next
				Imov, regOffs(REG_RP, 2), T1,	// lim
				Ijne, ">0",
				
				// lim == 0 --> next > 0?
				Ic, regInd(REG_RP), regInd(REG_RP),
				Ijgt, ">1",
				Ijmp, ">2",
				
			"0",
				// lim != 0 --> next < lim
				Ic, regInd(REG_RP), T1,
				Ijlt, ">2",
			"1",
				Iseto, TOS,
			"2"
				);

		defineInlinePrim("(ULOOP)", 
				StockInstruction.PUSH_TOS,
				Iclr, TOS,
				
				Iinc, regInd(REG_RP), // next
				Imov, regOffs(REG_RP, 2), T1,	// lim
				Ijne, ">0",
				
				// lim == 0 --> next > 0?
				Ic, regInd(REG_RP), regInd(REG_RP),
				Ijh, ">1",
				Ijmp, ">2",
				
			"0",
				// lim != 0 --> next < lim
				Ic, regInd(REG_RP), T1,
				Ijl, ">2",
			"1",
				Iseto, TOS,
			"2"
				);
		
		definePrim("(+LOOP)", // ( R: lim next -- lim next+change ) ( S: change -- done? )
				Ia, TOS, regInd(REG_RP), // next

				Iclr, T1,	// flag
				
				Imov, regOffs(REG_RP, 2), T2,	// lim
				Ijne, ">nonzero",
				
				// lim == 0 --> (next >= change) ? 0 : -1
				Ic, regInd(REG_RP), TOS,
				Ijlt, ">1",
				Iseto, T1,
			"1",
				Imov, T1, TOS,
				Ib, regInd(11),
				
			"nonzero",
				// lim != 0 --> (change < 0 ? next > lim  : next < lim) ? 0 : -1

				// assume positive
				Ic, regInd(REG_RP), T2, 
				Ijlt, ">2",
				Iseto, T1,
				
			"2",
				// invert if negative
				Imov, TOS, TOS,
				Ijgt, ">3",
				Iinv, T1,
				
			"3",
				Imov, T1, TOS
				);

		definePrim("(U+LOOP)", // ( R: lim next -- lim next+change ) ( S: change -- done? )
				Ia, TOS, regInd(REG_RP), // next

				Iclr, T1,	// flag
				
				Imov, regOffs(REG_RP, 2), T2,	// lim
				Ijne, ">nonzero",
				
				// lim == 0 --> (next >= change) ? 0 : -1
				Ic, regInd(REG_RP), TOS,
				Ijl, ">1",
				Iseto, T1,
			"1",
				Imov, T1, TOS,
				Ib, regInd(11),
				
			"nonzero",
				// lim != 0 --> (change < 0 ? next > lim  : next < lim) ? 0 : -1

				// assume positive
				Ic, regInd(REG_RP), T2, 
				Ijl, ">2",
				Iseto, T1,
				
			"2",
				// invert if negative
				Imov, TOS, TOS,
				Ijgt, ">3",
				Iinv, T1,
				
			"3",
				Imov, T1, TOS
				);
				
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
				Iai, reg(REG_SP), immed(-4),
				Imov, TOS, regOffs(REG_SP, -2),
				Imov, regInd(REG_SP), regOffs(REG_SP, -4)
				);
				
		
		definePrim("UNLOOP", 
				Iai, reg(REG_RP), immed(4)	// note: RTOS is unchanged
				);
				
		definePrim("?DUP", 
				Imov, TOS, TOS,
				Ijeq, ">0",
				StockInstruction.PUSH_TOS,
			"0"
				);
		
//		//definePrim("(CONTEXT>)", IcontextFrom);
//		//definePrim("(>CONTEXT)", ItoContext);
//		//definePrim("(USER)", Iuser);
//
//		defineInlinePrim("0<", IlitX, Icmp+CMP_LT);
//		defineInlinePrim("0<=", IlitX, Icmp+CMP_LE);
//		defineInlinePrim("0>", IlitX, Icmp+CMP_GT);
//		defineInlinePrim("0>=", IlitX, Icmp+CMP_GE);
//		defineInlinePrim("0U<", IlitX, Icmp+CMP_ULT);
//		defineInlinePrim("0U<=", IlitX, Icmp+CMP_ULE);
//		defineInlinePrim("0U>", IlitX, Icmp+CMP_UGT);
//		defineInlinePrim("0U>=", IlitX, Icmp+CMP_UGE);
//		defineInlinePrim("<", Icmp+CMP_LT);
//		defineInlinePrim("<=", Icmp+CMP_LE);
//		defineInlinePrim(">", Icmp+CMP_GT);
//		defineInlinePrim(">=", Icmp+CMP_GE);
//		defineInlinePrim("U<", Icmp+CMP_ULT);
//		defineInlinePrim("U<=", Icmp+CMP_ULE);
//		defineInlinePrim("U>", Icmp+CMP_UGT);
//		defineInlinePrim("U>=", Icmp+CMP_UGE);
//
//		defineInlinePrim("D0<", IlitX_d, Icmp+CMP_LT);
//		defineInlinePrim("D0<=", IlitX_d, Icmp+CMP_LE);
//		defineInlinePrim("D0>", IlitX_d, Icmp+CMP_GT);
//		defineInlinePrim("D0>=", IlitX_d, Icmp+CMP_GE);
//		defineInlinePrim("DU0<", IlitX_d, Icmp+CMP_ULT);
//		defineInlinePrim("DU0<=", IlitX_d, Icmp+CMP_ULE);
//		defineInlinePrim("DU0>", IlitX_d, Icmp+CMP_UGT);
//		defineInlinePrim("DU0>=", IlitX_d, Icmp+CMP_UGE);
//		defineInlinePrim("D<", Icmp_d+CMP_LT);
//		defineInlinePrim("D<=", Icmp_d+CMP_LE);
//		defineInlinePrim("D>", Icmp_d+CMP_GT);
//		defineInlinePrim("D>=", Icmp_d+CMP_GE);
//		defineInlinePrim("DU<", Icmp_d+CMP_ULT);
//		defineInlinePrim("DU<=", Icmp_d+CMP_ULE);
//		defineInlinePrim("DU>", Icmp_d+CMP_UGT);
//		defineInlinePrim("DU>=", Icmp_d+CMP_UGE);
//		
//		defineInlinePrim("(unloop)", IRfrom, Irdrop_d, ItoR);
//		defineInlinePrim("2rdrop", Irdrop_d);
//		defineInlinePrim("2/", I2div);
//		defineInlinePrim("2*", I2times);
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
//		
		defineInlinePrim("SWPB", 
				Iswpb, TOS
				);
//		
//		defineInlinePrim("DLSHIFT", Ilsh_d);
//		defineInlinePrim("DRSHIFT", Iash_d);
//		defineInlinePrim("DURSHIFT", Irsh_d);
//		defineInlinePrim("DCRSHIFT", Icsh_d);
//		
//		defineInlinePrim("*", Iumul, Idrop);
//
//		defineInlinePrim("2DROP", Idrop_d);
//		defineInlinePrim("D>Q", Idup, IlitX, Icmp + CMP_LT, Idup);
//		defineInlinePrim("DUM/MOD", Iudivmod_d);
//		
//		defineInlinePrim("S>D", Idup, IlitX, Icmp+CMP_LT);

		definePrim("EXECUTE",
				Idect, reg(REG_RP),
				Imov, reg(11), regInd(REG_RP),
				Imov, TOS, T1,
				StockInstruction.POP_TOS,
				Ibl, regInd(REG_T1),
				Imov, regInc(REG_RP), reg(11)
				);

//		//defineInlinePrim("DOVAR", IbranchX|0, Idovar);
//		//defineInlinePrim("DOLIT", IlitW, 0, 0, Iexit);
//		
		defineInlinePrim("TRUE", 
				StockInstruction.PUSH_TOS,
				Iseto, TOS
				);
		defineInlinePrim("FALSE", 
				StockInstruction.PUSH_TOS,
				Iclr, TOS
				);

//		
//		defineInlinePrim("(FILL)", Ifill);
//		defineInlinePrim("(CFILL)", Icfill);
//		defineInlinePrim("(CMOVE)", Icmove);
//		
//		defineInlinePrim("(LITERAL)", IlitW);
//		defineInlinePrim("(DLITERAL)", IlitD_d);
//		
//		//defineInlinePrim("(S\")", IcontextFrom, CTX_PC, IlitX | 5, Iadd, Idup, I1plus, Iswap, Icload);
//		defineInlinePrim("((S\"))", Irdrop, IatR, Idup, I1plus, Iswap, Icload, Idup, IRfrom, Iadd, I1plus, ItoR);
//		
//		define("(S\")", new TargetSQuote(defineEntry("(S\")")));
//		compileCall((ITargetWord) find("((s\"))"));
//		compileOpcode(Iexit);
//		
//		defineInlinePrim("CELL+", I2plus); 
//		defineInlinePrim("CELL", IlitX | 0x2);
//		
//		defineInlinePrim("0<>", I0equ, Inot); 
//		defineInlinePrim("<>", Iequ, Inot); 
	}
	
	/**
	 * Call addresses must be aligned
	 */
	public void initWordEntry() {
		alignDP();
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

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#doResolveRelocation(v9t9.forthcomp.RelocEntry)
	 */
	@Override
	protected int doResolveRelocation(RelocEntry reloc) throws AbortException {
		int val = reloc.target;
		return val;
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
		int ptr = alloc(2);
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
		alloc(2);		// assume short
	}
	
	protected int writeJumpOffs(HostContext hostContext, int opAddr, int diff)
			throws AbortException {
		
		int offs = diff;
		logfile.println("T>" + HexUtils.toHex4(opAddr) +" = " + HexUtils.toHex4(offs));
		
		// Opcode is already compiled as 2-byte branch, so diff is relative to offset
		writeCell(opAddr, offs);
		return 0;
	}


	protected void writeJumpOffsAlloc(int diff, boolean conditional)
			throws AbortException {

		buildCall((conditional ? require("0BRANCH") : require("BRANCH")));
		
		buildCell(diff);
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#defineCompilerWords(v9t9.forthcomp.HostContext)
	 */
	@Override
	public void defineCompilerWords(HostContext hostContext) {


	}

	public void compileUser(TargetUserVariable var) {
		int index = var.getIndex();
		pushTOS();
		writeInstruction(Imov, regOffs(REG_UP, index * 2), TOS);
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
	public void compileWordParamAddr(TargetValue word) {
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
	public int buildDoDoes(HostContext hostContext) throws AbortException {
		buildCall(require(";S"));
		return super.buildDoDoes(hostContext);
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileDoDoes(v9t9.forthcomp.HostContext)
	 */
	@Override
	public int compileDoDoes(HostContext hostContext) throws AbortException {
		int addr = getDP();

		// code
		writeInstruction(Ibl, regInd(REG_DODOES));
		
		return addr;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#compileDoes(int)
	 */
	@Override
	public void compileDoes(HostContext hostContext, DictEntry entry, int targetDP) throws AbortException {
		dp -= 4;	// overwrite code
		writeInstruction(Ibl, regInd(REG_DODOES));
		dp += 2;	// skip cell with 
		writeCell(alloc(2), targetDP);
	}

}
