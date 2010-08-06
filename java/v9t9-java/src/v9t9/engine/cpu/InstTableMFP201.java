/**
 * 
 */
package v9t9.engine.cpu;

import static v9t9.engine.cpu.InstPatternMFP201.*;
import static v9t9.engine.cpu.InstMFP201.*;
import static v9t9.engine.cpu.MachineOperandMFP201.*;

import java.util.HashMap;
import java.util.Map;

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
		"s",
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
	/** if the immediate size is a byte, at the bit specified in lower nybble */
	private final static byte _IMM_SZ = 0x30;
	/** if the immediate size is a byte, at the bit specified in lower nybble, of byte 0 */
	private final static byte _IMM_SZ_0 = 0x40;
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
	final static InstPatternMFP201 OFF_ = new InstPatternMFP201(
			OFF, new byte[] { _OPC, _NEXTO, _OFF16 });
	final static InstPatternMFP201 JMP_ = new InstPatternMFP201(
			OFF, new byte[] { _OPC, _JMP });
	final static InstPatternMFP201 IMMx_REG = new InstPatternMFP201(
			IMM, REG, new byte[] { _OPC, _IMM_SZ_0 | 0, _IMM8_16, _NEXTO, 0 });
	final static InstPatternMFP201 IMM8_REG = new InstPatternMFP201(
			IMM, REG, new byte[] { _OPC, _IMM8, _NEXTO, 0 });
	final static InstPatternMFP201 IMM16_REG = new InstPatternMFP201(
			IMM, REG, new byte[] { _OPC, _IMM16, _NEXTO, 0 });
	
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
			return;
		else 
			inst += 2;
		
		register(inst, opcode, GEN_REG_GEN, mask);
		register(inst + 1, opcode, GEN_REG_GEN, mask);
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
		register(Ibr, 0xc, OFF_, 0xc);
		register(Ibra, 0xd, IMM_, 0xd);
		register(Icall, 0xe, OFF_, 0xe);
		register(Icalla, 0xf, IMM_, 0xf);
		
		/* Register + immediate versions */
		register(Ior, 0x800, IMMx_REG, 0x90f);
		register(Iorb, 0x900, IMM8_REG, 0x90f);
		register(Iorq, 0x810, IMMx_REG, 0x91f);
		register(Iorbq, 0x910, IMM8_REG, 0x91f);
		
		register(Iand, 0x820, IMMx_REG, 0x92f);
		register(Iandb, 0x920, IMM8_REG, 0x92f);
		register(Itst, 0x830, IMMx_REG, 0x93f);
		register(Itstb, 0x930, IMM8_REG, 0x93f);
		
		register(Inand, 0x840, IMMx_REG, 0x94f);
		register(Inandb, 0x940, IMM8_REG, 0x94f);
		register(Itstn, 0x850, IMMx_REG, 0x95f);
		register(Itstnb, 0x950, IMM8_REG, 0x95f);

		register(Ixor, 0x860, IMMx_REG, 0x96f);
		register(Ixorb, 0x960, IMM8_REG, 0x96f);
		register(Ixorq, 0x870, IMMx_REG, 0x97f);
		register(Ixorbq, 0x970, IMM8_REG, 0x97f);
		
		register(Iadd, 0x880, IMMx_REG, 0x98f);
		register(Iaddb, 0x980, IMM8_REG, 0x98f);
		register(Iaddq, 0x890, IMMx_REG, 0x99f);
		register(Iaddbq, 0x990, IMM8_REG, 0x99f);

		register(Isub, 0x8a0, IMMx_REG, 0x9af);
		register(Isubb, 0x9a0, IMM8_REG, 0x9af);
		register(Icmp, 0x8b0, IMMx_REG, 0x9bf);
		register(Icmpb, 0x9b0, IMM8_REG, 0x9bf);

		register(Iadc, 0x8c0, IMMx_REG, 0x9cf);
		register(Iadcb, 0x9c0, IMM8_REG, 0x9cf);
		register(Iadcq, 0x8d0, IMMx_REG, 0x9df);
		register(Iadcbq, 0x9d0, IMM8_REG, 0x9df);
		
		/* Only immediate version available */
		register(Ildc, 0x8e0, IMMx_REG, 0x9ef);
		register(Ildcb, 0x9e0, IMM8_REG, 0x9ef);
		register(Ildcq, 0x8f0, IMMx_REG, 0x9ff);
		register(Ildcbq, 0x9f0, IMM8_REG, 0x9ff);
		
		/* simple one-ops */
		register(Isext, 0x10, GEN_, 0x1f);
		register(Iextl, 0x10, AS1_GEN_, 0x1f);
		register(Iexth, 0x10, AS2_GEN_, 0x1f);
		register(Iswpb, 0x10, AS3_GEN_, 0x1f);
		
		register2(Ipush, 0x20, GEN_, 0x2f);
		register2(Ipush, 0x20, CNT4M1_GEN, 0x2f);
		register2(Ipop, 0x30, GEN_, 0x3f);
		register2(Ipop, 0x30, CNT4M1_GEN, 0x3f);

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
		register(Ijs, 0x74, JMP_, 0x74);
		register(Ijge, 0x75, JMP_, 0x75);
		register(Ijl, 0x76, JMP_, 0x76);
		register(Ijmp, 0x77, JMP_, 0x77);
		
		/* moves */
		register2(Imovne, 0x78, GEN_GEN, 0x78);
		register2(Imoveq, 0x79, GEN_GEN, 0x79);
		register2(Imovnc, 0x7a, GEN_GEN, 0x7a);
		register2(Imovc, 0x7b, GEN_GEN, 0x7b);
		register2(Imovs, 0x7c, GEN_GEN, 0x7c);
		register2(Imovge, 0x7d, GEN_GEN, 0x7d);
		register2(Imovl, 0x7e, GEN_GEN, 0x7e);
		register2(Imov, 0x7f, GEN_GEN, 0x7f);
		
		/* lea! */
		register(Ilea, 0x5010, SFO_REG, 0x5f1f);
		
		register(Iloopne, 0x60, REG_INST, 0x60);
		register(Iloopeq, 0x61, REG_INST, 0x61);
		register(Iloopnc, 0x62, REG_INST, 0x62);
		register(Iloopc, 0x63, REG_INST, 0x63);
		register(Iloops, 0x64, REG_INST, 0x64);
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
	registerInstruction(Ipushb, "push.b");
	registerInstruction(Ipop, "pop");
	registerInstruction(Ipopb, "pop.b");
	
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
	registerInstruction(Iaddq, "add?");
	registerInstruction(Iaddbq, "add.b?");
	
	registerInstruction(Isub, "sub");
	registerInstruction(Isubb, "sub.b");
	registerInstruction(Icmp, "cmp");
	registerInstruction(Icmpb, "cmp.b");
	
	registerInstruction(Iadc, "adc");
	registerInstruction(Iadcb, "adc.b");
	registerInstruction(Iadcq, "adc?");
	registerInstruction(Iadcbq, "adc.b?");
	
	registerInstruction(Isbb, "sbb");
	registerInstruction(Isbbb, "sbb.b");
	registerInstruction(Icmpr, "cmpr");
	registerInstruction(Icmprb, "cmpr.b");
	

	registerInstruction(Ildc, "ldc");
	registerInstruction(Ildcb, "ldc.b");
	registerInstruction(Ildcq, "ldc?");
	registerInstruction(Ildcbq, "ldc.b?");
	
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
				Ipop, new LLImmedOperand(2), new LLRegisterOperand(PC), null);
		
		registerPseudoByte(Pclr, "clr", 1, 
				Ixor, P_OP1, P_OP1, P_OP1);
		registerPseudoByte(Pseto, "seto", 1, 
				Isub, new LLImmedOperand(0), new LLImmedOperand(1), P_OP1);
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
		
		coerceOperandTypes(rawInst);
		
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
							int bit = mopIdx == 1 && (pattern.op2 == InstPatternMFP201.GEN || pattern.op3 == InstPatternMFP201.GEN)
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
				else if (enc == _IMM8_16) {
					if (!isImm8(mop.immed))
						immeds[immIdx++] = (byte) ((mop.immed & 0xff00) >> 8);
					immeds[immIdx++] = (byte) (mop.immed & 0xff);
				}
				else if (enc == _OFF16) {
					int val;
					if (mop.type == OP_PCREL) {
						val = mop.val - workIdx - immIdx - 1;
					} else {
						val = mop.immed - workIdx - immIdx - 1 - rawInst.pc;
					}
					immeds[immIdx++] = (byte) ((val & 0xff00) >> 8); 
					immeds[immIdx++] = (byte) (val & 0xff); 
				}
				else if (enc == _JMP) {
					int val;
					if (mop.type == OP_PCREL) {
						val = mop.val - workIdx - immIdx - 1;
					} else {
						val = mop.immed - workIdx - immIdx - 1 - rawInst.pc;
					}
					
					if (mop.encoding == OP_ENC_PCREL8
							|| isImm8((short) val)) {
						immeds[immIdx++] = (byte) (val & 0xff);
					}
					else if (mop.encoding == OP_ENC_PCREL12
							|| isJumpImm12(val)) {
						immeds[immIdx++] = (byte) (val & 0xff);
						loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE 
							| (val >> 8) & 0xf;
					}
					else {
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
					InstPatternMFP201 subpattern = getInstPattern(subInst);
					MachineOperandMFP201 subop;
					int bit = 5;
					if (subpattern.op1 == GEN) {
						subop = (MachineOperandMFP201) subInst.getOp1();
						if (subop != null && subop.type == OP_DEC)
							work[1] |= 1 << bit;
						bit--;
					}
					if (subpattern.op2 == GEN) {
						subop = (MachineOperandMFP201) subInst.getOp2();
						if (subop != null && subop.type == OP_DEC)
							work[1] |= 1 << bit;
						bit--;
					}
					if (subpattern.op3 == GEN) {
						subop = (MachineOperandMFP201) subInst.getOp3();
						if (subop != null && subop.type == OP_DEC)
							work[1] |= 1 << bit;
						bit--;
					}
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
				else if ((enc & 0xf0) == _IMM_SZ_0) {	
					if (isImm8(mop.immed))
						work[0] |= (byte) (1 << (enc & 0xf));
				}
				else if ((enc & 0xf0) == _AS_CONST) {	
					loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE | (enc & 0xf) << 2;
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
		return (inst >= Ior && inst <= Ildcbq);
	}

	public static boolean isImm8(short immed) {
		return (byte) (immed & 0xff) == immed;
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
			
			if ((mop3 == null && pattern.length >= 3) || (mop3 != null && pattern.length < 3))
				continue;
			if ((mop2 == null && pattern.length >= 2) || (mop2 != null && pattern.length < 2))
				continue;
			if ((mop1 == null && pattern.length >= 1) || (mop1 != null && pattern.length < 1))
				continue;
			
			mop1 = coerceOperandType(instruction, mop1, pattern.op1);
			mop2 = coerceOperandType(instruction, mop2, pattern.op2);
			mop3 = coerceOperandType(instruction, mop3, pattern.op3);
			
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
	
	private static MachineOperandMFP201 coerceOperandType(RawInstruction instruction, MachineOperandMFP201 mop, int op) {
		if (op == NONE)
			return mop;
		
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
			if (mop.type == OP_IMM
					&& !isDestOp(instruction, mop)) {
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

	/**
	 * @param ins
	 * @param mop
	 * @return
	 */
	private static boolean isDestOp(RawInstruction ins,
			MachineOperandMFP201 mop) {
		if (ins.getOp3() != null)
			return mop == ins.getOp3();
		if (ins.getOp2() != null)
			return mop == ins.getOp2();
		return mop == ins.getOp1() 
			&& ins.getInst() != Ipush
			&& ins.getInst() != Ipushb;
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
				&& ((instruction.getInst() != Ipush
					&& instruction.getInst() != Ipop)
					|| instruction.getOp2() == null
					|| ((MachineOperandMFP201)instruction.getOp2()).type  == OP_NONE);
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
	/*
    public static RawInstruction decodeInstruction(int op, int pc, MemoryDomain domain) {
    	RawInstruction inst = new RawInstruction();
    	inst.pc = pc;
    	
        // deal with it unsigned 
        op &= 0xffff;
    
        inst.opcode = (short) op;
        inst.setInst(InstTableCommon.Idata);
        inst.setName("DATA");
        inst.size = 0;
        MachineOperandMFP201 mop1 = new MachineOperandMFP201(MachineOperand.OP_NONE);
        MachineOperandMFP201 mop2 = new MachineOperandMFP201(MachineOperand.OP_NONE);
        inst.setOp1(mop1);
        inst.setOp2(mop2);
    
        // Collect the instruction name
        // and operand structure.
    
        // Initially, inst.op?.val is incomplete, and is whatever
        // raw data from the opcode we can decode;
        // inst.op?.ea is that of the instruction or immediate
        // if the operand needs it.
    
        // after decoding the instruction, we complete
        // the operand, making inst.op?.val and inst.op?.ea valid.
    
        if (op < 0x200) {
        } else if (op < 0x2a0) {
            mop1.type = OP_REG;
            mop1.val = op & 15;
            mop2.type = OP_IMM;
            switch ((op & 0x1e0) >> 5) {
            case 0:
                //inst.name = "LI";
                inst.setInst(Ili);
                break;
            case 1:
                //inst.name = "AI";
                inst.setInst(Iai);
                break;
            case 2:
                //inst.name = "ANDI";
                inst.setInst(Iandi);
                break;
            case 3:
                //inst.name = "ORI";
                inst.setInst(Iori);
                break;
            case 4:
                //inst.name = "CI";
                inst.setInst(Ici);
                break;
            }
    
        } else if (op < 0x2e0) {
            mop1.type = OP_REG;
            mop1.val = op & 15;
            switch ((op & 0x1e0) >> 5) {
            case 5:
                //inst.name = "STWP";
                inst.setInst(Istwp);
                break;
            case 6:
                //inst.name = "STST";
                inst.setInst(Istst);
                break;
            }
    
        } else if (op < 0x320) {
            mop1.type = OP_IMM;
    
            switch ((op & 0x1e0) >> 5) {
            case 7:
                //inst.name = "LWPI";
                inst.setInst(Ilwpi);
                break;
            case 8:
                //inst.name = "LIMI";
                inst.setInst(Ilimi);
                break;
            }
    
        } else if (op < 0x400) {
            switch ((op & 0x1e0) >> 5) {
            case 10:
                //inst.name = "IDLE";
                inst.setInst(Iidle);
                break;
            case 11:
                //inst.name = "RSET";
                inst.setInst(Irset);
                break;
            case 12:
                //inst.name = "RTWP";
                inst.setInst(Irtwp);
                break;
            case 13:
                //inst.name = "CKON";
                inst.setInst(Ickon);
                break;
            case 14:
                //inst.name = "CKOF";
                inst.setInst(Ickof);
                break;
            case 15:
                //inst.name = "LREX";
                inst.setInst(Ilrex);
                break;
            }
    
        } else if (op < 0x800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
    
            switch ((op & 0x3c0) >> 6) {
            case 0:
                //inst.name = "BLWP";
                inst.setInst(Iblwp);
                break;
            case 1:
                //inst.name = "B";
                inst.setInst(Ib);
                break;
            case 2:
                //inst.name = "X";
                inst.setInst(Ix);
                break;
            case 3:
                //inst.name = "CLR";
                inst.setInst(Iclr);
                break;
            case 4:
                //inst.name = "NEG";
                inst.setInst(Ineg);
                break;
            case 5:
                //inst.name = "INV";
                inst.setInst(Iinv);
                break;
            case 6:
                //inst.name = "INC";
                inst.setInst(Iinc);
                break;
            case 7:
                //inst.name = "INCT";
                inst.setInst(Iinct);
                break;
            case 8:
                //inst.name = "DEC";
                inst.setInst(Idec);
                break;
            case 9:
                //inst.name = "DECT";
                inst.setInst(Idect);
                break;
            case 10:
                //inst.name = "BL";
                inst.setInst(Ibl);
                break;
            case 11:
                //inst.name = "SWPB";
                inst.setInst(Iswpb);
                break;
            case 12:
                //inst.name = "SETO";
                inst.setInst(Iseto);
                break;
            case 13:
                //inst.name = "ABS";
                inst.setInst(Iabs);
                break;
            }
    
        } else if (op < 0xc00) {
            mop1.type = OP_REG;
            mop1.val = op & 15;
            mop2.type = OP_CNT;
            mop2.val = (op & 0xf0) >> 4;
    
            switch ((op & 0x700) >> 8) {
            case 0:
                //inst.name = "SRA";
                inst.setInst(Isra);
                break;
            case 1:
                //inst.name = "SRL";
                inst.setInst(Isrl);
                break;
            case 2:
                //inst.name = "SLA";
                inst.setInst(Isla);
                break;
            case 3:
                //inst.name = "SRC";
                inst.setInst(Isrc);
                break;
            }
    
        } else if (op < 0x1000) {
}    
        if (inst.getInst() == 0) // data
        {
            mop1.type = OP_IMM;
            mop1.val = mop1.immed = (short) op;
            //inst.name = "DATA";
            inst.size = 2;
        } else {
        	// inst.completeInstruction(pc);
            // Finish reading operand immediates
            pc += 2;
            pc = mop1.fetchOperandImmediates(domain, (short)pc);
            pc = mop2.fetchOperandImmediates(domain, (short)pc);
            inst.size = pc - inst.pc;
            inst.setName(InstTableMFP201.getInstName(inst.getInst()));
        }

        return inst;
    }
    */
    
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
		return inst >= Ildc && inst <= Ildcbq;
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
}
