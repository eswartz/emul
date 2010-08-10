/**
 * Aug 7 2010
 */
package v9t9.engine.cpu;

import static v9t9.engine.cpu.InstPatternMFP201.*;
import static v9t9.engine.cpu.InstMFP201.*;
import static v9t9.engine.cpu.MachineOperandMFP201.*;

import java.util.HashMap;
import java.util.Map;

import org.ejs.coffee.core.utils.Pair;

import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

/**
 * @author ejs
 * 
 */
public class InstTableMFP201 {

	public static final String[] condSuffixes = {
		"ne",
		"eq",
		"nc",
		"c",
		"n",
		"ge",
		"l",
		null
		
	};
	private final static byte _IMM8 = 0x10;
	private final static byte _IMM16 = 0x11;
	private final static byte _IMM8_16 = 0x12;
	private final static byte _OFF16 = 0x13;
	private final static byte _JMP = 0x14;
	private final static byte _INST = 0x15;
	private final static byte _IMM3_10_16 = 0x16;
	/** if the immediate size is a byte, at the bit specified in lower nybble */
	private final static byte _IMM_SZ = 0x30;
	/** constant encoded into As bits, low nybble */
	private final static byte _AS_CONST = 0x50;
	/** constant encoded into opcode, value in low nybble, bit in 2nd nybble */
	private final static byte _CONST = (byte) 0x80;
	/** skip to the next byte */
	private final static byte _NEXTB = -2;
	/** skip to the next operand */
	private final static byte _NEXTO = -3;
	/** skip to the next byte and operand */
	private final static byte _NEXT = -4;
	/** emit the opcode */
	private final static byte _OPC = -1;
	/** emit the opcode (high byte) */
	private final static byte _OPC_HI = -5;
	/** emit the opcode (low byte) */
	private final static byte _OPC_LO = -6;
	/** immediate encoded into As bits */
	//private final static byte _IMM_AS = -8;
	/** immediate minus one encoded into As bits */
	private final static byte _CNT_AS_M1 = -9;
	/** LEA's complicated mode */
	private final static byte _LEA = -10;
	/** Flag any *R- bits for LOOP */
	private final static byte _DECS = -11;
	
	final static InstPatternMFP201 DATA_IMM8 = new InstPatternMFP201(
			IMM, new byte[] { _IMM8 });
	final static InstPatternMFP201 DATA_IMM16 = new InstPatternMFP201(
			IMM, new byte[] { _IMM16 });
	final static InstPatternMFP201 NONE_ = new InstPatternMFP201(
			new byte[] { _OPC } );
	final static InstPatternMFP201 IMM_ = new InstPatternMFP201(
			IMM, new byte[] { _OPC, _NEXTO, _IMM16 });
	final static InstPatternMFP201 GENCALL = new InstPatternMFP201(
			GEN, new byte[] { _OPC, _NEXTB, 0 });
	final static InstPatternMFP201 OFF_ = new InstPatternMFP201(
			OFF, new byte[] { _OPC, _NEXTO, _OFF16 });
	final static InstPatternMFP201 JMP_ = new InstPatternMFP201(
			OFF, new byte[] { _OPC, _JMP });
	final static InstPatternMFP201 IMMx_GEN = new InstPatternMFP201(
			IMM, GEN, new byte[] { _OPC, _NEXTB, _IMM3_10_16, _NEXTO, 0 });
	
	final static InstPatternMFP201 GEN_ = new InstPatternMFP201(
			GEN,  
			new byte[] { _OPC, 0 });
	final static InstPatternMFP201 AS1_GEN_ = new InstPatternMFP201(
			GEN,  
			new byte[] { _AS_CONST | 1, _OPC, 0 });
	final static InstPatternMFP201 AS2_GEN_ = new InstPatternMFP201(
			GEN,  
			new byte[] { _AS_CONST | 2, _OPC, 0 });
	final static InstPatternMFP201 AS3_GEN_ = new InstPatternMFP201(
			GEN,  
			new byte[] { _AS_CONST | 3, _OPC, 0 });
	final static InstPatternMFP201 CNT4M1_GEN = new InstPatternMFP201(
			CNT, GEN,  
			new byte[] { _CNT_AS_M1, _OPC, _NEXTO, 0 });
	
	final static InstPatternMFP201 GEN_REG_GEN = new InstPatternMFP201(
			GEN, REG, GEN, 
			new byte[] { _OPC, 0, _NEXT, 4, _NEXTO, 0 });
	final static InstPatternMFP201 GEN_GEN_SR = new InstPatternMFP201(
			GEN, GEN,  
			new byte[] { _OPC, 0, _NEXT, 4, _CONST | 0xf });
	
	final static InstPatternMFP201 GEN_GEN = new InstPatternMFP201(
			GEN, GEN,  
			new byte[] { _OPC, _NEXTB, 4, _NEXTO, 0 });
	final static InstPatternMFP201 CNT_GEN = new InstPatternMFP201(
			CNT, GEN,  
			new byte[] { _OPC, _NEXTB, 4, _NEXTO, 0 });
	final static InstPatternMFP201 SFO_REG = new InstPatternMFP201(
			SRO, REG,  
			new byte[] { _OPC_HI, _IMM_SZ | 3, _OPC_LO, _LEA, _IMM8_16 });
	
	final static InstPatternMFP201 REG_INST = new InstPatternMFP201(
			REG, INST,  
			new byte[] { _OPC, _NEXTB, 0, _NEXTO, _DECS, _NEXTB, _INST });
	final static InstPatternMFP201 INST_ = new InstPatternMFP201(
			INST,  
			new byte[] { _OPC, _NEXTB, _CONST | 0xf, _DECS, _NEXTB, _INST });

	/** mappings for R13, R14, R15 in src1R position of 3-op logical instruction */
	public static final int[][] LOGICAL_INST_CONSTANTS = {
		{ 1, 0x8000, 0xffff },
		{ 1, 0x80, 0xff }
	};
	/** mappings for R13, R14, R15 in src1R position of 3-op arith instruction */
	public static final int[] ARITHMETIC_INST_CONSTANTS = {
		1, 2, 0
	};

	static Map<Integer, InstPatternMFP201[]> instEntries = new HashMap<Integer, InstPatternMFP201[]>();
	static Map<Integer, Integer> instMasks = new HashMap<Integer, Integer>();

	private static void register(int inst, int opcode, InstPatternMFP201 entry, int mask) {
		
		entry = entry.opcode(opcode);
		
		registerInstPattern(inst, entry);
		instMasks.put(inst, mask);
	}

	private static void registerInstPattern(int inst, InstPatternMFP201 entry) {
		InstPatternMFP201[] entries = instEntries.get(inst);
		InstPatternMFP201[] newEntries;
		if (entries == null) {
			newEntries = new InstPatternMFP201[] { entry };
		} else {
			newEntries = new InstPatternMFP201[entries.length + 1];
			System.arraycopy(entries, 0, newEntries, 0, entries.length);
			newEntries[entries.length] = entry;
		}
			
		instEntries.put(inst, newEntries);
	}
	
	private static void register2(int inst, int opcode,InstPatternMFP201 entry,  int mask) {
		register(inst, opcode, entry, mask);
		register(inst + 1, opcode, entry, mask);
	}
	
	private static void register4(int inst, int opcode, int mask) {
		register(inst, opcode, GEN_REG_GEN, mask);
		register(inst + 1, opcode, GEN_REG_GEN, mask);
		
		// in 3-op form, non-writing ADD->TST, ADC->TSTN
		if (inst == Iadd) 
			inst = Itst;
		else if (inst == Iadc) 
			inst = Itstn;
		else if (inst == Iand || inst == Inand)
			// these do not have no-write forms, since we write SR
			;
		else 
			inst += 2;
		
		register(inst, opcode, GEN_REG_GEN, mask);
		register(inst + 1, opcode, GEN_REG_GEN, mask);
		register(inst, opcode, GEN_GEN_SR, mask);
		register(inst + 1, opcode, GEN_GEN_SR, mask);
	}

	private static void registerImm(int inst, int opcode) {
		register(inst, opcode, IMMx_GEN, opcode);
	}

	static {
		/*
		 * Multiple patterns may be registered for the same instruction.
		 * They are selected according to the order registered here.  
		 */
		register(InstTableCommon.Idata, 0x0000, DATA_IMM16, 0xffff);
		register(InstTableCommon.Ibyte, 0x0000, DATA_IMM8, 0xffff);

		register(Ibkpt, 0x0, NONE_, 0x0);
		//register(Iret, 0x3e, NONE_, 0x3e);
		//register(Ireti, 0x443e, NONE_, 0x443e);
		register(Ibr, 0x04, OFF_, 0x04);
		register(Ibra, 0x05, IMM_, 0x05);
		register(Icall, 0x06, OFF_, 0x06);
		register(Icalla, 0x07, IMM_, 0x07);
		
		register(Icall, 0x07, GENCALL, 0x07);
		
		/* Register + immediate versions */
		registerImm(Ior, 0x08);
		registerImm(Iand, 0x09);
		registerImm(Inand, 0x0a);
		registerImm(Ixor, 0x0b);
		registerImm(Iadd, 0x0c);
		registerImm(Iadc, 0x0d);
		registerImm(Isub, 0x0e);
		registerImm(Ildc, 0x0f);

		//// long form of LDC is just MOV
		// _testEncode("MOV #>1234, R2", new byte[] { 0x4c, 0x7f, (byte) 0xE2, (byte) 0x12, 0x34 });
		//register(Ildc, 0x4c7fe0, IMM16_REG_, 0x4c7fef);

		/* simple one-ops */
		register(Isext, 0x10, GEN_, 0x1f);
		register(Iextl, 0x10, AS1_GEN_, 0x1f);
		register(Iexth, 0x10, AS2_GEN_, 0x1f);
		register(Iswpb, 0x10, AS3_GEN_, 0x1f);
		
		register2(Ipush, 0x20, GEN_, 0x2f);
		register2(Ipushn, 0x20, CNT4M1_GEN, 0x2f);
		register2(Ipop, 0x30, GEN_, 0x3f);
		register2(Ipopn, 0x30, CNT4M1_GEN, 0x3f);

		/* shifts, mul/div */
		register2(Ilsh, 0x68, CNT_GEN, 0x68);
		register2(Irsh, 0x69, CNT_GEN, 0x69);
		register2(Iash, 0x6a, CNT_GEN, 0x6a);
		register2(Irol, 0x6b, CNT_GEN, 0x6b);
		register2(Imul, 0x6c, GEN_GEN, 0x6c);
		register2(Idiv, 0x6d, GEN_GEN, 0x6d);
		register2(Imuld, 0x6e, GEN_GEN, 0x6e);
		register2(Idivd, 0x6f, GEN_GEN, 0x6f);

		/* jumps */
		register(Ijne, 0x70, JMP_, 0x70);
		register(Ijeq, 0x71, JMP_, 0x71);
		register(Ijnc, 0x72, JMP_, 0x72);
		register(Ijc, 0x73, JMP_, 0x73);
		register(Ijn, 0x74, JMP_, 0x74);
		register(Ijge, 0x75, JMP_, 0x75);
		register(Ijl, 0x76, JMP_, 0x76);
		register(Ijmp, 0x77, JMP_, 0x77);
		
		/* moves */
		register2(Imovne, 0x78, GEN_GEN, 0x78);
		register2(Imoveq, 0x79, GEN_GEN, 0x79);
		register2(Imovnc, 0x7a, GEN_GEN, 0x7a);
		register2(Imovc, 0x7b, GEN_GEN, 0x7b);
		register2(Imovn, 0x7c, GEN_GEN, 0x7c);
		register2(Imovge, 0x7d, GEN_GEN, 0x7d);
		register2(Imovl, 0x7e, GEN_GEN, 0x7e);
		register2(Imov, 0x7f, GEN_GEN, 0x7f);
		
		/* lea! */
		register(Ilea, 0x5010, SFO_REG, 0x5f1f);
		
		register(Iloopne, 0x60, REG_INST, 0x60);
		register(Iloopeq, 0x61, REG_INST, 0x61);
		register(Iloopnc, 0x62, REG_INST, 0x62);
		register(Iloopc, 0x63, REG_INST, 0x63);
		register(Iloopn, 0x64, REG_INST, 0x64);
		register(Iloopge, 0x65, REG_INST, 0x65);
		register(Iloopl, 0x66, REG_INST, 0x66);
		register(Iloop, 0x67, REG_INST, 0x67);
		register(Istepne, 0x60, INST_, 0x60);
		register(Istepeq, 0x61, INST_, 0x61);
		register(Istepnc, 0x62, INST_, 0x62);
		register(Istepc, 0x63, INST_, 0x63);
		register(Isteps, 0x64, INST_, 0x64);
		register(Istepge, 0x65, INST_, 0x65);
		register(Istepl, 0x66, INST_, 0x66);
		register(Istep, 0x67, INST_, 0x67);
		
		/* three-op instructions */
		register4(Ior, 0x80, 0x8f);
		register4(Iand, 0x90, 0x9f);
		register4(Inand, 0xa0, 0xaf);
		register4(Ixor, 0xb0, 0xbf);
		register4(Iadd, 0xc0, 0xcf);
		register4(Iadc, 0xd0, 0xdf);
		register4(Isub, 0xe0, 0xef);
		register4(Isbb, 0xf0, 0xff);
		
		/*
		register(InstTableCommon.Idsr, 0x0c00, OFF_NONE, 0xcff);
		register(InstTableCommon.Ikysl, 0x0d40, NONE_, 0xd40);
		register(InstTableCommon.Iticks, 0x0d60, REG_NONE, 0xd60);
		register(InstTableCommon.Iemitchar, 0x0dc0, REG_CNT, 0xdcf);
		register(InstTableCommon.Idbg, 0x0de0, NONE_, 0xde0);
		register(InstTableCommon.Idbgf, 0x0de1, NONE_, 0xde1);
		 */

	};
	

	public static Map<String, Integer> nameToInst = new HashMap<String, Integer>();
	public static Map<Integer, String> instToName = new HashMap<Integer, String>();
	
	static {
	registerInstruction(InstTableCommon.Idata, "data");
	registerInstruction(InstTableCommon.Ibyte, "byte");
	registerInstruction(Ibkpt, "bkpt");
	//registerInstruction(Iret, "ret");
	//registerInstruction(Ireti, "reti");
	registerInstruction(Ibr, "br");
	registerInstruction(Ibra, "bra");
	registerInstruction(Icall, "call");
	registerInstruction(Icalla, "calla");
	
	registerInstruction(Isext, "sext");
	registerInstruction(Iexth, "exth");
	registerInstruction(Iextl, "extl");
	registerInstruction(Iswpb, "swpb");
	registerInstruction(Ipush, "push");
	registerInstruction(Ipushn, "pushn");
	registerInstruction(Ipushb, "push.b");
	registerInstruction(Ipushnb, "pushn.b");
	registerInstruction(Ipop, "pop");
	registerInstruction(Ipopn, "popn");
	registerInstruction(Ipopb, "pop.b");
	registerInstruction(Ipopnb, "popn.b");
	
	registerInstructionCond(Ijmp, "jmp", "j");
	
	registerInstructionCondByte(Imov, "mov", "mov");
	
	registerInstruction(Ilsh, "lsh");
	registerInstruction(Ilshb, "lsh.b");
	registerInstruction(Irsh, "rsh");
	registerInstruction(Irshb, "rsh.b");
	registerInstruction(Iash, "ash");
	registerInstruction(Iashb, "ash.b");
	registerInstruction(Irol, "rol");
	registerInstruction(Irolb, "rol.b");
	registerInstruction(Imul, "mul");
	registerInstruction(Imulb, "mul.b");
	registerInstruction(Idiv, "div");
	registerInstruction(Idivb, "div.b");
	registerInstruction(Imuld, "muld");
	registerInstruction(Imuldb, "muld.b");
	registerInstruction(Idivd, "divd");
	registerInstruction(Idivdb, "divd.b");
	
	registerInstruction(Ilea, "lea");
	
	registerInstructionCond(Iloop, "loop", "loop");
	registerInstructionCond(Istep, "step", "step");
	
	registerInstruction(Ior, "or");
	registerInstruction(Iorb, "or.b");
	registerInstruction(Iorq, "or?");
	registerInstruction(Iorbq, "or.b?");
	
	registerInstruction(Iand, "and");
	registerInstruction(Iandb, "and.b");
	registerInstruction(Itst, "tst");
	registerInstruction(Itstb, "tst.b");
	
	registerInstruction(Inand, "nand");
	registerInstruction(Inandb, "nand.b");
	registerInstruction(Itstn, "tstn");
	registerInstruction(Itstnb, "tstn.b");
	
	registerInstruction(Ixor, "xor");
	registerInstruction(Ixorb, "xor.b");
	registerInstruction(Ixorq, "xor?");
	registerInstruction(Ixorbq, "xor.b?");
	
	registerInstruction(Iadd, "add");
	registerInstruction(Iaddb, "add.b");
	
	registerInstruction(Isub, "sub");
	registerInstruction(Isubb, "sub.b");
	registerInstruction(Icmp, "cmp");
	registerInstruction(Icmpb, "cmp.b");
	
	registerInstruction(Iadc, "adc");
	registerInstruction(Iadcb, "adc.b");
	
	registerInstruction(Isbb, "sbb");
	registerInstruction(Isbbb, "sbb.b");
	registerInstruction(Icmpr, "cmpr");
	registerInstruction(Icmprb, "cmpr.b");
	
	registerInstruction(Ildc, "ldc");
	
	registerInstruction(InstTableCommon.Idsr, "dsr");
	registerInstruction(InstTableCommon.Ikysl, "kysl");
	registerInstruction(InstTableCommon.Iticks, "ticks");
	registerInstruction(InstTableCommon.Iemitchar, "emitchar");
	registerInstruction(InstTableCommon.Idbg, "dbg");
	registerInstruction(InstTableCommon.Idbgf, "dbgf");
	registerInstruction(InstTableCommon.Ibyte, "byte");

	//registerAlias(Ijeq, "je");
	//registerAlias(Ijoc, "jc");

	}

	private static final Map<Integer, PseudoPattern> pseudoPatterns = new HashMap<Integer, PseudoPattern>();
	
	private static final void registerPseudo(int pop, String pname, int numops, int realop,
			LLOperand op1patt, LLOperand op2patt, LLOperand op3patt) {
		nameToInst.put(pname.toUpperCase(), pop);
		instToName.put(pop, pname.toUpperCase());
		PseudoPattern pattern = new PseudoPattern(numops, realop, op1patt, op2patt, op3patt);
		pseudoPatterns.put(pop, pattern);
		
		final byte[] NO_BYTES = new byte[0];
		InstPatternMFP201 instPattern;
		if (numops == 0)
			instPattern = new InstPatternMFP201(NO_BYTES);
		else if (numops == 1)
			instPattern = new InstPatternMFP201(GEN, NO_BYTES);
		else if (numops == 2)
			instPattern = new InstPatternMFP201(GEN, GEN, NO_BYTES);
		else 
			instPattern = new InstPatternMFP201(GEN, GEN, GEN, NO_BYTES);
		registerInstPattern(pop, instPattern);
		
	}
	private static final void registerPseudoByte(int pop, String pname, int numops, int realop,
			LLOperand op1patt, LLOperand op2patt, LLOperand op3patt) {
		registerPseudo(pop, pname, numops, realop, op1patt, op2patt, op3patt);
		registerPseudo(pop + 1, pname + ".b", numops, realop + 1, op1patt, op2patt, op3patt);
	}
	
	public static PseudoPattern lookupPseudoPattern(int inst) {
		return pseudoPatterns.get(inst);
	}
	
	public static final LLPositionalOperand P_OP1 = new LLPositionalOperand(0);
	public static final LLPositionalOperand P_OP2 = new LLPositionalOperand(1);
	public static final LLPositionalOperand P_OP3 = new LLPositionalOperand(2);
	static {
		registerPseudo(Pnop, "nop", 0,
				Ijmp, new LLPCRelativeOperand(null, 0), null, null);
		registerPseudo(Pret, "ret", 0,
				Ipop, new LLRegisterOperand(PC), null, null);
		registerPseudo(Preti, "reti", 0,
				Ipopn, new LLImmedOperand(2), new LLRegisterOperand(PC), null);
		
		registerPseudo(Pclr, "clr", 1, 
				Ildc, new LLImmedOperand(0), P_OP1, null);
		registerPseudo(Pclrb, "clr.b", 1, 
				Ildc, new LLImmedOperand(0), P_OP1, null);
		registerPseudo(Pseto, "seto", 1, 
				Ildc, new LLImmedOperand(0xffff), P_OP1, null);
		
		registerPseudo(Pinv, "inv", 1, 
				Ixor, P_OP1, new LLImmedOperand(0xffff), P_OP1);
		registerPseudo(Pinvb, "inv.b", 1, 
				Ixorb, P_OP1, new LLImmedOperand(0xff), P_OP1);
		
		registerPseudoByte(Pinc, "inc", 1, 
				Iadd, P_OP1, new LLImmedOperand(1), P_OP1);
		registerPseudoByte(Pinct, "inct", 1, 
				Iadd, P_OP1, new LLImmedOperand(2), P_OP1);
		registerPseudoByte(Pdec, "dec", 1, 
				Isub, P_OP1, new LLImmedOperand(1), P_OP1);
		registerPseudoByte(Pdect, "dect", 1, 
				Isub, P_OP1, new LLImmedOperand(2), P_OP1);
		
	}
	public static InstPatternMFP201 getInstPattern(RawInstruction rawInst) throws IllegalArgumentException {
		int inst = rawInst.getInst();
		//int variant = rawInst instanceof InstructionMFP201 ? 
		//		((InstructionMFP201) rawInst).variant : InstructionMFP201.VARIANT_NONE;
				
		InstPatternMFP201[] patterns = instEntries.get(inst);
		if (patterns == null)
			throw new IllegalArgumentException("Non-encoded instruction " + rawInst);
		
		MachineOperandMFP201 mop1 = (MachineOperandMFP201) rawInst.getOp1();
		MachineOperandMFP201 mop2 = (MachineOperandMFP201) rawInst.getOp2();
		MachineOperandMFP201 mop3 = (MachineOperandMFP201) rawInst.getOp3();
		
		IllegalArgumentException lastException = null;
		for (InstPatternMFP201 pattern : patterns) {
			try {
				assertOperandMatches(rawInst, mop1, pattern.op1);
				assertOperandMatches(rawInst, mop2, pattern.op2);
				assertOperandMatches(rawInst, mop3, pattern.op3);
				return pattern;
			} catch (IllegalArgumentException e) {
				lastException = e;
				continue;
			}
		}
		if (lastException != null)
			throw lastException;
		return null;
	}
	
	public static byte[] encode(RawInstruction rawInst) throws IllegalArgumentException {
		int inst = rawInst.getInst();
		
		//int variant = rawInst instanceof InstructionMFP201 ? 
		//		((InstructionMFP201) rawInst).variant : InstructionMFP201.VARIANT_NONE;
				
		if (rawInst.getOp1() != null && !(rawInst.getOp1() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 1: " + rawInst.getOp1());
		if (rawInst.getOp2() != null && !(rawInst.getOp2() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 2: " + rawInst.getOp2());
		if (rawInst.getOp3() != null && !(rawInst.getOp3() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 3: " + rawInst.getOp3());

		// copy away since we change it
		rawInst = new RawInstruction(rawInst);
		coerceOperandTypes(rawInst);
		
		rawInst.byteop = isByteInst(inst);
		
		MachineOperandMFP201 mop1 = (MachineOperandMFP201) rawInst.getOp1();
		MachineOperandMFP201 mop2 = (MachineOperandMFP201) rawInst.getOp2();
		MachineOperandMFP201 mop3 = (MachineOperandMFP201) rawInst.getOp3();
		
		InstPatternMFP201 pattern = getInstPattern(rawInst);
		
		// hi 2 bytes: loop prefix, lo byte: mem/size byte 
		int loopAndMemSize = 0;

		MachineOperandMFP201[] mops = { mop1, mop2, mop3 };
		
		byte[] immeds = { 0, 0, 0, 0 };
		int immIdx = 0;
		
		byte[] work = new byte[16];
		
		int mopIdx = 0;
		int workIdx = -1;
		
		MachineOperandMFP201 mop = null;

		int opcode = pattern.opcode;
		
		if (InstTableMFP201.isByteInst(inst)
				&& !(opcode >= 0x800 && opcode <= 0x9ff)) {
			loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE 
				| InstructionMFP201.MEM_SIZE_SZ;
		}
		
		for (int idx = 0; idx < pattern.enc.length; idx++) {
			byte enc = pattern.enc[idx];
			if (enc == _OPC) {
				workIdx++;
				if (opcode >= 0x1000000) {
					work[workIdx++] = (byte) (opcode >> 24);
				}
				if (opcode >= 0x10000) {
					work[workIdx++] = (byte) (opcode >> 16);
				}
				if (opcode >= 0x100) {
					work[workIdx++] = (byte) (opcode >> 8);
				}
				work[workIdx] = (byte) (opcode & 0xff);
			}
			else if (enc == _OPC_HI) {
				workIdx++;
				work[workIdx] = (byte) (opcode >> 8);
			}
			else if (enc == _OPC_LO) {
				workIdx++;
				work[workIdx] = (byte) (opcode & 0xff);
			}
			else {
				if (enc == _NEXT) {
					workIdx++;
					mop = null;
					continue;
				}
				if (enc == _NEXTB) {
					workIdx++;
				}
				if (enc == _NEXTO) {
					mop = null;
					continue;
				}
				if (mop == null) {
					mop = mops[mopIdx++];
					
					if (mop != null && inst != Ilea) {
						if ((mop.type >= OP_REG
								&& mop.type <= OP_INC) 
								|| mop.type == OP_DEC) {
							int bit = mopIdx == 1 && (inst == Icall || pattern.op2 == InstPatternMFP201.GEN || pattern.op3 == InstPatternMFP201.GEN)
								? 2 : 0;
							int Am = mop.type & 0x3;
							if (Am != 0)
								loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE
									| (Am << bit);
							
						}
						
						int opm = pattern.op(mopIdx);
						if (mop.hasImmediate() && opm == InstPatternMFP201.GEN) 
						{
							if (mop.encoding == OP_ENC_IMM8) {
								immeds[immIdx++] = (byte) (mop.immed & 0xff);
							}
							else {
								immeds[immIdx++] = (byte) (mop.immed >> 8);
								immeds[immIdx++] = (byte) (mop.immed & 0xff);
							}
						}
					}
				}

				if (enc >= 0 && enc < 8) {
					work[workIdx] |= (byte) ((mop.val & 0xf) << enc); 
				}
				else if (enc == _IMM8) {
					immeds[immIdx++] = (byte) (mop.immed & 0xff); 
				}
				else if (enc == _IMM16) {
					immeds[immIdx++] = (byte) ((mop.immed & 0xff00) >> 8); 
					immeds[immIdx++] = (byte) (mop.immed & 0xff); 
				}
				else if (enc == _IMM3_10_16) {
					work[workIdx] |= (mop.immed & 0x7) << 4;
					if (!isImm3(mop.immed)) {
						work[workIdx] |= 0x80;
						immeds[immIdx++] = (byte) ((mop.immed & 0x3f8) >> 3);
						if (!isImm10(mop.immed)) {
							immeds[immIdx-1] |= 0x80;
							immeds[immIdx++] = (byte) ((mop.immed & 0xfc00) >> 10);
						}
					}
				}
				else if (enc == _IMM8_16) {
					if (!isImm8(mop.immed))
						immeds[immIdx++] = (byte) ((mop.immed & 0xff00) >> 8);
					immeds[immIdx++] = (byte) (mop.immed & 0xff);
				}
				else if (enc == _OFF16) {
					int val;
					if (mop.type == OP_PCREL) {
						val = mop.val - workIdx - immIdx - 2 - 1;
					} else {
						val = mop.immed - workIdx - immIdx - 2 - 1 - rawInst.pc;
					}
					immeds[immIdx++] = (byte) ((val & 0xff00) >> 8); 
					immeds[immIdx++] = (byte) (val & 0xff); 
				}
				else if (enc == _JMP) {
					int val;
					if (mop.type == OP_PCREL) {
						val = mop.val - workIdx - immIdx;
					} else {
						val = mop.immed - workIdx - immIdx - rawInst.pc;
					}
					
					if (mop.encoding == OP_ENC_PCREL8
							|| isImm8((short) (val - 2))) {
						val -= 2;
						immeds[immIdx++] = (byte) (val & 0xff);
					}
					else if (mop.encoding == OP_ENC_PCREL12
							|| isJumpImm12(val - 3)) {
						val -= 3;
						immeds[immIdx++] = (byte) (val & 0xff);
						loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE 
							| (val >> 8) & 0xf;
					}
					else {
						val -= 4;
						immeds[immIdx++] = (byte) (val & 0xff);
						loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE 
							| InstructionMFP201.MEM_SIZE_SZ
							| (val >> 8) & 0xf;
						immeds[immIdx++] = (byte) (val >> 12);
					}
				}
				else if (enc == _CNT_AS_M1) {	
					if (mop.val >= 1 && mop.val <= 4) 
						loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE |
							((mop.val - 1) << 2);
					else
						throw new IllegalArgumentException("count must be in the range 1 to 4 in " + rawInst);
				}
				else if (enc == _LEA) {
					// this is a special case: we do not emit a traditional mem/size byte
					// but a cooked one which looks like "sext.b" as part of the opcode
					MachineOperandMFP201 sro = (MachineOperandMFP201) rawInst.getOp1();
					
					// first byte has scale
					work[0] |= sro.scaleBits;
					
					// second byte has opcode and destR
					work[1] |= ((MachineOperandMFP201) rawInst.getOp2()).val;
					
					// third byte (new) has addR | srcR
					if (sro.type == OP_SRO)
						work[++workIdx] = (byte) ((sro.val << 4) | (sro.scaleReg));
					else
						work[++workIdx] = (byte) ((sro.val << 4) | SR);
					
					// immediate and 
				}
				else if (enc == _DECS) {
					RawInstruction subInst = ((MachineOperandMFP201Inst) mop).inst;
					subInst = new RawInstruction(subInst);
					coerceOperandTypes(subInst);
					MachineOperandMFP201 subop;
					subop = subInst.getOp(getFirstSrcOp(subInst));
					if (subop != null && subop.type == OP_DEC)
						work[1] |= 0x20;
					subop = subInst.getOp(getDestOp(subInst));
					if (subop != null && subop.type == OP_DEC)
						work[1] |= 0x10;
				}
				else if (enc == _INST) {
					RawInstruction subInst = ((MachineOperandMFP201Inst) mop).inst;
					byte[] bytes = encode(subInst);
					System.arraycopy(bytes, 0, work, workIdx, bytes.length);
					workIdx += bytes.length - 1;
				}
				else if ((enc & 0xf0) == _IMM_SZ) {	
					if (isImm8(mop.immed))
						work[workIdx] |= (byte) (1 << (enc & 0xf));
				}
				else if ((enc & 0xf0) == _AS_CONST) {	
					loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE | ((enc & 0x3) << 2);
					
				}
				else if ((byte)(enc & 0xf0) == _CONST) {	
					work[workIdx] |= (enc & 0xf) << ((enc & 0x70) >> 4);
				}
			}
		}
		workIdx++;
		
		int offs = 0;
		if ((loopAndMemSize & 0xff) != 0)
			offs++;
		if (loopAndMemSize >= 0x10000) {
			offs += 2;
		}
		byte[] bytes = new byte[workIdx + immIdx + offs];
		System.arraycopy(work, 0, bytes, offs, workIdx);
		System.arraycopy(immeds, 0, bytes, workIdx + offs, immIdx);
		
		offs = 0;
		if (loopAndMemSize >= 0x10000) {
			bytes[offs++] = (byte) ((loopAndMemSize & 0xff000000) >> 24);
			bytes[offs++] = (byte) ((loopAndMemSize & 0xff0000) >> 16);
			offs += 2;
		}
		if ((loopAndMemSize & 0xff) != 0) {
			bytes[offs++] = (byte) (loopAndMemSize & 0xff);	
		}
		return bytes;
	}

	public static boolean canHaveMemSizeByte(int inst) {
		return inst >= _IlastSimpleImmediate;
	}

	public static boolean canBeSimpleImmediateInst(int inst) {
		return (inst >= Ior && inst <= Ildc);
	}

	public static boolean isImm3(short immed) {
		return immed >= -4 && immed < 4;
	}
	public static boolean isImm8(short immed) {
		return (byte) (immed & 0xff) == immed;
	}
	public static boolean isImm10(int i) {
		return i >= -0x200 && i < 0x200;
	}
	public static boolean isJumpImm12(int immed) {
		return immed >= -0x800 && immed < 0x800;
	}

	private static void assertOperandMatches(RawInstruction inst, BaseMachineOperand mop, int op) {
		if (mop == null && op != NONE)
			throw new IllegalArgumentException("Operand missing in " + inst);
		else if (mop != null && mop.type != MachineOperand.OP_NONE && op == NONE)
			throw new IllegalArgumentException("Unexpected operand: " + mop + " in " + inst);
		
		switch (op) {
		case IMM:
			if (!mop.isConstant())
				throw new IllegalArgumentException("Expected immediate: " + mop + " in " + inst);
			break;
		case CNT:
			if (mop.type != OP_CNT && mop.type != OP_IMM)
				throw new IllegalArgumentException("Expected count: " + mop + " in " + inst);
			break;
		case OFF:
			if (mop.type != OP_CNT && mop.type != OP_IMM
					&& mop.type != OP_PCREL)
				throw new IllegalArgumentException("Expected offset: " + mop + " in " + inst);
			break;
		case REG:
			if (mop.type != OP_REG && mop.type != OP_REG0_SHIFT_COUNT)
				throw new IllegalArgumentException("Expected register: " + mop + " in " + inst);
			break;
		case GEN:
			if (mop.type == OP_NONE
					|| mop.type == OP_IMM)
				throw new IllegalArgumentException("Expected general operand: " + mop + " in " + inst);
			break;
		case SRO:
			if (mop.type != OP_DEC
					&& mop.type != OP_INC
					&& mop.type != OP_IND
					&& mop.type != OP_OFFS
					&& mop.type != OP_SRO) {
				throw new IllegalArgumentException("Expected general or scaled operand: " + mop + " in " + inst);
			}
			break;
		case INST:
			if (mop.type != OP_INST)
				throw new IllegalArgumentException("Expected instruction: " + mop + " in " + inst);
			break;
		}
	}

	/** 
	 * Ensure that operands have the final types needed for machine code.
	 * operands as needed
	 * @param instruction
	 * @param finalPass if true, performs final coercions before generating
	 * machine code; otherwise, just overcomes 
	 */
	public static InstPatternMFP201 coerceOperandTypes(RawInstruction instruction) {
		InstPatternMFP201[] patterns = instEntries.get(instruction.getInst());
		if (patterns == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		// find a match
		IllegalArgumentException lastException = null;
		
		for (InstPatternMFP201 pattern : patterns) {
			MachineOperandMFP201 
				mop1 = (MachineOperandMFP201) instruction.getOp1(), 
				mop2 = (MachineOperandMFP201) instruction.getOp2(), 
				mop3 = (MachineOperandMFP201) instruction.getOp3();
			
			mop1 = coerceOperandType(instruction, 1, mop1, pattern.op1);
			mop2 = coerceOperandType(instruction, 2, mop2, pattern.op2);
			mop3 = coerceOperandType(instruction, 3, mop3, pattern.op3);
			
			if ((mop3 == null && pattern.length >= 3) || (mop3 != null && pattern.length < 3))
				continue;
			if ((mop2 == null && pattern.length >= 2) || (mop2 != null && pattern.length < 2))
				continue;
			if ((mop1 == null && pattern.length >= 1) || (mop1 != null && pattern.length < 1))
				continue;
			

			try {
				assertOperandMatches(instruction, mop1, pattern.op1);
				assertOperandMatches(instruction, mop2, pattern.op2);
				assertOperandMatches(instruction, mop3, pattern.op3);
				
				instruction.setOp1(mop1);
				instruction.setOp2(mop2);
				instruction.setOp3(mop3);
				return pattern;
			} catch (IllegalArgumentException e) {
				lastException = e;
			}
		}
		if (lastException != null)
			throw lastException;
		
		return null;
	}
	
	private static MachineOperandMFP201 coerceOperandType(RawInstruction instruction, int nop, MachineOperandMFP201 mop, int op) {
		if (op == NONE) {
			if (mop != null && mop.type == OP_REG && mop.val == 15 && mop.encoding == OP_ENC_NON_WRITING)
				return null;
			return mop;
		}
		
		if (mop == null)
			return null;
		
		switch (op) {
		case IMM:
			break;
		case CNT:
			if (mop.type == OP_REG0_SHIFT_COUNT
					|| (mop.type == OP_REG && mop.val == 0)) {
				return createGeneralOperand(OP_CNT, 
						mop.val);
			}
			if (mop.type == OP_IMM) {
				return createGeneralOperand(OP_CNT,
						mop.immed);
			}
			break;
		case OFF:
			if (mop.type == OP_IMM) {
				// convert address to offset from this inst
				return createGeneralOperand(OP_PCREL,
						mop.val - instruction.pc);
			}
			break;
		case REG:
			break;
		case GEN:
			if (mop.type == OP_IMM && nop != getDestOp(instruction)) {
				// immediate is *PC+
				MachineOperandMFP201 immed = createGeneralOperand(
						OP_INC,
						PC,
						mop.immed);
				if (canConvertToByteInst(instruction, mop)) {
					// if all imm or reg ops, we can convert this to a byte
					if (hasNoOtherMemoryOperands(instruction, mop)) {
						instruction.setInst(instruction.getInst() | 1);
					}

				}
				immed.encoding = isByteInst(instruction.getInst()) ? 
						OP_ENC_IMM8 : OP_ENC_IMM16;
				return immed;
			}
			else if (mop.type == OP_PCREL) {
				// PC-rel is @x(PC)
				MachineOperandMFP201 pcrel = createGeneralOperand(
						OP_OFFS,
						PC,
						mop.immed);
				return pcrel;
			}
			break;
		}
		return mop;
	}

	private static boolean hasNoOtherMemoryOperands(RawInstruction instruction,
			MachineOperandMFP201 mop) {
		return instruction.getOp2() == null
		||
			((instruction.getOp2() != null 
				&& instruction.getOp2() != mop 
				&& ((MachineOperand) instruction.getOp2()).isRegister())
			&& 
			((instruction.getOp3() == null 
					|| (instruction.getOp3() != mop 
					&& ((MachineOperand) instruction.getOp3()).isRegister())))
			);
	}

	private static boolean canConvertToByteInst(RawInstruction instruction,
			MachineOperandMFP201 mop) {
		return !isByteInst(instruction.getInst()) && isImm8(mop.immed)
				&& canBeByteInst(instruction.getInst())
				&& (instruction.getInst() < _IfirstPushPopNOp
					|| instruction.getInst() > _IlastPushPopNOp)
					;
	}
	
	public static int coerceInstructionOpcode(int inst, int opcode) {
		Integer mask = instMasks.get(inst);
		if (mask == null)
			return opcode;
		return opcode & mask;
	}
	public static InstPatternMFP201[] lookupEncodePatterns(int inst) {
		return instEntries.get(inst);
	}
	
	public static void registerInstruction(int inst, String str) {
	    Integer i = new Integer(inst);
	    nameToInst.put(str.toUpperCase(), i);
	    instToName.put(i, str.toUpperCase());
	}
	
	public static void registerInstructionCond(int inst, String nocond, String prefix) {
		registerInstruction(inst, nocond);
	    
	    for( int cond = 0; cond < 7; cond++) {
	    	registerInstruction(inst - 7 * 2 + cond * 2, prefix + condSuffixes[cond]);
	    }
	}
	public static void registerInstructionCondByte(int inst, String nocond, String prefix) {
		registerInstruction(inst, nocond);
		registerInstruction(inst + 1, nocond + ".b");
	    
	    for( int cond = 0; cond < 7; cond++) {
	    	registerInstruction(inst - 7 * 2 + cond * 2, prefix + condSuffixes[cond]);
	    	registerInstruction(inst - 7 * 2 + cond * 2 + 1, prefix + condSuffixes[cond] + ".b");
	    }
	}
	public static void registerAlias(int inst, String str) {
	    Integer i = new Integer(inst);
	    nameToInst.put(str.toUpperCase(), i);
	}
	/**
	 * Get the instruction code for the given instruction
	 * @param str
	 * @return
	 */
	public static Integer lookupInst(String str) {
		str = str.toUpperCase();
		Integer inst = nameToInst.get(str);
		return inst;
	}
	/**
	 * Get an instruction by name
	 * @param inst
	 * @return
	 */
	public static String getInstName(int inst) {
		return instToName.get(inst);
	}
	
   /**
     * Decode instruction with opcode 'op' at 'addr' into 'ins'.
     * 
     * @param domain
     *            provides read access to memory, to decode registers and
     *            instructions
     */
    public static InstructionMFP201 decodeInstruction(int pc, MemoryDomain domain) {
    	InstructionMFP201 inst = new InstructionMFP201();
		
    	inst.pc = pc;

    	int op;
    	
    	op = domain.flatReadByte(pc) & 0xff;
    	
    	// loop?
    	if ((op & 0xf8) == 0x60) {
    		// yes
    		pc++;
    		byte descr = domain.flatReadByte(pc++);
    		
    		InstructionMFP201 subInst = decodeInstruction(pc, domain);
    		int countR = descr & 0xf;
			if (countR == 0xf) {
    			inst.setInst(_IfirstStepInst + (op & 0x7) * 2);
    			inst.setName(getInstName(inst.getInst()));
    			inst.setOp1(new MachineOperandMFP201Inst(subInst));
    		} else {
	    		inst.setInst(_IfirstLoopInst + (op & 0x7) * 2);
	    		inst.setName(getInstName(inst.getInst()));
	    		inst.setOp1(MachineOperandMFP201.createRegisterOperand(countR));
	    		inst.setOp2(new MachineOperandMFP201Inst(subInst));
	    		((MachineOperandMFP201) inst.getOp1()).dest = OP_DEST_TRUE;
    		}
			
			// apply decrement bits if enabled
			MachineOperandMFP201 mop;
			if ((descr & 0x20) != 0) { 
				mop = subInst.getOp(getFirstSrcOp(subInst));
				if (mop != null && mop.type == OP_INC)
					mop.type = OP_DEC;
			}
			if ((descr & 0x10) != 0) {
				mop = subInst.getOp(getDestOp(subInst));
				if (mop != null && mop.type == OP_INC)
					mop.type = OP_DEC;
			}
			
			InstInfo info = inst.getInfo();
			
			InstInfo subinfo = subInst.getInfo();
			info.stReads |= subinfo.stReads;
			info.stWrites |= subinfo.stWrites;
			info.stsetAfter |= subinfo.stsetAfter;
			info.stsetBefore |= subinfo.stsetBefore;
			info.reads |= subinfo.reads;
			info.writes |= subinfo.writes;
			
			info.cycles = (short) (2 + subinfo.cycles - 1);		// per iteration

			pc += subInst.getSize();
    	} else {
    		pc = decodeInstruction(pc, op, inst, domain);
    	}
    	
    	inst.byteop = isByteInst(inst.getInst());
    	
    	inst.opcode = 0;
	    for (int i = inst.pc; i < pc; i++) {
	    	inst.opcode = (inst.opcode << 8) | (domain.flatReadByte(i) & 0xff);
	    }
    	
	    inst.setSize(pc - inst.pc);
	    
        return inst;
    }

	private static int decodeInstruction(int pc, int op, InstructionMFP201 inst,
			MemoryDomain domain) {
		
		InstInfo info = inst.getInfo();

		// mem/size byte?
		int As = 0, Ad = 0;
		boolean isByte = false;
		
		byte memSize = 0;
		if ((op & 0xe0) == 0x40) {
			pc++;
			memSize = (byte) op;
			As = (op & 0xc) >> 2;
			Ad = (op & 0x3);
			isByte = (op & 0x10) != 0;
			
			info.cycles++;
		}
		
		// get opcode
		op = domain.flatReadByte(pc++) & 0xff;
		
		info.cycles++;
		
		// tell which operand the bits apply to
		int AsOp = 0, AdOp = 0;
		
		// simple instructions
		if (op < 0x8) {
			switch (op) {
			case 0:
				inst.setInst(Ibkpt);
				info.cycles += 1;
				break;
			case 4:
				inst.setInst(Ibr);
				inst.setOp1(MachineOperandMFP201.createPCRelativeOperand(read16(pc, domain) + 3));
				pc += 2;
				info.cycles += 1;
				As = 0;
				AsOp = 1;
				break;
			case 5:
				inst.setInst(Ibra); 
				inst.setOp1(MachineOperandMFP201.createImmediate(read16(pc, domain)));
				pc += 2;
				info.cycles += 1;
				AsOp = 1;
				As = 0;
				break;
			case 6:
				inst.setInst(Icall); 
				inst.setOp1(MachineOperandMFP201.createPCRelativeOperand(read16(pc, domain) + 3));
				pc += 2;
				info.cycles += 2;
				AsOp = 1;
				As = 0;
				break;
			case 7:
				if (As == 0) {
					inst.setInst(Icalla); 
					inst.setOp1(MachineOperandMFP201.createImmediate(read16(pc, domain)));
					pc += 2;
				} else {
					inst.setInst(Icall); 
					byte descr = domain.flatReadByte(pc++);
					inst.setOp1(MachineOperandMFP201.createRegisterOperand(descr & 0xf));
				}
				info.cycles += 2;
				AsOp = 1;
				break;
			}
		}
		
		// immediate instructions
		else if (op < 0x10) {
			byte descr = domain.flatReadByte(pc++);
			inst.setInst(Ior + (op - 0x8) * 4);
			if (inst.getInst() == Isbb)
				inst.setInst(Ildc);
			Pair<Integer, Integer> inf = readImm3_16(descr, pc, domain);
			info.cycles += (inf.first - pc) / 2;	// for extra words
			pc = inf.first;
			inst.setOp1(MachineOperandMFP201.createImmediate(inf.second));
			inst.setOp2(MachineOperandMFP201.createRegisterOperand(descr & 0xf));
			
			AsOp = 1;
			As = 0;
			AdOp = 2;
			
			info.cycles += 1;
		}
		
		// 1-op instructions 
		else if (op >= 0x10 && op < 0x40) {
			inst.setOp1(MachineOperandMFP201.createRegisterOperand(op & 0xf));
			AdOp = 1;
			switch (op & 0x30) {
			case 0x10:
				// sext/etc or LEA
				if (!isByte) {
					inst.setInst(Isext + As * 2);
					As = 0;
					info.cycles += 1;
				} else {
					
					inst.setInst(Ilea);
					inst.setOp2(inst.getOp1());

					AdOp = 2;
					
					As = 0;
					Ad = 0;
					
					int scaleBits = (memSize & 0x7);
					boolean isImm8 = (memSize & 0x8) != 0;
					byte descr = domain.flatReadByte(pc++);
					int srcR = descr & 0xf;
					int addR = (descr >> 4) & 0xf;
					int immed = domain.flatReadByte(pc++);
					if (!isImm8) {
						immed = (immed << 8) | (domain.flatReadByte(pc++) & 0xff);
					}
					inst.setOp1(MachineOperandMFP201.createScaledRegOffsOperand(
							immed, addR, srcR, 1 << scaleBits));

					((MachineOperandMFP201) inst.getOp1()).bIsReference = true;
					
					info.cycles++;
					// most cycles added when resolving operand
				}
				break;
			case 0x20:
			case 0x30:
				// push 
				inst.setInst(((op & 0x30) == 0x20 ? Ipush : Ipop) + (isByte ? 1 : 0));
				info.cycles += 1;
				AdOp = 1;
				if (As != 0) {
					AdOp = 2;
					inst.setInst(inst.getInst() - Ipush + Ipushn);
					inst.setOp2(inst.getOp1());
					inst.setOp1(MachineOperandMFP201.createGeneralOperand(OP_CNT, As + 1));
					info.cycles += As;
					As = 0;
					((MachineOperandMFP201) inst.getOp2()).bIsReference = true;
				}
				break;
			}
		}
		
		// 2-op groups (shifts/mul/div)
		else if (op >= 0x68 && op < 0x70) {
			inst.setInst(Ilsh + (op - 0x68) * 2 + (isByte ? 1 : 0));
			
			byte descr = domain.flatReadByte(pc++);
			inst.setOp2(MachineOperandMFP201.createRegisterOperand(descr & 0xf));
			
			AsOp = 1;
			AdOp = 2;
			
			int srcCnt = (descr & 0xf0) >> 4;
			if (op < 0x6c) {
				// shift
				inst.setOp1(MachineOperandMFP201.createGeneralOperand(
						srcCnt == 0 ? OP_REG0_SHIFT_COUNT : OP_CNT, srcCnt));
				
				if (srcCnt == 0)
					info.cycles += 2;
				info.cycles += 2;
				
				As = 0;
			} else {
				// mul/div
				inst.setOp1(MachineOperandMFP201.createRegisterOperand(srcCnt));
				AsOp = 1;
				
				if (inst.getInst() >= Imul && inst.getInst() <= Imulb)
					info.cycles += 2;
				else if (inst.getInst() >= Imuld && inst.getInst() <= Imuldb)
					info.cycles += 4;
				else if (inst.getInst() >= Idiv && inst.getInst() <= Idivb)
					info.cycles += 8;
				else 
					info.cycles += 16;
			}
		}
		
		// jumps
		else if (op >= 0x70 && op < 0x78) {
			inst.setInst(_IfirstJumpOp + (op & 0x7) * 2);
			Pair<Integer, Integer> pair = readJumpOffset(memSize, pc, domain);
			int immed = pair.first + pair.second - inst.pc;
			info.cycles += (pair.first - pc) / 2;	// for extra words
			inst.setOp1(MachineOperandMFP201.createPCRelativeOperand((short) (immed)));
			pc = pair.first;

			info.cycles++;
			
			As = Ad = 0;
			AsOp = 1;
			// additional cycle added if jump taken, while executing
		}
		
		// movcc
		else if (op >= 0x78 && op < 0x80) {
			inst.setInst(_IfirstMovOp + (op & 0x7) * 2 + (isByte ? 1 : 0));
			byte descr = domain.flatReadByte(pc++);
			inst.setOp1(MachineOperandMFP201.createRegisterOperand((descr & 0xf0) >> 4));
			inst.setOp2(MachineOperandMFP201.createRegisterOperand(descr & 0xf));
			
			info.cycles += 2;
			
			AsOp = 1;
			AdOp = 2;
		}
		
		// 3-ops
		else if (op >= 0x80) {
			inst.setInst(_IfirstPossibleThreeOp + (((op & 0xf0) - 0x80) >> 4) * 4 + (isByte ? 1 : 0));
			byte descr = domain.flatReadByte(pc++);
			
			info.cycles += 2;
			
			int src1R = op & 0xf;
			if (isArithOpInst(inst.getInst()) && src1R == SR) {
				inst.setOp1(MachineOperandMFP201.createImplicitConstantReg(SR, 0));
			} else {
				inst.setOp1(MachineOperandMFP201.createRegisterOperand(src1R));
			}
	
			int[] consts = isArithOpInst(inst.getInst()) ? ARITHMETIC_INST_CONSTANTS : LOGICAL_INST_CONSTANTS[isByte ? 1 : 0];
			int src2R = (descr & 0xf0) >> 4;
			switch (src2R) {
			case 13:
			case 14:
			case 15:
				inst.setOp2(MachineOperandMFP201.createImplicitConstantReg(src2R, consts[src2R - 13]));
				break;
			default:
				inst.setOp2(MachineOperandMFP201.createRegisterOperand(src2R));
				break;
			}
			
			AsOp = 1;
			AdOp = 3;
	
			int destR = descr & 0xf;
			inst.setOp3(MachineOperandMFP201.createRegisterOperand(destR));
			if (destR == SR) {
				if (isArithOpInst(inst.getInst())) {
					switch (inst.getInst()) {
					case Iadd:
					case Iaddb:
						inst.setInst(inst.getInst() - Iadd + Itst);
						break;
					case Iadc:
					case Iadcb:
						inst.setInst(inst.getInst() - Iadc + Itstn);
						break;
					case Isub:
					case Isubb:
						inst.setInst(inst.getInst() - Isub + Icmp);
						break;
					case Isbb:
					case Isbbb:
						inst.setInst(inst.getInst() - Isbb + Icmpr);
						break;
					}
					AdOp = 2;
				} else {
					// make non-writing inst
					if (Ad == 0)
						inst.setInst(inst.getInst() + 2);
					else
						AdOp = 2;
				}
				inst.setOp3(MachineOperandMFP201.createNonWritingSROperand());
			}
			else {
				info.cycles++;
			}
		}
		
		if (inst.getInst() == 0) {
			if (memSize != 0) {
				inst.setInst(InstTableCommon.Idata);
				int immed = (memSize << 8) | (op & 0xff);
				inst.setOp1(MachineOperandMFP201.createImmediate(immed));
			} else {
				inst.setInst(InstTableCommon.Ibyte);
				inst.setOp1(MachineOperandMFP201.createImmediate(op));
				
			}
		} else {
			pc = updateOperandAndCycles(inst, true, AsOp, As, pc, domain);
			if (AdOp == 3)
				pc = updateOperandAndCycles(inst, true, 2, 0, pc, domain);
			pc = updateOperandAndCycles(inst, isPushInst(inst.getInst()), AdOp, Ad, pc, domain);
		}
		return pc;
	}
	    
	public static boolean isPushInst(int inst) {
		return inst == Ipush || inst == Ipushb || inst == Ipushn || inst == Ipushnb;
	}
	public static boolean isPushNInst(int inst) {
		return inst == Ipushn || inst == Ipushnb;
	}
	public static boolean isPopInst(int inst) {
		return inst == Ipop || inst == Ipopb || inst == Ipopn || inst == Ipopnb;
	}
	public static boolean isPopNInst(int inst) {
		return inst == Ipopn || inst == Ipopnb;
	}
	

	private static int getDestOp(RawInstruction ins) {
		int inst = ins.getInst();
		if (isPossibleThreeOpInst(inst)) {
			if (ins.getOp3() != null)
				return 3;
			else
				return 2;
		}
		else if (isPushInst(inst) || isJumpInst(inst))
			return 0;
		//else if (isPopInst(inst) && ins.getOp2() != null)
		//	return 0;
		else if (ins.getOp2() != null && !isLoopInst(inst))
			return 2;
		else if (ins.getOp1() != null && !isStepInst(inst))
			return 1;
		return 0;
	}

	/**
	 * @param ins
	 * @return
	 */
	private static int getFirstSrcOp(RawInstruction ins) {
		int inst = ins.getInst();
		if (isPushInst(inst))
			return 2;
		if (ins.getOp1() != null)
			return 1;
		return 0;
	}

	/**
	 * @param inst
	 * @param asOp
	 * @param as
	 */
	private static int updateOperandAndCycles(RawInstruction inst, boolean isSrc, int asOp, int as, int pc, MemoryDomain domain) {
		MachineOperandMFP201 mop = inst.getOp(asOp);
		if (mop == null)
			return pc;
		
		if (!isSrc && !isNonWritingInst(inst.getInst())) {
			mop.dest = isKillingInst(inst.getInst()) ? OP_DEST_KILLED : OP_DEST_TRUE;
		}
		
		mop.type += as;
		InstInfo info = inst.getInfo();

		switch (mop.type) {
		case OP_REG:
			break;
		case OP_CNT:
			break;
		case OP_IMM:
			break;
		case OP_INC:
			if (isSrc && mop.val == PC) {
				if (!isByteInst(inst.getInst())) {
					mop.immed = read16(pc, domain);
					pc += 2;
					mop.encoding = OP_ENC_IMM16;
				} else {
					mop.immed = domain.flatReadByte(pc++);
					mop.encoding = OP_ENC_IMM8;
				}
				mop.type = OP_IMM;
				info.cycles++;
			}
			info.cycles++;
			break;
		case OP_OFFS:
			mop.immed = read16(pc, domain);
			pc += 2;
			info.cycles++;
			break;
		case OP_DEC:
			info.cycles++;
			break;
		case OP_SRO:
			info.cycles++;	// offs
			if (mop.val != SR)
				info.cycles++;
			if (mop.scaleReg != SR)
				info.cycles++;
			break;
		}	
		return pc;
	}

	/**
	 * @param inst
	 * @return
	 */
	public static boolean isKillingInst(int inst) {
		return isLoadConstInst(inst)
		 || isMoveInst(inst)
		 || isPopInst(inst);
	}

	/**
	 * @param op
	 * @param pc
	 * @param domain
	 * @return
	 */
	private static Pair<Integer, Integer> readJumpOffset(byte memSize, int pc, MemoryDomain domain) {
		int immed = domain.flatReadByte(pc++);
		
		if (memSize != 0) {
			immed &= 0xff;
			int bits8_11 = ((byte)(memSize << 4) << 4);
			immed |= bits8_11;
			if ((memSize & 0x10) != 0) {
				immed = (immed & 0xfff) | (domain.flatReadByte(pc++) << 12);
			}
		}
		return new Pair<Integer, Integer>(pc, immed);
	}

	/**
	 * @param pc
	 * @param domain
	 * @return
	 */
	private static Pair<Integer, Integer> readImm3_16(byte op, int pc, MemoryDomain domain) {
		int immed;
        if ((op & 0x80) == 0) {
        	immed = ((byte) (op << 1)) >> 5; 
        } else {
        	immed = (op & 0x70) >> 4;
        	op = domain.flatReadByte(pc++);
        	if ((op & 0x80) == 0) {
        		immed |= (op & 0x7f) << 3;
        		immed = ((short)(immed << 6) >> 6); 
        	} else {
        		immed |= (op & 0x7f) << 3;
        		op = domain.flatReadByte(pc++);
        		immed |= (op & 0x3f) << 10;
        	}
        }
		return new Pair<Integer, Integer>(pc, immed);
	}

	private static short read16(int pc, MemoryDomain domain) {
		return (short) ((domain.flatReadByte(pc) << 8) | (domain.flatReadByte(pc + 1) & 0xff));
	}

	/*
    public static void calculateInstructionSize(RawInstruction target) {
    	if (target.getInst() == InstTableCommon.Idata) {
    		target.setSize(2);
    		return;
    	} else if (target.getInst() == InstTableCommon.Ibyte) {
    		target.setSize(1);
    		return;
    	}
    	target.setSize(0);
    	InstPatternMFP201 pattern = lookupEncodePattern(target.getInst());
		if (pattern == null)
			return;
		
		byte[] bytes = encode(target);
		target.setSize(target.getSize() + bytes.length);
    }
    */
	
	public static boolean isJumpInst(int inst) {
		return inst >= _IfirstJumpOp && inst <= _IlastJumpOp;
	}
	
	public static boolean isMoveInst(int inst) {
		return inst >= _IfirstMovOp && inst <= _IlastMovOp;
	}
	
	public static boolean isByteInst(int inst) {
		return (inst & 1) != 0 && canBeByteInst(inst - 1);
	}

	
	public static boolean canBeByteInst(int inst) {
		return (inst >= _IfirstPossibleByteOp && inst <= _IlastPossibleByteOp)
		|| (inst >= _IfirstPossibleByteOp2 && inst <= _IlastPossibleByteOp2)
		|| (inst >= _IfirstPossibleByteOp3 && inst <= _IlastPossibleByteOp3);
	}

	public static boolean canBeThreeOpInst(int inst) {
		return inst >= _IfirstPossibleThreeOp 
			&& inst <= _IlastPossibleThreeOp;
	}
	public static boolean isLogicalOpInst(int inst) {
		return inst >= _IfirstLogicalOp 
			&& inst <= _IlastLogicalOp;
	}
	public static boolean isLoadConstInst(int inst) {
		return inst == Ildc ;
	}
	
	public static boolean isNonWritingInst(int inst) {
		if (canBeThreeOpInst(inst) || isLoadConstInst(inst)) {
			return (inst & 2) != 0;
		}
		else {
			return false;
		}
	}

	/**
	 * @param inst
	 * @return
	 */
	public static boolean isCommutativeInst(int inst) {
		if (canBeThreeOpInst(inst)) {
			inst &= ~3;
			if (inst == Iadd 
					|| inst == Iand
					|| inst == Ior
					|| inst == Ixor
					|| inst == Inand) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param inst
	 * @return
	 */
	public static int getReversedInst(int inst) {
		if (inst >= Icmp && inst <= Icmpb)
			return inst - Icmp + Icmpr;
		if (inst >= Icmpr && inst <= Icmprb)
			return inst - Icmpr + Icmp;
		if (isCommutativeInst(inst))
			return inst;
		return 0;
	}

	public static boolean isArithOpInst(int inst) {
		return inst >= _IfirstArithOp 
		&& inst <= _IlastArithOp;
	}

	public static boolean isLoopOrStepInst(int inst) {
		return inst >= _IfirstLoopStepInst
		&& inst <= _IlastLoopStepInst;
	}
	public static boolean isLoopInst(int inst) {
		return inst >= _IfirstLoopInst
		&& inst <= _IlastLoopInst;
	}
	public static boolean isStepInst(int inst) {
		return inst >= _IfirstStepInst
		&& inst <= _IlastStepInst;
	}
	public static boolean isPossibleThreeOpInst(int inst) {
		return inst >= _IfirstPossibleThreeOp && inst <= _IlastPossibleThreeOp;
	}
}
