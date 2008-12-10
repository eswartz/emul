/**
 * 
 */
package v9t9.engine.cpu;

import static v9t9.engine.cpu.InstEncodePattern.CNT;
import static v9t9.engine.cpu.InstEncodePattern.GEN;
import static v9t9.engine.cpu.InstEncodePattern.IMM;
import static v9t9.engine.cpu.InstEncodePattern.NONE;
import static v9t9.engine.cpu.InstEncodePattern.OFF;
import static v9t9.engine.cpu.InstEncodePattern.REG;

import java.util.HashMap;
import java.util.Map;

import v9t9.engine.memory.MemoryDomain;

/**
 * This class takes an Instruction and generates its opcode.
 * 
 * @author ejs
 * 
 */
public class InstructionTable {
	final static InstEncodePattern NONE_NONE = new InstEncodePattern(NONE, NONE);
	final static InstEncodePattern IMM_NONE = new InstEncodePattern(IMM, NONE);
	final static InstEncodePattern REG_IMM = new InstEncodePattern(REG, IMM);
	final static InstEncodePattern GEN_NONE = new InstEncodePattern(GEN, NONE);
	final static InstEncodePattern REG_CNT = new InstEncodePattern(REG, CNT, 4);
	final static InstEncodePattern JMP_NONE = new InstEncodePattern(OFF, NONE);
	final static InstEncodePattern GEN_REG = new InstEncodePattern(GEN, REG, 6);
	final static InstEncodePattern GEN6_REG = new InstEncodePattern(GEN, REG, 6);
	final static InstEncodePattern GEN_CNT = new InstEncodePattern(GEN, CNT, 6);
	final static InstEncodePattern GEN_GEN = new InstEncodePattern(GEN, GEN, 6);
	final static InstEncodePattern REG_NONE = new InstEncodePattern(REG, NONE);
	final static InstEncodePattern CNT_NONE = new InstEncodePattern(CNT, NONE);
	final static InstEncodePattern OFF_NONE = new InstEncodePattern(OFF, NONE);

	static Map<Integer, InstEncodePattern> instEntries = new HashMap<Integer, InstEncodePattern>();
	static Map<Integer, Integer> instOpcodes = new HashMap<Integer, Integer>();
	static Map<Integer, Integer> instMasks = new HashMap<Integer, Integer>();

	private static void register(int inst, int opcode, InstEncodePattern entry) {
		instEntries.put(inst, entry);
		instOpcodes.put(inst, opcode);
		instMasks.put(inst, entry == NONE_NONE || entry == IMM_NONE ? opcode : 0xffff);
	}
	private static void register(int inst, int opcode, InstEncodePattern entry, int mask) {
		instEntries.put(inst, entry);
		instOpcodes.put(inst, opcode);
		instMasks.put(inst, mask);
	}
	static {
		register(InstructionTable.Idata, 0x0000, IMM_NONE, 0xffff);
		register(InstructionTable.Ibyte, 0x0000, IMM_NONE, 0xffff);

		register(InstructionTable.Ili, 0x0200, REG_IMM, 0x20f);
		register(InstructionTable.Iai, 0x0220, REG_IMM, 0x22f);
		register(InstructionTable.Iandi, 0x0240, REG_IMM, 0x24f);
		register(InstructionTable.Iori, 0x0260, REG_IMM, 0x26f);
		register(InstructionTable.Ici, 0x0280, REG_IMM, 0x28f);

		register(InstructionTable.Istwp, 0x02a0, REG_NONE, 0x2af);
		register(InstructionTable.Istst, 0x02c0, REG_NONE, 0x2cf);
		register(InstructionTable.Ilwpi, 0x02e0, IMM_NONE);
		register(InstructionTable.Ilimi, 0x0300, IMM_NONE);
		register(InstructionTable.Iidle, 0x0340, NONE_NONE);
		register(InstructionTable.Irset, 0x0360, NONE_NONE);
		register(InstructionTable.Irtwp, 0x0380, NONE_NONE);
		register(InstructionTable.Ickon, 0x03a0, NONE_NONE);
		register(InstructionTable.Ickof, 0x03c0, NONE_NONE);
		register(InstructionTable.Ilrex, 0x03e0, NONE_NONE);

		register(InstructionTable.Iblwp, 0x0400, GEN_NONE);
		register(InstructionTable.Ib, 0x0440, GEN_NONE);
		register(InstructionTable.Ix, 0x0480, GEN_NONE);
		register(InstructionTable.Iclr, 0x04c0, GEN_NONE);
		register(InstructionTable.Ineg, 0x0500, GEN_NONE);
		register(InstructionTable.Iinv, 0x0540, GEN_NONE);
		register(InstructionTable.Iinc, 0x0580, GEN_NONE);
		register(InstructionTable.Iinct, 0x05c0, GEN_NONE);
		register(InstructionTable.Idec, 0x0600, GEN_NONE);
		register(InstructionTable.Idect, 0x0640, GEN_NONE);
		register(InstructionTable.Ibl, 0x0680, GEN_NONE);
		register(InstructionTable.Iswpb, 0x06c0, GEN_NONE);
		register(InstructionTable.Iseto, 0x0700, GEN_NONE);
		register(InstructionTable.Iabs, 0x0740, GEN_NONE);

		register(InstructionTable.Isra, 0x0800, REG_CNT);
		register(InstructionTable.Isrl, 0x0900, REG_CNT);
		register(InstructionTable.Isla, 0x0a00, REG_CNT);
		register(InstructionTable.Isrc, 0x0b00, REG_CNT);

		register(InstructionTable.Idsr, 0x0c00, CNT_NONE, 0xc00);
		register(InstructionTable.Ikysl, 0x0d40, NONE_NONE, 0xd40);
		register(InstructionTable.Iticks, 0x0d60, REG_NONE, 0xd60);
		register(InstructionTable.Iemitchar, 0x0dc0, REG_CNT, 0xdcf);
		register(InstructionTable.Idbg, 0x0de0, NONE_NONE, 0xde0);
		register(InstructionTable.Idbgf, 0x0de1, NONE_NONE, 0xde1);

		register(InstructionTable.Ijmp, 0x1000, JMP_NONE);
		register(InstructionTable.Ijlt, 0x1100, JMP_NONE);
		register(InstructionTable.Ijle, 0x1200, JMP_NONE);
		register(InstructionTable.Ijeq, 0x1300, JMP_NONE);
		register(InstructionTable.Ijhe, 0x1400, JMP_NONE);
		register(InstructionTable.Ijgt, 0x1500, JMP_NONE);
		register(InstructionTable.Ijne, 0x1600, JMP_NONE);
		register(InstructionTable.Ijnc, 0x1700, JMP_NONE);
		register(InstructionTable.Ijoc, 0x1800, JMP_NONE);
		register(InstructionTable.Ijno, 0x1900, JMP_NONE);
		register(InstructionTable.Ijl, 0x1a00, JMP_NONE);
		register(InstructionTable.Ijh, 0x1b00, JMP_NONE);
		register(InstructionTable.Ijop, 0x1c00, JMP_NONE);

		register(InstructionTable.Isbo, 0x1d00, OFF_NONE);
		register(InstructionTable.Isbz, 0x1e00, OFF_NONE);
		register(InstructionTable.Itb, 0x1f00, OFF_NONE);

		register(InstructionTable.Icoc, 0x2000, GEN6_REG);
		register(InstructionTable.Iczc, 0x2400, GEN6_REG);
		register(InstructionTable.Ixor, 0x2800, GEN6_REG);
		register(InstructionTable.Ixop, 0x2c00, GEN6_REG);
		
		register(InstructionTable.Ildcr, 0x3000, GEN_CNT);
		register(InstructionTable.Istcr, 0x3400, GEN_CNT);
		register(InstructionTable.Impy, 0x3800, GEN_REG);
		register(InstructionTable.Idiv, 0x3c00, GEN_REG);


		register(InstructionTable.Iszc, 0x4000, GEN_GEN);
		register(InstructionTable.Iszcb, 0x5000, GEN_GEN);
		register(InstructionTable.Is, 0x6000, GEN_GEN);
		register(InstructionTable.Isb, 0x7000, GEN_GEN);
		register(InstructionTable.Ic, 0x8000, GEN_GEN);
		register(InstructionTable.Icb, 0x9000, GEN_GEN);
		register(InstructionTable.Ia, 0xa000, GEN_GEN);
		register(InstructionTable.Iab, 0xb000, GEN_GEN);
		register(InstructionTable.Imov, 0xc000, GEN_GEN);
		register(InstructionTable.Imovb, 0xd000, GEN_GEN);
		register(InstructionTable.Isoc, 0xe000, GEN_GEN);
		register(InstructionTable.Isocb, 0xf000, GEN_GEN);

	};

	public static short[] encode(RawInstruction rawInstruction) throws IllegalArgumentException {
		Integer opcodeI = instOpcodes.get(rawInstruction.inst);
		if (opcodeI == null)
			throw new IllegalArgumentException("Non-machine instruction");
		int opcode = opcodeI;
		
		InstEncodePattern pattern = instEntries.get(rawInstruction.inst);
		if (pattern == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (!(rawInstruction.op1 instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 1: " + rawInstruction.op1);
		if (!(rawInstruction.op2 instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 2: " + rawInstruction.op1);
		
		rawInstruction.size = 2;	// at least (for jumps)
		coerceOperandTypes(rawInstruction);
		
		MachineOperand mop1 = (MachineOperand) rawInstruction.op1;
		MachineOperand mop2 = (MachineOperand) rawInstruction.op2;
		
		assertOperandMatches(mop1, pattern.op1);
		assertOperandMatches(mop2, pattern.op2);
		
		opcode |= (mop2.getBits() << pattern.off1) | mop1.getBits();
		
		short[] words = { (short)opcode, 0, 0 };
		int wordCount = rawInstruction.inst != InstructionTable.Idata ? 1 : 0;
		if (mop1.hasImmediate()) {
			words[wordCount++] = mop1.immed;
		}
		if (mop2.hasImmediate()) {
			words[wordCount++] = mop2.immed;
		}
		
		rawInstruction.size = wordCount * 2;
		
		if (wordCount == 1)
			return new short[] { words[0] };
		else if (wordCount == 2)
			return new short[] { words[0], words[1] };
		else
			return words;
	}

	private static void assertOperandMatches(MachineOperand mop, int op) {
		switch (op) {
		case NONE:
			if (mop.type != MachineOperand.OP_NONE
					&& mop.type != MachineOperand.OP_STATUS
					&& mop.type != MachineOperand.OP_INST)
				throw new IllegalArgumentException("Unexpected operand: " + mop);
			break;
		case IMM:
			if (mop.type != MachineOperand.OP_IMMED)
				throw new IllegalArgumentException("Expected immediate: " + mop);
			break;
		case CNT:
			if (mop.type != MachineOperand.OP_CNT && mop.type != MachineOperand.OP_IMMED)
				throw new IllegalArgumentException("Expected count: " + mop);
			break;
		case OFF:
			if (mop.type != MachineOperand.OP_CNT && mop.type != MachineOperand.OP_IMMED
					&& mop.type != MachineOperand.OP_OFFS_R12
					&& mop.type != MachineOperand.OP_JUMP)
				throw new IllegalArgumentException("Expected offset: " + mop);
			break;
		case REG:
			if (mop.type != MachineOperand.OP_REG && mop.type != MachineOperand.OP_REG0_SHIFT_COUNT)
				throw new IllegalArgumentException("Expected immediate: " + mop);
			break;
		case GEN:
			if (mop.type != MachineOperand.OP_REG 
					&& mop.type != MachineOperand.OP_INC
					&& mop.type != MachineOperand.OP_IND
					&& mop.type != MachineOperand.OP_ADDR)
				throw new IllegalArgumentException("Expected general operand: " + mop);
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
	public static void coerceOperandTypes(RawInstruction instruction) {
		InstEncodePattern pattern = instEntries.get(instruction.inst);
		if (pattern == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (instruction.op1 instanceof MachineOperand) {
			coerceOperandType(instruction, (MachineOperand) instruction.op1, pattern.op1);
		}
		if (instruction.op2 instanceof MachineOperand) {
			coerceOperandType(instruction, (MachineOperand) instruction.op2, pattern.op2);
		}		
		
	}
	private static void coerceOperandType(RawInstruction instruction, MachineOperand mop, int op) {
		switch (op) {
		case NONE:
			if (mop.type == MachineOperand.OP_STATUS
					|| mop.type == MachineOperand.OP_INST
					|| (instruction.inst >= InstructionTable.Iinc && instruction.inst <= InstructionTable.Idect))
				mop.type = MachineOperand.OP_NONE;
			break;
		case IMM:
			if (mop.type == MachineOperand.OP_REG)
				mop.type = MachineOperand.OP_IMMED;
			break;
		case CNT:
			if (mop.type == MachineOperand.OP_REG
					|| mop.type == MachineOperand.OP_IMMED
					|| mop.type == MachineOperand.OP_REG0_SHIFT_COUNT)
				mop.type = MachineOperand.OP_CNT;
			if (mop.val == 16)
				mop.val = 0;
			break;
		case OFF:
			if (instruction.isJumpInst()) {
				if (mop.type == MachineOperand.OP_IMMED) {
					// convert address to offset from this inst
					mop.type = MachineOperand.OP_JUMP;
					mop.val = (mop.val - instruction.pc);
				}
			} else {
				if (mop.type == MachineOperand.OP_IMMED) {
					mop.type = MachineOperand.OP_OFFS_R12;
					mop.val = mop.immed;
				}
			}
			break;
		case REG:
			if (mop.type == MachineOperand.OP_IMMED)
				mop.type = MachineOperand.OP_REG;
			break;
		case GEN:
			if (mop.type == MachineOperand.OP_IMMED)
				mop.type = MachineOperand.OP_REG;
			break;
		}
	}
	
	public static int coerceInstructionOpcode(int inst, int opcode) {
		Integer mask = instMasks.get(inst);
		if (mask == null)
			return opcode;
		return opcode & mask;
	}
	public static InstEncodePattern lookupEncodePattern(int inst) {
		return instEntries.get(inst);
	}
	// Opcodes: ORDERING MATTERS!
	public static final int Idata = 0;
	public static final int Ili = 1;
	public static final int Iai = 2;
	public static final int Iandi = 3;
	public static final int Iori = 4;
	public static final int Ici = 5;
	public static final int Istwp = 6;
	public static final int Istst = 7;
	public static final int Ilwpi = 8;
	public static final int Ilimi = 9;
	public static final int Iidle = 10;
	public static final int Irset = 11;
	public static final int Irtwp = 12;
	public static final int Ickon = 13;
	public static final int Ickof = 14;
	public static final int Ilrex = 15;
	public static final int Iblwp = 16;
	public static final int Ib = 17;
	public static final int Ix = 18;
	public static final int Iclr = 19;
	public static final int Ineg = 20;
	public static final int Iinv = 21;
	public static final int Iinc = 22;
	public static final int Iinct = 23;
	public static final int Idec = 24;
	public static final int Idect = 25;
	public static final int Ibl = 26;
	public static final int Iswpb = 27;
	public static final int Iseto = 28;
	public static final int Iabs = 29;
	public static final int Isra = 30;
	public static final int Isrl = 31;
	public static final int Isla = 32;
	public static final int Isrc = 33;
	public static final int Ijmp = 34;
	public static final int Ijlt = 35;
	public static final int Ijle = 36;
	public static final int Ijeq = 37;
	public static final int Ijhe = 38;
	public static final int Ijgt = 39;
	public static final int Ijne = 40;
	public static final int Ijnc = 41;
	public static final int Ijoc = 42;
	public static final int Ijno = 43;
	public static final int Ijl = 44;
	public static final int Ijh = 45;
	public static final int Ijop = 46;
	public static final int Isbo = 47;
	public static final int Isbz = 48;
	public static final int Itb = 49;
	public static final int Icoc = 50;
	public static final int Iczc = 51;
	public static final int Ixor = 52;
	public static final int Ixop = 53;
	public static final int Impy = 54;
	public static final int Idiv = 55;
	public static final int Ildcr = 56;
	public static final int Istcr = 57;
	public static final int Iszc = 58;
	public static final int Iszcb = 59;
	public static final int Is = 60;
	public static final int Isb = 61;
	public static final int Ic = 62;
	public static final int Icb = 63;
	public static final int Ia = 64;
	public static final int Iab = 65;
	public static final int Imov = 66;
	public static final int Imovb = 67;
	public static final int Isoc = 68;
	public static final int Isocb = 69;
	public static final int Idsr = 70;
	public static final int Ikysl = 71;
	public static final int Iticks = 72;
	public static final int Iemitchar = 73;
	public static final int Idbg = 74;
	public static final int Idbgf = 75;
	public static final int Ibyte = 76;
	public static final int Idelete = 77;	// noop
	public static Map<String, Integer> nameToInst = new HashMap<String, Integer>();
	public static Map<Integer, String> instToName = new HashMap<Integer, String>();
	
	static { registerInstruction(InstructionTable.Idata, "data"); }
	static { registerInstruction(InstructionTable.Ili, "li"); }
	static { registerInstruction(InstructionTable.Iai, "ai"); }
	static { registerInstruction(InstructionTable.Iandi, "andi"); }
	static { registerInstruction(InstructionTable.Iori, "ori"); }
	static { registerInstruction(InstructionTable.Ici, "ci"); }
	static { registerInstruction(InstructionTable.Istwp, "stwp"); }
	static { registerInstruction(InstructionTable.Istst, "stst"); }
	static { registerInstruction(InstructionTable.Ilwpi, "lwpi"); }
	static { registerInstruction(InstructionTable.Ilimi, "limi"); }
	static { registerInstruction(InstructionTable.Iidle, "idle"); }
	static { registerInstruction(InstructionTable.Irset, "rset"); }
	static { registerInstruction(InstructionTable.Irtwp, "rtwp"); }
	static { registerInstruction(InstructionTable.Ickon, "ckon"); }
	static { registerInstruction(InstructionTable.Ickof, "ckof"); }
	static { registerInstruction(InstructionTable.Ilrex, "lrex"); }
	static { registerInstruction(InstructionTable.Iblwp, "blwp"); }
	static { registerInstruction(InstructionTable.Ib, "b"); }
	static { registerInstruction(InstructionTable.Ix, "x"); }
	static { registerInstruction(InstructionTable.Iclr, "clr"); }
	static { registerInstruction(InstructionTable.Ineg, "neg"); }
	static { registerInstruction(InstructionTable.Iinv, "inv"); }
	static { registerInstruction(InstructionTable.Iinc, "inc"); }
	static { registerInstruction(InstructionTable.Iinct, "inct"); }
	static { registerInstruction(InstructionTable.Idec, "dec"); }
	static { registerInstruction(InstructionTable.Idect, "dect"); }
	static { registerInstruction(InstructionTable.Ibl, "bl"); }
	static { registerInstruction(InstructionTable.Iswpb, "swpb"); }
	static { registerInstruction(InstructionTable.Iseto, "seto"); }
	static { registerInstruction(InstructionTable.Iabs, "abs"); }
	static { registerInstruction(InstructionTable.Isra, "sra"); }
	static { registerInstruction(InstructionTable.Isrl, "srl"); }
	static { registerInstruction(InstructionTable.Isla, "sla"); }
	static { registerInstruction(InstructionTable.Isrc, "src"); }
	static { registerInstruction(InstructionTable.Ijmp, "jmp"); }
	static { registerInstruction(InstructionTable.Ijlt, "jlt"); }
	static { registerInstruction(InstructionTable.Ijle, "jle"); }
	static { registerInstruction(InstructionTable.Ijeq, "jeq"); }
	static { registerInstruction(InstructionTable.Ijhe, "jhe"); }
	static { registerInstruction(InstructionTable.Ijgt, "jgt"); }
	static { registerInstruction(InstructionTable.Ijne, "jne"); }
	static { registerInstruction(InstructionTable.Ijnc, "jnc"); }
	static { registerInstruction(InstructionTable.Ijoc, "joc"); }
	static { registerInstruction(InstructionTable.Ijno, "jno"); }
	static { registerInstruction(InstructionTable.Ijl, "jl"); }
	static { registerInstruction(InstructionTable.Ijh, "jh"); }
	static { registerInstruction(InstructionTable.Ijop, "jop"); }
	static { registerInstruction(InstructionTable.Isbo, "sbo"); }
	static { registerInstruction(InstructionTable.Isbz, "sbz"); }
	static { registerInstruction(InstructionTable.Itb, "tb"); }
	static { registerInstruction(InstructionTable.Icoc, "coc"); }
	static { registerInstruction(InstructionTable.Iczc, "czc"); }
	static { registerInstruction(InstructionTable.Ixor, "xor"); }
	static { registerInstruction(InstructionTable.Ixop, "xop"); }
	static { registerInstruction(InstructionTable.Impy, "mpy"); }
	static { registerInstruction(InstructionTable.Idiv, "div"); }
	static { registerInstruction(InstructionTable.Ildcr, "ldcr"); }
	static { registerInstruction(InstructionTable.Istcr, "stcr"); }
	static { registerInstruction(InstructionTable.Iszc, "szc"); }
	static { registerInstruction(InstructionTable.Iszcb, "szcb"); }
	static { registerInstruction(InstructionTable.Is, "s"); }
	static { registerInstruction(InstructionTable.Isb, "sb"); }
	static { registerInstruction(InstructionTable.Ic, "c"); }
	static { registerInstruction(InstructionTable.Icb, "cb"); }
	static { registerInstruction(InstructionTable.Ia, "a"); }
	static { registerInstruction(InstructionTable.Iab, "ab"); }
	static { registerInstruction(InstructionTable.Imov, "mov"); }
	static { registerInstruction(InstructionTable.Imovb, "movb"); }
	static { registerInstruction(InstructionTable.Isoc, "soc"); }
	static { registerInstruction(InstructionTable.Isocb, "socb"); }
	static { registerInstruction(InstructionTable.Idsr, "dsr"); }
	static { registerInstruction(InstructionTable.Ikysl, "kysl"); }
	static { registerInstruction(InstructionTable.Iticks, "ticks"); }
	static { registerInstruction(InstructionTable.Iemitchar, "emitchar"); }
	static { registerInstruction(InstructionTable.Idbg, "dbg"); }
	static { registerInstruction(InstructionTable.Idbgf, "dbgf"); }
	static { registerInstruction(InstructionTable.Ibyte, "byte"); }
	
	static { registerAlias(InstructionTable.Ijeq, "je"); }
	static { registerAlias(InstructionTable.Ijoc, "jc"); }

	private static void registerInstruction(int inst, String str) {
	    Integer i = new Integer(inst);
	    nameToInst.put(str.toUpperCase(), i);
	    instToName.put(i, str.toUpperCase());
	}
	private static void registerAlias(int inst, String str) {
	    Integer i = new Integer(inst);
	    nameToInst.put(str.toUpperCase(), i);
	}
	/**
	 * Get the instruction code for the given instruction
	 * @param str
	 * @return
	 */
	public static int lookupInst(String str) {
		str = str.toUpperCase();
		Integer inst = nameToInst.get(str);
		if (inst != null)
			return inst;
		else
			return -1;
	}
	/**
	 * Get an instruction by name
	 * @param inst
	 * @return
	 */
	public static String getInstName(int inst) {
		return instToName.get(new Integer(inst));
	}
	
   /**
     * Decode instruction with opcode 'op' at 'addr' into 'ins'.
     * 
     * @param domain
     *            provides read access to memory, to decode registers and
     *            instructions
     */
    public static RawInstruction decodeInstruction(int op, int pc, MemoryDomain domain) {
    	RawInstruction inst = new RawInstruction();
    	inst.pc = pc;
    	
        /* deal with it unsigned */
        op &= 0xffff;
    
        inst.opcode = (short) op;
        inst.inst = InstructionTable.Idata;
        inst.size = 0;
        MachineOperand mop1 = new MachineOperand(MachineOperand.OP_NONE);
        MachineOperand mop2 = new MachineOperand(MachineOperand.OP_NONE);
        inst.op1 = mop1;
        inst.op2 = mop2;
    
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
            mop1.type = MachineOperand.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperand.OP_IMMED;
            switch ((op & 0x1e0) >> 5) {
            case 0:
                //inst.name = "LI";
                inst.inst = InstructionTable.Ili;
                break;
            case 1:
                //inst.name = "AI";
                inst.inst = InstructionTable.Iai;
                break;
            case 2:
                //inst.name = "ANDI";
                inst.inst = InstructionTable.Iandi;
                break;
            case 3:
                //inst.name = "ORI";
                inst.inst = InstructionTable.Iori;
                break;
            case 4:
                //inst.name = "CI";
                inst.inst = InstructionTable.Ici;
                break;
            }
    
        } else if (op < 0x2e0) {
            mop1.type = MachineOperand.OP_REG;
            mop1.val = op & 15;
            switch ((op & 0x1e0) >> 5) {
            case 5:
                //inst.name = "STWP";
                inst.inst = InstructionTable.Istwp;
                break;
            case 6:
                //inst.name = "STST";
                inst.inst = InstructionTable.Istst;
                break;
            }
    
        } else if (op < 0x320) {
            mop1.type = MachineOperand.OP_IMMED;
    
            switch ((op & 0x1e0) >> 5) {
            case 7:
                //inst.name = "LWPI";
                inst.inst = InstructionTable.Ilwpi;
                break;
            case 8:
                //inst.name = "LIMI";
                inst.inst = InstructionTable.Ilimi;
                break;
            }
    
        } else if (op < 0x400) {
            switch ((op & 0x1e0) >> 5) {
            case 10:
                //inst.name = "IDLE";
                inst.inst = InstructionTable.Iidle;
                break;
            case 11:
                //inst.name = "RSET";
                inst.inst = InstructionTable.Irset;
                break;
            case 12:
                //inst.name = "RTWP";
                inst.inst = InstructionTable.Irtwp;
                break;
            case 13:
                //inst.name = "CKON";
                inst.inst = InstructionTable.Ickon;
                break;
            case 14:
                //inst.name = "CKOF";
                inst.inst = InstructionTable.Ickof;
                break;
            case 15:
                //inst.name = "LREX";
                inst.inst = InstructionTable.Ilrex;
                break;
            }
    
        } else if (op < 0x800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
    
            switch ((op & 0x3c0) >> 6) {
            case 0:
                //inst.name = "BLWP";
                inst.inst = InstructionTable.Iblwp;
                break;
            case 1:
                //inst.name = "B";
                inst.inst = InstructionTable.Ib;
                break;
            case 2:
                //inst.name = "X";
                inst.inst = InstructionTable.Ix;
                break;
            case 3:
                //inst.name = "CLR";
                inst.inst = InstructionTable.Iclr;
                break;
            case 4:
                //inst.name = "NEG";
                inst.inst = InstructionTable.Ineg;
                break;
            case 5:
                //inst.name = "INV";
                inst.inst = InstructionTable.Iinv;
                break;
            case 6:
                //inst.name = "INC";
                inst.inst = InstructionTable.Iinc;
                break;
            case 7:
                //inst.name = "INCT";
                inst.inst = InstructionTable.Iinct;
                break;
            case 8:
                //inst.name = "DEC";
                inst.inst = InstructionTable.Idec;
                break;
            case 9:
                //inst.name = "DECT";
                inst.inst = InstructionTable.Idect;
                break;
            case 10:
                //inst.name = "BL";
                inst.inst = InstructionTable.Ibl;
                break;
            case 11:
                //inst.name = "SWPB";
                inst.inst = InstructionTable.Iswpb;
                break;
            case 12:
                //inst.name = "SETO";
                inst.inst = InstructionTable.Iseto;
                break;
            case 13:
                //inst.name = "ABS";
                inst.inst = InstructionTable.Iabs;
                break;
            }
    
        } else if (op < 0xc00) {
            mop1.type = MachineOperand.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperand.OP_CNT;
            mop2.val = (op & 0xf0) >> 4;
    
            switch ((op & 0x700) >> 8) {
            case 0:
                //inst.name = "SRA";
                inst.inst = InstructionTable.Isra;
                break;
            case 1:
                //inst.name = "SRL";
                inst.inst = InstructionTable.Isrl;
                break;
            case 2:
                //inst.name = "SLA";
                inst.inst = InstructionTable.Isla;
                break;
            case 3:
                //inst.name = "SRC";
                inst.inst = InstructionTable.Isrc;
                break;
            }
    
        } else if (op < 0x1000) {
            switch ((op & 0x1e0) >> 5) {
        	// 0xc00
    		case 0:				/* DSR, OP_DSR */
    			inst.inst = InstructionTable.Idsr;
    			break;
    			// 0xd60
    		case 11:			/* TICKS */
    			inst.inst = InstructionTable.Iticks;
    			mop1.type = MachineOperand.OP_REG;
                mop1.val = (byte) (op & 0xf);
    			break;

            // TODO: extended instructions
            }
    
        } else if (op < 0x2000) {
            mop1.type = MachineOperand.OP_IMMED;
            mop1.val = (byte) (op & 0xff);
            if (op < 0x1D00) {
                mop1.val = (mop1.val << 1) + 2;
                mop1.type = MachineOperand.OP_JUMP;
            } else {
            	mop1.type = MachineOperand.OP_OFFS_R12;
            }
    
            switch ((op & 0xf00) >> 8) {
            case 0:
                //inst.name = "JMP";
                inst.inst = InstructionTable.Ijmp;
                break;
            case 1:
                //inst.name = "JLT";
                inst.inst = InstructionTable.Ijlt;
                break;
            case 2:
                //inst.name = "JLE";
                inst.inst = InstructionTable.Ijle;
                break;
            case 3:
                //inst.name = "JEQ";
                inst.inst = InstructionTable.Ijeq;
                break;
            case 4:
                //inst.name = "JHE";
                inst.inst = InstructionTable.Ijhe;
                break;
            case 5:
                //inst.name = "JGT";
                inst.inst = InstructionTable.Ijgt;
                break;
            case 6:
                //inst.name = "JNE";
                inst.inst = InstructionTable.Ijne;
                break;
            case 7:
                //inst.name = "JNC";
                inst.inst = InstructionTable.Ijnc;
                break;
            case 8:
                //inst.name = "JOC";
                inst.inst = InstructionTable.Ijoc;
                break;
            case 9:
                //inst.name = "JNO";
                inst.inst = InstructionTable.Ijno;
                break;
            case 10:
                //inst.name = "JL";
                inst.inst = InstructionTable.Ijl;
                break;
            case 11:
                //inst.name = "JH";
                inst.inst = InstructionTable.Ijh;
                break;
            case 12:
                //inst.name = "JOP";
                inst.inst = InstructionTable.Ijop;
                break;
            case 13:
                //inst.name = "SBO";
                inst.inst = InstructionTable.Isbo;
                break;
            case 14:
                //inst.name = "SBZ";
                inst.inst = InstructionTable.Isbz;
                break;
            case 15:
                //inst.name = "TB";
                inst.inst = InstructionTable.Itb;
                break;
            }
    
        } else if (op < 0x4000 && !(op >= 0x3000 && op < 0x3800)) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = MachineOperand.OP_REG;
            mop2.val = (op & 0x3c0) >> 6;
    
            switch ((op & 0x1c00) >> 10) {
            case 0:
                //inst.name = "COC";
                inst.inst = InstructionTable.Icoc;
                break;
            case 1:
                //inst.name = "CZC";
                inst.inst = InstructionTable.Iczc;
                break;
            case 2:
                //inst.name = "XOR";
                inst.inst = InstructionTable.Ixor;
                break;
            case 3:
                //inst.name = "XOP";
                inst.inst = InstructionTable.Ixop;
                break;
            case 6:
                //inst.name = "MPY";
                inst.inst = InstructionTable.Impy;
                break;
            case 7:
                //inst.name = "DIV";
                inst.inst = InstructionTable.Idiv;
                break;
            }
    
        } else if (op >= 0x3000 && op < 0x3800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = MachineOperand.OP_CNT;
            mop2.val = (op & 0x3c0) >> 6;
    
            if (op < 0x3400) {
                //inst.name = "LDCR";
                inst.inst = InstructionTable.Ildcr;
            } else {
                //inst.name = "STCR";
                inst.inst = InstructionTable.Istcr;
            }
    
        } else {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = (op & 0x0c00) >> 10;
            mop2.val = (op & 0x3c0) >> 6;
    
            switch ((op & 0xf000) >> 12) {
            case 4:
                //inst.name = "SZC";
                inst.inst = InstructionTable.Iszc;
                break;
            case 5:
                //inst.name = "SZCB";
                inst.inst = InstructionTable.Iszcb;
                break;
            case 6:
                //inst.name = "S";
                inst.inst = InstructionTable.Is;
                break;
            case 7:
                //inst.name = "SB";
                inst.inst = InstructionTable.Isb;
                break;
            case 8:
                //inst.name = "C";
                inst.inst = InstructionTable.Ic;
                break;
            case 9:
                //inst.name = "CB";
                inst.inst = InstructionTable.Icb;
                break;
            case 10:
                //inst.name = "A";
                inst.inst = InstructionTable.Ia;
                break;
            case 11:
                //inst.name = "AB";
                inst.inst = InstructionTable.Iab;
                break;
            case 12:
                //inst.name = "MOV";
                inst.inst = InstructionTable.Imov;
                break;
            case 13:
                //inst.name = "MOVB";
                inst.inst = InstructionTable.Imovb;
                break;
            case 14:
                //inst.name = "SOC";
                inst.inst = InstructionTable.Isoc;
                break;
            case 15:
                //inst.name = "SOCB";
                inst.inst = InstructionTable.Isocb;
                break;
            }
        }
    
        if (inst.inst == 0) // data
        {
            mop1.type = MachineOperand.OP_IMMED;
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
        }

        return inst;
    }
    
    public static void calculateInstructionSize(RawInstruction target) {
    	if (target.inst == InstructionTable.Idata) {
    		target.size = 2;
    		return;
    	} else if (target.inst == InstructionTable.Ibyte) {
    		target.size = 1;
    		return;
    	}
    	target.size = 2;
    	InstEncodePattern pattern = lookupEncodePattern(target.inst);
		if (pattern == null)
			return;
		
		if ((pattern.op1 == GEN && ((MachineOperand)target.op1).type == MachineOperand.OP_ADDR)
				|| (pattern.op1 == IMM)) 
			target.size += 2;
		if ((pattern.op2 == GEN && ((MachineOperand)target.op2).type == MachineOperand.OP_ADDR)
				|| (pattern.op2 == IMM)) 
			target.size += 2;
	
    }
	public static RawInstruction createDataInstruction(short immed) {
		RawInstruction inst = new RawInstruction();
		//inst.name = "DATA";
		inst.inst = Idata;
		inst.opcode = immed;
		inst.op1 = MachineOperand.createImmediate(immed);
		inst.op2 = MachineOperand.createEmptyOperand();
		inst.size = 2;
		return inst;
	}
	public static RawInstruction createDataInstruction(MachineOperand mop) {
		RawInstruction inst = createDataInstruction(mop.immed);
		((MachineOperand)inst.op1).symbol = mop.symbol;
		((MachineOperand)inst.op1).symbolResolved = mop.symbolResolved;
		return inst;
	}
	
	public static RawInstruction createByteInstruction(short immed) {
		RawInstruction inst = new RawInstruction();
		//inst.name = "BYTE";
		inst.inst = Ibyte;
		inst.opcode = immed;
		inst.op1 = MachineOperand.createImmediate(immed);
		inst.op2 = MachineOperand.createEmptyOperand();
		inst.size = 1;
		return inst;
	}
	public static RawInstruction createByteInstruction(MachineOperand mop) {
		RawInstruction inst = createByteInstruction(mop.immed);
		((MachineOperand)inst.op1).symbol = mop.symbol;
		((MachineOperand)inst.op1).symbolResolved = mop.symbolResolved;
		return inst;
	}
}
