/**
 * 
 */
package v9t9.engine.cpu;

import static v9t9.engine.cpu.InstPatternMFP201.CNT;
import static v9t9.engine.cpu.InstPatternMFP201.GEN;
import static v9t9.engine.cpu.InstPatternMFP201.NONE;
import static v9t9.engine.cpu.InstPatternMFP201.OFF;
import static v9t9.engine.cpu.InstPatternMFP201.IMM;
import static v9t9.engine.cpu.InstPatternMFP201.REG;
import static v9t9.engine.cpu.InstPatternMFP201.SRO;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ejs
 * 
 */
public class InstTableMFP201 {
	public final static byte _IMM8 = 0x10;
	public final static byte _IMM16 = 0x11;
	public final static byte _IMM8_16 = 0x12;
	public final static byte _OFF16 = 0x13;
	public final static byte _JMP = 0x14;
	/** if the immediate size is a byte, at the bit specified in lower nybble */
	public final static byte _IMM_SZ = 0x30;
	/** if the immediate size is a byte, at the bit specified in lower nybble, of byte 0 */
	public final static byte _IMM_SZ_0 = 0x40;
	/** constant encoded into As bits, low nybble */
	public final static byte _AS_CONST = 0x50;
	/** skip to the next byte */
	public final static byte _NEXTB = -2;
	/** skip to the next operand */
	public final static byte _NEXTO = -3;
	/** skip to the next byte and operand */
	public final static byte _NEXT = -4;
	/** emit the opcode */
	public final static byte _OPC = -1;
	/** emit the opcode (high byte) */
	public final static byte _OPC_HI = -5;
	/** emit the opcode (low byte) */
	public final static byte _OPC_LO = -6;
	/** immediate encoded into As bits */
	public final static byte _IMM_AS = -8;
	/** immediate minus one encoded into As bits */
	public final static byte _IMM_AS_M1 = -9;
	/** LEA's complicated mode */
	public final static byte _LEA = -10;
	
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
	final static InstPatternMFP201 IMM4M1_GEN = new InstPatternMFP201(
			IMM, GEN,  
			new byte[] { _IMM_AS_M1, _OPC, _NEXTO, 0 });
	
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
		instMasks.put(inst, mask);
	}
	
	private static void register2(int inst, int opcode,InstPatternMFP201 entry,  int mask) {
		register(inst, opcode, entry, mask);
		register(inst + 1, opcode, entry, mask);
	}
	
	private static void register4(int inst, int opcode, int mask) {
		register(inst, opcode, GEN_REG_GEN, mask);
		register(inst + 1, opcode, GEN_REG_GEN, mask);
		
		// in 3-op form, non-writing ADD->TST, ADC->TSTN
		if (inst == InstMFP201.Iadd) 
			inst = InstMFP201.Itst;
		else if (inst == InstMFP201.Iadc) 
			inst = InstMFP201.Itstn;
		else if (inst == InstMFP201.Iand || inst == InstMFP201.Inand)
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

		register(InstMFP201.Ibkpt, 0x0, NONE_, 0x0);
		register(InstMFP201.Iret, 0x3e, NONE_, 0x3e);
		register(InstMFP201.Ireti, 0x443e, NONE_, 0x443e);
		register(InstMFP201.Ibr, 0xc, OFF_, 0xc);
		register(InstMFP201.Ibra, 0xd, IMM_, 0xd);
		register(InstMFP201.Icall, 0xe, OFF_, 0xe);
		register(InstMFP201.Icalla, 0xf, IMM_, 0xf);
		
		/* Register + immediate versions */
		register(InstMFP201.Ior, 0x800, IMMx_REG, 0x90f);
		register(InstMFP201.Iorb, 0x900, IMM8_REG, 0x90f);
		register(InstMFP201.Iorq, 0x810, IMMx_REG, 0x91f);
		register(InstMFP201.Iorbq, 0x910, IMM8_REG, 0x91f);
		
		register(InstMFP201.Iand, 0x820, IMMx_REG, 0x92f);
		register(InstMFP201.Iandb, 0x920, IMM8_REG, 0x92f);
		register(InstMFP201.Itst, 0x830, IMMx_REG, 0x93f);
		register(InstMFP201.Itstb, 0x930, IMM8_REG, 0x93f);
		
		register(InstMFP201.Inand, 0x840, IMMx_REG, 0x94f);
		register(InstMFP201.Inandb, 0x940, IMM8_REG, 0x94f);
		register(InstMFP201.Itstn, 0x850, IMMx_REG, 0x95f);
		register(InstMFP201.Itstnb, 0x950, IMM8_REG, 0x95f);

		register(InstMFP201.Ixor, 0x860, IMMx_REG, 0x96f);
		register(InstMFP201.Ixorb, 0x960, IMM8_REG, 0x96f);
		register(InstMFP201.Ixorq, 0x870, IMMx_REG, 0x97f);
		register(InstMFP201.Ixorbq, 0x970, IMM8_REG, 0x97f);
		
		register(InstMFP201.Iadd, 0x880, IMMx_REG, 0x98f);
		register(InstMFP201.Iaddb, 0x980, IMM8_REG, 0x98f);
		register(InstMFP201.Iaddq, 0x890, IMMx_REG, 0x99f);
		register(InstMFP201.Iaddbq, 0x990, IMM8_REG, 0x99f);

		register(InstMFP201.Isub, 0x8a0, IMMx_REG, 0x9af);
		register(InstMFP201.Isubb, 0x9a0, IMM8_REG, 0x9af);
		register(InstMFP201.Icmp, 0x8b0, IMMx_REG, 0x9bf);
		register(InstMFP201.Icmpb, 0x9b0, IMM8_REG, 0x9bf);

		register(InstMFP201.Iadc, 0x8c0, IMMx_REG, 0x9cf);
		register(InstMFP201.Iadcb, 0x9c0, IMM8_REG, 0x9cf);
		register(InstMFP201.Iadcq, 0x8d0, IMMx_REG, 0x9df);
		register(InstMFP201.Iadcbq, 0x9d0, IMM8_REG, 0x9df);
		
		/* Only immediate version available */
		register(InstMFP201.Ildc, 0x8e0, IMMx_REG, 0x9ef);
		register(InstMFP201.Ildcb, 0x9e0, IMM8_REG, 0x9ef);
		register(InstMFP201.Ildcq, 0x8f0, IMMx_REG, 0x9ff);
		register(InstMFP201.Ildcbq, 0x9f0, IMM8_REG, 0x9ff);
		
		/* simple one-ops */
		register(InstMFP201.Isext, 0x10, GEN_, 0x1f);
		register(InstMFP201.Iextl, 0x10, AS1_GEN_, 0x1f);
		register(InstMFP201.Iexth, 0x10, AS2_GEN_, 0x1f);
		register(InstMFP201.Iswpb, 0x10, AS3_GEN_, 0x1f);
		
		register2(InstMFP201.Ipush, 0x20, GEN_, 0x2f);
		register2(InstMFP201.Ipush, 0x20, IMM4M1_GEN, 0x2f);
		register2(InstMFP201.Ipop, 0x30, GEN_, 0x3f);
		register2(InstMFP201.Ipop, 0x30, IMM4M1_GEN, 0x3f);

		/* shifts, mul/div */
		register2(InstMFP201.Ilsh, 0x68, CNT_GEN, 0x68);
		register2(InstMFP201.Irsh, 0x69, CNT_GEN, 0x69);
		register2(InstMFP201.Iash, 0x6a, CNT_GEN, 0x6a);
		register2(InstMFP201.Irol, 0x6b, CNT_GEN, 0x6b);
		register2(InstMFP201.Imul, 0x6c, GEN_GEN, 0x6c);
		register2(InstMFP201.Idiv, 0x6d, GEN_GEN, 0x6d);
		register2(InstMFP201.Imuld, 0x6e, GEN_GEN, 0x6e);
		register2(InstMFP201.Idivd, 0x6f, GEN_GEN, 0x6f);

		/* jumps */
		register(InstMFP201.Ijne, 0x70, JMP_, 0x70);
		register(InstMFP201.Ijeq, 0x71, JMP_, 0x71);
		register(InstMFP201.Ijnc, 0x72, JMP_, 0x72);
		register(InstMFP201.Ijc, 0x73, JMP_, 0x73);
		register(InstMFP201.Ijs, 0x74, JMP_, 0x74);
		register(InstMFP201.Ijge, 0x75, JMP_, 0x75);
		register(InstMFP201.Ijl, 0x76, JMP_, 0x76);
		register(InstMFP201.Ijmp, 0x77, JMP_, 0x77);
		
		/* moves */
		register2(InstMFP201.Imovne, 0x78, GEN_GEN, 0x78);
		register2(InstMFP201.Imoveq, 0x79, GEN_GEN, 0x79);
		register2(InstMFP201.Imovnc, 0x7a, GEN_GEN, 0x7a);
		register2(InstMFP201.Imovc, 0x7b, GEN_GEN, 0x7b);
		register2(InstMFP201.Imovs, 0x7c, GEN_GEN, 0x7c);
		register2(InstMFP201.Imovge, 0x7d, GEN_GEN, 0x7d);
		register2(InstMFP201.Imovl, 0x7e, GEN_GEN, 0x7e);
		register2(InstMFP201.Imov, 0x7f, GEN_GEN, 0x7f);
		
		/* lea! */
		register(InstMFP201.Ilea, 0x5010, SFO_REG, 0x5f1f);
		
		/* three-op instructions */
		register4(InstMFP201.Ior, 0x80, 0x8f);
		register4(InstMFP201.Iand, 0x90, 0x9f);
		register4(InstMFP201.Inand, 0xa0, 0xaf);
		register4(InstMFP201.Ixor, 0xb0, 0xbf);
		register4(InstMFP201.Iadd, 0xc0, 0xcf);
		register4(InstMFP201.Iadc, 0xd0, 0xdf);
		register4(InstMFP201.Isub, 0xe0, 0xef);
		register4(InstMFP201.Isbb, 0xf0, 0xff);
		
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
	registerInstruction(InstMFP201.Ibkpt, "bkpt");
	registerInstruction(InstMFP201.Iret, "ret");
	registerInstruction(InstMFP201.Ireti, "reti");
	registerInstruction(InstMFP201.Ibr, "br");
	registerInstruction(InstMFP201.Ibra, "bra");
	registerInstruction(InstMFP201.Icall, "call");
	registerInstruction(InstMFP201.Icalla, "calla");
	
	registerInstruction(InstMFP201.Isext, "sext");
	registerInstruction(InstMFP201.Iexth, "exth");
	registerInstruction(InstMFP201.Iextl, "extl");
	registerInstruction(InstMFP201.Iswpb, "swpb");
	registerInstruction(InstMFP201.Ipush, "push");
	registerInstruction(InstMFP201.Ipushb, "push.b");
	registerInstruction(InstMFP201.Ipop, "pop");
	registerInstruction(InstMFP201.Ipopb, "pop.b");
	
	registerInstruction(InstMFP201.Ijne, "jne");
	registerInstruction(InstMFP201.Ijeq, "jeq");
	registerInstruction(InstMFP201.Ijnc, "jnc");
	registerInstruction(InstMFP201.Ijc, "jc");
	registerInstruction(InstMFP201.Ijs, "js");
	registerInstruction(InstMFP201.Ijge, "jge");
	registerInstruction(InstMFP201.Ijl, "jl");
	registerInstruction(InstMFP201.Ijmp, "jmp");
	
	registerInstruction(InstMFP201.Imovne, "movne");
	registerInstruction(InstMFP201.Imovneb, "movne.b");
	registerInstruction(InstMFP201.Imoveq, "moveq");
	registerInstruction(InstMFP201.Imoveqb, "moveq.b");
	registerInstruction(InstMFP201.Imovnc, "movnc");
	registerInstruction(InstMFP201.Imovncb, "movnc.b");
	registerInstruction(InstMFP201.Imovc, "movc");
	registerInstruction(InstMFP201.Imovcb, "movc.b");
	registerInstruction(InstMFP201.Imovs, "movs");
	registerInstruction(InstMFP201.Imovsb, "movs.b");
	registerInstruction(InstMFP201.Imovge, "movge");
	registerInstruction(InstMFP201.Imovgeb, "movge.b");
	registerInstruction(InstMFP201.Imovl, "movl");
	registerInstruction(InstMFP201.Imovlb, "movl.b");
	registerInstruction(InstMFP201.Imov, "mov");
	registerInstruction(InstMFP201.Imovb, "mov.b");
	
	registerInstruction(InstMFP201.Ilsh, "lsh");
	registerInstruction(InstMFP201.Ilshb, "lsh.b");
	registerInstruction(InstMFP201.Irsh, "rsh");
	registerInstruction(InstMFP201.Irshb, "rsh.b");
	registerInstruction(InstMFP201.Iash, "ash");
	registerInstruction(InstMFP201.Iashb, "ash.b");
	registerInstruction(InstMFP201.Irol, "rol");
	registerInstruction(InstMFP201.Irolb, "rol.b");
	registerInstruction(InstMFP201.Imul, "mul");
	registerInstruction(InstMFP201.Imulb, "mul.b");
	registerInstruction(InstMFP201.Idiv, "div");
	registerInstruction(InstMFP201.Idivb, "div.b");
	registerInstruction(InstMFP201.Imuld, "muld");
	registerInstruction(InstMFP201.Imuldb, "muld.b");
	registerInstruction(InstMFP201.Idivd, "divd");
	registerInstruction(InstMFP201.Idivdb, "divd.b");
	
	registerInstruction(InstMFP201.Ilea, "lea");
	
	registerInstruction(InstMFP201.Ior, "or");
	registerInstruction(InstMFP201.Iorb, "or.b");
	registerInstruction(InstMFP201.Iorq, "or?");
	registerInstruction(InstMFP201.Iorbq, "or.b?");
	
	registerInstruction(InstMFP201.Iand, "and");
	registerInstruction(InstMFP201.Iandb, "and.b");
	registerInstruction(InstMFP201.Itst, "tst");
	registerInstruction(InstMFP201.Itstb, "tst.b");
	
	registerInstruction(InstMFP201.Inand, "nand");
	registerInstruction(InstMFP201.Inandb, "nand.b");
	registerInstruction(InstMFP201.Itstn, "tstn");
	registerInstruction(InstMFP201.Itstnb, "tstn.b");
	
	registerInstruction(InstMFP201.Ixor, "xor");
	registerInstruction(InstMFP201.Ixorb, "xor.b");
	registerInstruction(InstMFP201.Ixorq, "xor?");
	registerInstruction(InstMFP201.Ixorbq, "xor.b?");
	
	registerInstruction(InstMFP201.Iadd, "add");
	registerInstruction(InstMFP201.Iaddb, "add.b");
	registerInstruction(InstMFP201.Iaddq, "add?");
	registerInstruction(InstMFP201.Iaddbq, "add.b?");
	
	registerInstruction(InstMFP201.Isub, "sub");
	registerInstruction(InstMFP201.Isubb, "sub.b");
	registerInstruction(InstMFP201.Icmp, "cmp");
	registerInstruction(InstMFP201.Icmpb, "cmp.b");
	
	registerInstruction(InstMFP201.Iadc, "adc");
	registerInstruction(InstMFP201.Iadcb, "adc.b");
	registerInstruction(InstMFP201.Iadcq, "adc?");
	registerInstruction(InstMFP201.Iadcbq, "adc.b?");
	
	registerInstruction(InstMFP201.Isbb, "sbb");
	registerInstruction(InstMFP201.Isbbb, "sbb.b");
	registerInstruction(InstMFP201.Icmpr, "cmpr");
	registerInstruction(InstMFP201.Icmprb, "cmpr.b");
	

	registerInstruction(InstMFP201.Ildc, "ldc");
	registerInstruction(InstMFP201.Ildcb, "ldc.b");
	registerInstruction(InstMFP201.Ildcq, "ldc?");
	registerInstruction(InstMFP201.Ildcbq, "ldc.b?");
	
	registerInstruction(InstTableCommon.Idsr, "dsr");
	registerInstruction(InstTableCommon.Ikysl, "kysl");
	registerInstruction(InstTableCommon.Iticks, "ticks");
	registerInstruction(InstTableCommon.Iemitchar, "emitchar");
	registerInstruction(InstTableCommon.Idbg, "dbg");
	registerInstruction(InstTableCommon.Idbgf, "dbgf");
	registerInstruction(InstTableCommon.Ibyte, "byte");

	//registerAlias(InstMFP201.Ijeq, "je");
	//registerAlias(InstMFP201.Ijoc, "jc");

	}

	public static byte[] encode(RawInstruction rawInst) throws IllegalArgumentException {
		int inst = rawInst.getInst();
		//int variant = rawInst instanceof InstructionMFP201 ? 
		//		((InstructionMFP201) rawInst).variant : InstructionMFP201.VARIANT_NONE;
				
		InstPatternMFP201[] patterns = instEntries.get(inst);
		if (patterns == null)
			throw new IllegalArgumentException("Non-encoded instruction " + rawInst);
		
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
		
		IllegalArgumentException lastException = null;
		for (InstPatternMFP201 pattern : patterns) {
			try {
				assertOperandMatches(rawInst, mop1, pattern.op1);
				assertOperandMatches(rawInst, mop2, pattern.op2);
				assertOperandMatches(rawInst, mop3, pattern.op3);
				lastException = null;
			} catch (IllegalArgumentException e) {
				lastException = e;
				continue;
			}
			
			// hi 2 bytes: loop prefix, lo byte: mem/size byte 
			int loopAndMemSize = 0;
	
			MachineOperandMFP201[] mops = { mop1, mop2, mop3 };
			
			byte[] immeds = { 0, 0, 0, 0 };
			int immIdx = 0;
			
			byte[] work = { 0, 0, 0, 0, 0, 0 };
			
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
						
						if (mop != null && inst != InstMFP201.Ilea) {
							if ((mop.type >= MachineOperandMFP201.OP_REG
									&& mop.type <= MachineOperandMFP201.OP_INC) 
									|| mop.type == MachineOperandMFP201.OP_DEC) {
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
								if (mop.encoding == MachineOperandMFP201.OP_ENC_IMM8) {
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
						if (mop.type == MachineOperandMFP201.OP_PCREL) {
							val = mop.val - workIdx - immIdx - 1;
						} else {
							val = mop.immed - workIdx - immIdx - 1 - rawInst.pc;
						}
						immeds[immIdx++] = (byte) ((val & 0xff00) >> 8); 
						immeds[immIdx++] = (byte) (val & 0xff); 
					}
					else if (enc == _JMP) {
						int val;
						if (mop.type == MachineOperandMFP201.OP_PCREL) {
							val = mop.val - workIdx - immIdx - 1;
						} else {
							val = mop.immed - workIdx - immIdx - 1 - rawInst.pc;
						}
						
						if (mop.encoding == MachineOperandMFP201.OP_ENC_PCREL8
								|| isImm8((short) val)) {
							immeds[immIdx++] = (byte) (val & 0xff);
						}
						else if (mop.encoding == MachineOperandMFP201.OP_ENC_PCREL12
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
					else if (enc == _IMM_AS_M1) {	
						if (mop.immed >= 1 && mop.immed <= 4) 
							loopAndMemSize |= InstructionMFP201.MEM_SIZE_OPCODE |
								((mop.immed - 1) << 2);
						else
							throw new IllegalArgumentException("immediate must be in the range 1 to 4 in " + rawInst);
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
						if (sro.type == MachineOperandMFP201.OP_SRO)
							work[++workIdx] = (byte) ((sro.val << 4) | (sro.scaleReg));
						else
							work[++workIdx] = (byte) ((sro.val << 4) | MachineOperandMFP201.SR);
						
						// immediate and 
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
		throw lastException;
	}

	/**
	 * @param inst
	 * @return
	 */
	public static boolean canHaveMemSizeByte(int inst) {
		return inst >= InstMFP201._IlastSimpleImmediate;
	}

	public static boolean canBeSimpleImmediateInst(int inst) {
		return (inst >= InstMFP201.Ior && inst <= InstMFP201.Ildcbq);
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
			if (mop.type != MachineOperandMFP201.OP_CNT && mop.type != MachineOperandMFP201.OP_IMM)
				throw new IllegalArgumentException("Expected count: " + mop + " in " + inst);
			break;
		case OFF:
			if (mop.type != MachineOperandMFP201.OP_CNT && mop.type != MachineOperandMFP201.OP_IMM
					&& mop.type != MachineOperandMFP201.OP_PCREL)
				throw new IllegalArgumentException("Expected offset: " + mop + " in " + inst);
			break;
		case REG:
			if (mop.type != MachineOperandMFP201.OP_REG && mop.type != MachineOperandMFP201.OP_REG0_SHIFT_COUNT)
				throw new IllegalArgumentException("Expected register: " + mop + " in " + inst);
			break;
		case GEN:
			if (mop.type == MachineOperandMFP201.OP_NONE
					|| mop.type == MachineOperandMFP201.OP_IMM)
				throw new IllegalArgumentException("Expected general operand: " + mop + " in " + inst);
			break;
		case SRO:
			if (mop.type != MachineOperandMFP201.OP_DEC
					&& mop.type != MachineOperandMFP201.OP_INC
					&& mop.type != MachineOperandMFP201.OP_IND
					&& mop.type != MachineOperandMFP201.OP_OFFS
					&& mop.type != MachineOperandMFP201.OP_SRO) {
				throw new IllegalArgumentException("Expected general or scaled operand: " + mop + " in " + inst);
			}
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
			
			if (mop3 == null && pattern.length >= 3)
				continue;
			if (mop2 == null && pattern.length >= 2)
				continue;
			if (mop1 == null && pattern.length >= 1)
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
			if (mop.type == MachineOperandMFP201.OP_REG0_SHIFT_COUNT
					|| (mop.type == MachineOperandMFP201.OP_REG && mop.val == 0)) {
				return MachineOperandMFP201.createGeneralOperand(MachineOperandMFP201.OP_CNT, 
						mop.val);
			}
			if (mop.type == MachineOperandMFP201.OP_IMM) {
				return MachineOperandMFP201.createGeneralOperand(MachineOperandMFP201.OP_CNT,
						mop.immed);
			}
			break;
		case OFF:
			if (mop.type == MachineOperandMFP201.OP_IMM) {
				// convert address to offset from this inst
				return MachineOperandMFP201.createGeneralOperand(MachineOperandMFP201.OP_PCREL,
						mop.val - instruction.pc);
			}
			break;
		case REG:
			break;
		case GEN:
			if (mop.type == MachineOperandMFP201.OP_IMM
					&& !isDestOp(instruction, mop)) {
				// immediate is *PC+
				MachineOperandMFP201 immed = MachineOperandMFP201.createGeneralOperand(
						MachineOperandMFP201.OP_INC,
						MachineOperandMFP201.PC,
						mop.immed);
				if (canConvertToByteInst(instruction, mop)) {
					// if all imm or reg ops, we can convert this to a byte
					if (hasNoOtherMemoryOperands(instruction, mop)) {
						instruction.setInst(instruction.getInst() | 1);
					}

				}
				immed.encoding = isByteInst(instruction.getInst()) ? 
						MachineOperandMFP201.OP_ENC_IMM8 : MachineOperandMFP201.OP_ENC_IMM16;
				return immed;
			}
			else if (mop.type == MachineOperandMFP201.OP_PCREL) {
				// PC-rel is @x(PC)
				MachineOperandMFP201 pcrel = MachineOperandMFP201.createGeneralOperand(
						MachineOperandMFP201.OP_OFFS,
						MachineOperandMFP201.PC,
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
			&& ins.getInst() != InstMFP201.Ipush
			&& ins.getInst() != InstMFP201.Ipushb;
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
				&& ((instruction.getInst() != InstMFP201.Ipush
					&& instruction.getInst() != InstMFP201.Ipop)
					|| instruction.getOp2() == null
					|| ((MachineOperandMFP201)instruction.getOp2()).type  == MachineOperandMFP201.OP_NONE);
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
            mop1.type = MachineOperandMFP201.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperandMFP201.OP_IMM;
            switch ((op & 0x1e0) >> 5) {
            case 0:
                //inst.name = "LI";
                inst.setInst(InstMFP201.Ili);
                break;
            case 1:
                //inst.name = "AI";
                inst.setInst(InstMFP201.Iai);
                break;
            case 2:
                //inst.name = "ANDI";
                inst.setInst(InstMFP201.Iandi);
                break;
            case 3:
                //inst.name = "ORI";
                inst.setInst(InstMFP201.Iori);
                break;
            case 4:
                //inst.name = "CI";
                inst.setInst(InstMFP201.Ici);
                break;
            }
    
        } else if (op < 0x2e0) {
            mop1.type = MachineOperandMFP201.OP_REG;
            mop1.val = op & 15;
            switch ((op & 0x1e0) >> 5) {
            case 5:
                //inst.name = "STWP";
                inst.setInst(InstMFP201.Istwp);
                break;
            case 6:
                //inst.name = "STST";
                inst.setInst(InstMFP201.Istst);
                break;
            }
    
        } else if (op < 0x320) {
            mop1.type = MachineOperandMFP201.OP_IMM;
    
            switch ((op & 0x1e0) >> 5) {
            case 7:
                //inst.name = "LWPI";
                inst.setInst(InstMFP201.Ilwpi);
                break;
            case 8:
                //inst.name = "LIMI";
                inst.setInst(InstMFP201.Ilimi);
                break;
            }
    
        } else if (op < 0x400) {
            switch ((op & 0x1e0) >> 5) {
            case 10:
                //inst.name = "IDLE";
                inst.setInst(InstMFP201.Iidle);
                break;
            case 11:
                //inst.name = "RSET";
                inst.setInst(InstMFP201.Irset);
                break;
            case 12:
                //inst.name = "RTWP";
                inst.setInst(InstMFP201.Irtwp);
                break;
            case 13:
                //inst.name = "CKON";
                inst.setInst(InstMFP201.Ickon);
                break;
            case 14:
                //inst.name = "CKOF";
                inst.setInst(InstMFP201.Ickof);
                break;
            case 15:
                //inst.name = "LREX";
                inst.setInst(InstMFP201.Ilrex);
                break;
            }
    
        } else if (op < 0x800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
    
            switch ((op & 0x3c0) >> 6) {
            case 0:
                //inst.name = "BLWP";
                inst.setInst(InstMFP201.Iblwp);
                break;
            case 1:
                //inst.name = "B";
                inst.setInst(InstMFP201.Ib);
                break;
            case 2:
                //inst.name = "X";
                inst.setInst(InstMFP201.Ix);
                break;
            case 3:
                //inst.name = "CLR";
                inst.setInst(InstMFP201.Iclr);
                break;
            case 4:
                //inst.name = "NEG";
                inst.setInst(InstMFP201.Ineg);
                break;
            case 5:
                //inst.name = "INV";
                inst.setInst(InstMFP201.Iinv);
                break;
            case 6:
                //inst.name = "INC";
                inst.setInst(InstMFP201.Iinc);
                break;
            case 7:
                //inst.name = "INCT";
                inst.setInst(InstMFP201.Iinct);
                break;
            case 8:
                //inst.name = "DEC";
                inst.setInst(InstMFP201.Idec);
                break;
            case 9:
                //inst.name = "DECT";
                inst.setInst(InstMFP201.Idect);
                break;
            case 10:
                //inst.name = "BL";
                inst.setInst(InstMFP201.Ibl);
                break;
            case 11:
                //inst.name = "SWPB";
                inst.setInst(InstMFP201.Iswpb);
                break;
            case 12:
                //inst.name = "SETO";
                inst.setInst(InstMFP201.Iseto);
                break;
            case 13:
                //inst.name = "ABS";
                inst.setInst(InstMFP201.Iabs);
                break;
            }
    
        } else if (op < 0xc00) {
            mop1.type = MachineOperandMFP201.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperandMFP201.OP_CNT;
            mop2.val = (op & 0xf0) >> 4;
    
            switch ((op & 0x700) >> 8) {
            case 0:
                //inst.name = "SRA";
                inst.setInst(InstMFP201.Isra);
                break;
            case 1:
                //inst.name = "SRL";
                inst.setInst(InstMFP201.Isrl);
                break;
            case 2:
                //inst.name = "SLA";
                inst.setInst(InstMFP201.Isla);
                break;
            case 3:
                //inst.name = "SRC";
                inst.setInst(InstMFP201.Isrc);
                break;
            }
    
        } else if (op < 0x1000) {
}    
        if (inst.getInst() == 0) // data
        {
            mop1.type = MachineOperandMFP201.OP_IMM;
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
		return inst >= InstMFP201._IfirstJumpOp && inst <= InstMFP201._IlastJumpOp;
	}
	
	public static boolean isMoveInst(int inst) {
		return inst >= InstMFP201._IfirstMovOp && inst <= InstMFP201._IlastMovOp;
	}
	
	public static boolean isByteInst(int inst) {
		return (inst & 1) != 0 && canBeByteInst(inst - 1);
	}

	
	public static boolean canBeByteInst(int inst) {
		return (inst >= InstMFP201._IfirstPossibleByteOp && inst <= InstMFP201._IlastPossibleByteOp)
		|| (inst >= InstMFP201._IfirstPossibleByteOp2 && inst <= InstMFP201._IlastPossibleByteOp2)
		|| (inst >= InstMFP201._IfirstPossibleByteOp3 && inst <= InstMFP201._IlastPossibleByteOp3);
	}

	public static boolean canBeThreeOpInst(int inst) {
		return inst >= InstMFP201._IfirstPossibleThreeOp 
			&& inst <= InstMFP201._IlastPossibleThreeOp;
	}
	public static boolean isLogicalOpInst(int inst) {
		return inst >= InstMFP201._IfirstLogicalOp 
			&& inst <= InstMFP201._IlastLogicalOp;
	}
	public static boolean isLoadConstInst(int inst) {
		return inst >= InstMFP201.Ildc && inst <= InstMFP201.Ildcbq;
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
			if (inst == InstMFP201.Iadd 
					|| inst == InstMFP201.Iand
					|| inst == InstMFP201.Ior
					|| inst == InstMFP201.Ixor
					|| inst == InstMFP201.Inand) {
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
		if (inst >= InstMFP201.Icmp && inst <= InstMFP201.Icmpb)
			return inst - InstMFP201.Icmp + InstMFP201.Icmpr;
		if (inst >= InstMFP201.Icmpr && inst <= InstMFP201.Icmprb)
			return inst - InstMFP201.Icmpr + InstMFP201.Icmp;
		if (isCommutativeInst(inst))
			return inst;
		return 0;
	}

	/**
	 * @param inst
	 * @return
	 */
	public static boolean isArithOpInst(int inst) {
		return inst >= InstMFP201._IfirstArithOp 
		&& inst <= InstMFP201._IlastArithOp;
	}
}
