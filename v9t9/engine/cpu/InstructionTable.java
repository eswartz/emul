/**
 * 
 */
package v9t9.engine.cpu;

import java.util.HashMap;
import java.util.Map;

import static v9t9.engine.cpu.InstEncodePattern.*;

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
		register(Instruction.Idata, 0x0000, IMM_NONE, 0xffff);

		register(Instruction.Ili, 0x0200, REG_IMM, 0x20f);
		register(Instruction.Iai, 0x0220, REG_IMM, 0x22f);
		register(Instruction.Iandi, 0x0240, REG_IMM, 0x24f);
		register(Instruction.Iori, 0x0260, REG_IMM, 0x26f);
		register(Instruction.Ici, 0x0280, REG_IMM, 0x28f);

		register(Instruction.Istwp, 0x02a0, REG_NONE, 0x2af);
		register(Instruction.Istst, 0x02c0, REG_NONE, 0x2cf);
		register(Instruction.Ilwpi, 0x02e0, IMM_NONE);
		register(Instruction.Ilimi, 0x0300, IMM_NONE);
		register(Instruction.Iidle, 0x0340, NONE_NONE);
		register(Instruction.Irset, 0x0360, NONE_NONE);
		register(Instruction.Irtwp, 0x0380, NONE_NONE);
		register(Instruction.Ickon, 0x03a0, NONE_NONE);
		register(Instruction.Ickof, 0x03c0, NONE_NONE);
		register(Instruction.Ilrex, 0x03e0, NONE_NONE);

		register(Instruction.Iblwp, 0x0400, GEN_NONE);
		register(Instruction.Ib, 0x0440, GEN_NONE);
		register(Instruction.Ix, 0x0480, GEN_NONE);
		register(Instruction.Iclr, 0x04c0, GEN_NONE);
		register(Instruction.Ineg, 0x0500, GEN_NONE);
		register(Instruction.Iinv, 0x0540, GEN_NONE);
		register(Instruction.Iinc, 0x0580, GEN_NONE);
		register(Instruction.Iinct, 0x05c0, GEN_NONE);
		register(Instruction.Idec, 0x0600, GEN_NONE);
		register(Instruction.Idect, 0x0640, GEN_NONE);
		register(Instruction.Ibl, 0x0680, GEN_NONE);
		register(Instruction.Iswpb, 0x06c0, GEN_NONE);
		register(Instruction.Iseto, 0x0700, GEN_NONE);
		register(Instruction.Iabs, 0x0740, GEN_NONE);

		register(Instruction.Isra, 0x0800, REG_CNT);
		register(Instruction.Isrl, 0x0900, REG_CNT);
		register(Instruction.Isla, 0x0a00, REG_CNT);
		register(Instruction.Isrc, 0x0b00, REG_CNT);

		register(Instruction.Idsr, 0x0c00, CNT_NONE, 0xc00);
		register(Instruction.Ikysl, 0x0d40, NONE_NONE, 0xd40);
		register(Instruction.Iticks, 0x0d60, REG_NONE, 0xd60);
		register(Instruction.Iemitchar, 0x0dc0, REG_CNT, 0xdcf);
		register(Instruction.Idbg, 0x0de0, CNT_NONE, 0xde1);

		register(Instruction.Ijmp, 0x1000, JMP_NONE);
		register(Instruction.Ijlt, 0x1100, JMP_NONE);
		register(Instruction.Ijle, 0x1200, JMP_NONE);
		register(Instruction.Ijeq, 0x1300, JMP_NONE);
		register(Instruction.Ijhe, 0x1400, JMP_NONE);
		register(Instruction.Ijgt, 0x1500, JMP_NONE);
		register(Instruction.Ijne, 0x1600, JMP_NONE);
		register(Instruction.Ijnc, 0x1700, JMP_NONE);
		register(Instruction.Ijoc, 0x1800, JMP_NONE);
		register(Instruction.Ijno, 0x1900, JMP_NONE);
		register(Instruction.Ijl, 0x1a00, JMP_NONE);
		register(Instruction.Ijh, 0x1b00, JMP_NONE);
		register(Instruction.Ijop, 0x1c00, JMP_NONE);

		register(Instruction.Isbo, 0x1d00, OFF_NONE);
		register(Instruction.Isbz, 0x1e00, OFF_NONE);
		register(Instruction.Itb, 0x1f00, OFF_NONE);

		register(Instruction.Icoc, 0x2000, GEN6_REG);
		register(Instruction.Iczc, 0x2400, GEN6_REG);
		register(Instruction.Ixor, 0x2800, GEN6_REG);
		register(Instruction.Ixop, 0x2c00, GEN6_REG);
		
		register(Instruction.Ildcr, 0x3000, GEN_CNT);
		register(Instruction.Istcr, 0x3400, GEN_CNT);
		register(Instruction.Impy, 0x3800, GEN_REG);
		register(Instruction.Idiv, 0x3c00, GEN_REG);


		register(Instruction.Iszc, 0x4000, GEN_GEN);
		register(Instruction.Iszcb, 0x5000, GEN_GEN);
		register(Instruction.Is, 0x6000, GEN_GEN);
		register(Instruction.Isb, 0x7000, GEN_GEN);
		register(Instruction.Ic, 0x8000, GEN_GEN);
		register(Instruction.Icb, 0x9000, GEN_GEN);
		register(Instruction.Ia, 0xa000, GEN_GEN);
		register(Instruction.Iab, 0xb000, GEN_GEN);
		register(Instruction.Imov, 0xc000, GEN_GEN);
		register(Instruction.Imovb, 0xd000, GEN_GEN);
		register(Instruction.Isoc, 0xe000, GEN_GEN);
		register(Instruction.Isocb, 0xf000, GEN_GEN);

	};

	public static short[] encode(Instruction inst) throws IllegalArgumentException {
		Integer opcodeI = instOpcodes.get(inst.inst);
		if (opcodeI == null)
			throw new IllegalArgumentException("Non-machine instruction");
		int opcode = opcodeI;
		
		InstEncodePattern pattern = instEntries.get(inst.inst);
		if (pattern == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (!(inst.op1 instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 1: " + inst.op1);
		if (!(inst.op2 instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 2: " + inst.op1);
		
		inst.size = 2;	// at least (for jumps)
		coerceOperandTypes(inst);
		
		MachineOperand mop1 = (MachineOperand) inst.op1;
		MachineOperand mop2 = (MachineOperand) inst.op2;
		
		assertOperandMatches(mop1, pattern.op1);
		assertOperandMatches(mop2, pattern.op2);
		
		opcode |= (mop2.getBits() << pattern.off1) | mop1.getBits();
		
		short[] words = { (short)opcode, 0, 0 };
		int wordCount = inst.inst != Instruction.Idata ? 1 : 0;
		if (mop1.hasImmediate()) {
			words[wordCount++] = mop1.immed;
		}
		if (mop2.hasImmediate()) {
			words[wordCount++] = mop2.immed;
		}
		
		inst.size = wordCount * 2;
		
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
	 * @param inst
	 * @param finalPass if true, performs final coercions before generating
	 * machine code; otherwise, just overcomes 
	 */
	public static void coerceOperandTypes(Instruction inst) {
		InstEncodePattern pattern = instEntries.get(inst.inst);
		if (pattern == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (inst.op1 instanceof MachineOperand) {
			coerceOperandType(inst, (MachineOperand) inst.op1, pattern.op1);
		}
		if (inst.op2 instanceof MachineOperand) {
			coerceOperandType(inst, (MachineOperand) inst.op2, pattern.op2);
		}		
		
	}
	private static void coerceOperandType(Instruction inst, MachineOperand mop, int op) {
		switch (op) {
		case NONE:
			if (mop.type == MachineOperand.OP_STATUS
					|| mop.type == MachineOperand.OP_INST
					|| (inst.inst >= Instruction.Iinc && inst.inst <= Instruction.Idect))
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
			if (inst.isJumpInst()) {
				if (mop.type == MachineOperand.OP_IMMED) {
					// convert address to offset from this inst
					mop.type = MachineOperand.OP_JUMP;
					mop.val = (mop.val - inst.pc);
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
}
