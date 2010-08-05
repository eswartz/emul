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

import java.util.HashMap;
import java.util.Map;

import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 * 
 */
public class InstTableMFP201 {
	public final static byte _IMM8 = 0x10;
	public final static byte _IMM16 = 0x11;
	public final static byte _IMM8_16 = 0x12;
	public final static byte _OFF16 = 0x13;
	/** set bit if the operand size is a byte, at the bit specified in lower nybble */
	public final static byte _SZ = 0x20;
	/** set bit if the immediate size is a byte, at the bit specified in lower nybble */
	public final static byte _IMM_SZ = 0x30;
	/** set bit if the immediate size is a byte, at the bit specified in lower nybble, of byte 0 */
	public final static byte _IMM_SZ_0 = 0x40;
	/** skip to the next byte */
	public final static byte _NEXTB = -2;
	/** skip to the next operand */
	public final static byte _NEXTO = -3;
	/** skip to the next byte and operand */
	public final static byte _NEXT = -4;
	/** emit the opcode */
	public final static byte _OPC = -1;
	
	final static InstPatternMFP201 DATA_IMM8 = new InstPatternMFP201(
			IMM, new byte[] { _IMM8 });
	final static InstPatternMFP201 DATA_IMM16 = new InstPatternMFP201(
			IMM, new byte[] { _IMM16 });
	final static InstPatternMFP201 NONE_ = new InstPatternMFP201(
			NONE, new byte[] { _OPC } );
	final static InstPatternMFP201 IMM_ = new InstPatternMFP201(
			IMM, new byte[] { _OPC, _NEXT, _IMM16 });
	final static InstPatternMFP201 OFF_ = new InstPatternMFP201(
			OFF, new byte[] { _OPC, _NEXT, _OFF16 });
	final static InstPatternMFP201 REG_IMMx = new InstPatternMFP201(
			REG, IMM, new byte[] { _OPC, 0, _NEXTO, _IMM_SZ_0 | 0, _NEXTB, _IMM8_16 });

	static Map<Integer, InstPatternMFP201> instEntries = new HashMap<Integer, InstPatternMFP201>();
	static Map<Integer, Integer> instOpcodes = new HashMap<Integer, Integer>();
	static Map<Integer, Integer> instMasks = new HashMap<Integer, Integer>();

	private static void register(int inst, int opcode, InstPatternMFP201 entry, int mask) {
		instEntries.put(inst, entry);
		instOpcodes.put(inst, opcode);
		instMasks.put(inst, mask);
	}
	static {
		register(InstTableCommon.Idata, 0x0000, DATA_IMM16, 0xffff);
		register(InstTableCommon.Ibyte, 0x0000, DATA_IMM8, 0xffff);

		register(InstMFP201.Ibkpt, 0x0, NONE_, 0x0);
		register(InstMFP201.Iret, 0x1, NONE_, 0x1);
		register(InstMFP201.Ireti, 0x2, NONE_, 0x2);
		register(InstMFP201.Ibr, 0x4, OFF_, 0x4);
		register(InstMFP201.Ibra, 0x5, IMM_, 0x5);
		register(InstMFP201.Icall, 0x6, OFF_, 0x6);
		register(InstMFP201.Icalla, 0x7, IMM_, 0x7);
		
		register(InstMFP201.Iorc, 0x800, REG_IMMx, 0x90f);
		register(InstMFP201.Iorcq, 0x810, REG_IMMx, 0x91f);
		register(InstMFP201.Iandc, 0x820, REG_IMMx, 0x92f);
		register(InstMFP201.Itstc, 0x830, REG_IMMx, 0x93f);
		register(InstMFP201.Inandc, 0x840, REG_IMMx, 0x94f);
		register(InstMFP201.Itstnc, 0x850, REG_IMMx, 0x95f);
		register(InstMFP201.Ixorc, 0x860, REG_IMMx, 0x96f);
		register(InstMFP201.Ixorcq, 0x870, REG_IMMx, 0x97f);
		register(InstMFP201.Iaddc, 0x880, REG_IMMx, 0x98f);
		register(InstMFP201.Iaddcq, 0x890, REG_IMMx, 0x99f);
		register(InstMFP201.Isubc, 0x8a0, REG_IMMx, 0x9af);
		register(InstMFP201.Icmpc, 0x8b0, REG_IMMx, 0x9bf);
		register(InstMFP201.Iadcc, 0x8c0, REG_IMMx, 0x9cf);
		register(InstMFP201.Iadccq, 0x8d0, REG_IMMx, 0x9df);
		register(InstMFP201.Ildc, 0x8e0, REG_IMMx, 0x9ef);
		register(InstMFP201.Ildcq, 0x8f0, REG_IMMx, 0x9ff);
		
		/*
		register(InstTableCommon.Idsr, 0x0c00, OFF_NONE, 0xcff);
		register(InstTableCommon.Ikysl, 0x0d40, NONE_, 0xd40);
		register(InstTableCommon.Iticks, 0x0d60, REG_NONE, 0xd60);
		register(InstTableCommon.Iemitchar, 0x0dc0, REG_CNT, 0xdcf);
		register(InstTableCommon.Idbg, 0x0de0, NONE_, 0xde0);
		register(InstTableCommon.Idbgf, 0x0de1, NONE_, 0xde1);
		 */

	};

	public static byte[] encode(RawInstruction rawInst) throws IllegalArgumentException {
		int inst = rawInst.getInst();
		Integer opcodeI = instOpcodes.get(inst);
		if (opcodeI == null)
			throw new IllegalArgumentException("Non-machine instruction");
		int opcode = opcodeI;
		
		InstPatternMFP201 pattern = instEntries.get(inst);
		if (pattern == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (!(rawInst.getOp1() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 1: " + rawInst.getOp1());
		if (!(rawInst.getOp2() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 2: " + rawInst.getOp2());
		if (!(rawInst.getOp3() instanceof MachineOperand))
			throw new IllegalArgumentException("Non-machine operand 3: " + rawInst.getOp3());
		
		coerceOperandTypes(rawInst);
		
		MachineOperandMFP201 mop1 = (MachineOperandMFP201) rawInst.getOp1();
		MachineOperandMFP201 mop2 = (MachineOperandMFP201) rawInst.getOp2();
		MachineOperandMFP201 mop3 = (MachineOperandMFP201) rawInst.getOp3();
		
		assertOperandMatches(mop1, pattern.op1);
		assertOperandMatches(mop2, pattern.op2);
		assertOperandMatches(mop3, pattern.op3);
		
		byte memSize = 0;

		MachineOperandMFP201[] mops = { mop1, mop2, mop3 };
		byte[] work = { 0, 0, 0, 0, 0, 0 };
		
		int mopIdx = 0;
		int workIdx = 0;
		
		MachineOperandMFP201 mop = null;

		for (int idx = 0; idx < pattern.enc.length; idx++) {
			byte enc = pattern.enc[idx];
			if (enc == _OPC) {
				if (opcode >= 0x100) {
					work[workIdx++] = (byte) (opcode >> 8);
				}
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
				}

				if (enc < 8) {
					work[workIdx] |= (byte) (mop.val & 0xf); 
				}
				else if (enc == _IMM8) {
					work[workIdx] = (byte) (mop.immed & 0xff); 
				}
				else if (enc == _IMM16) {
					work[workIdx++] = (byte) ((mop.immed & 0xff00) >> 8); 
					work[workIdx] = (byte) (mop.immed & 0xff); 
				}
				else if (enc == _IMM8_16) {
					if (!isImm8(mop.immed))
						work[workIdx++] = (byte) ((mop.immed & 0xff00) >> 8);
					work[workIdx] = (byte) (mop.immed & 0xff);
				}
				else if (enc == _OFF16) {
					int val = mop.immed - workIdx - rawInst.pc; 
					work[workIdx++] = (byte) ((val & 0xff00) >> 8); 
					work[workIdx] = (byte) (val & 0xff); 
				}
				else if ((enc & 0xf0) == _IMM_SZ) {	
					if (isImm8(mop.immed))
						work[workIdx] |= (byte) (1 << (enc & 0xf));
				}
				else if ((enc & 0xf0) == _IMM_SZ_0) {	
					if (isImm8(mop.immed))
						work[0] |= (byte) (1 << (enc & 0xf));
				}
			}
		}
		workIdx++;
		
		int offs = (memSize != 0 ? 1 : 0);
		byte[] bytes = new byte[workIdx + offs];
		System.arraycopy(work, 0, bytes, offs, workIdx);
		if (offs != 0)
			bytes[0] = memSize;
		
		return bytes;
	}

	/**
	 * @param immed
	 * @return
	 */
	public static boolean isImm8(short immed) {
		return (byte) (immed & 0xff) == immed;
	}

	private static void assertOperandMatches(BaseMachineOperand mop, int op) {
		switch (op) {
		case NONE:
			if (mop.type != MachineOperand.OP_NONE)
				throw new IllegalArgumentException("Unexpected operand: " + mop);
			break;
		case IMM:
			if (mop.type != MachineOperandMFP201.OP_IMM)
				throw new IllegalArgumentException("Expected immediate: " + mop);
			break;
		case CNT:
			if (mop.type != MachineOperandMFP201.OP_CNT && mop.type != MachineOperandMFP201.OP_IMM)
				throw new IllegalArgumentException("Expected count: " + mop);
			break;
		case OFF:
			if (mop.type != MachineOperandMFP201.OP_CNT && mop.type != MachineOperandMFP201.OP_IMM
					&& mop.type != MachineOperandMFP201.OP_PCREL)
				throw new IllegalArgumentException("Expected offset: " + mop);
			break;
		case REG:
			if (mop.type != MachineOperandMFP201.OP_REG && mop.type != MachineOperandMFP201.OP_REG0_SHIFT_COUNT)
				throw new IllegalArgumentException("Expected register: " + mop);
			break;
		case GEN:
			if (mop.type == MachineOperandMFP201.OP_NONE)
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
		InstPatternMFP201 pattern = instEntries.get(instruction.getInst());
		if (pattern == null)
			throw new IllegalArgumentException("Non-encoded instruction");
		
		if (instruction.getOp1() instanceof MachineOperand) {
			coerceOperandType(instruction, (BaseMachineOperand) instruction.getOp1(), pattern.op1);
		}
		if (instruction.getOp2() instanceof MachineOperand) {
			coerceOperandType(instruction, (BaseMachineOperand) instruction.getOp2(), pattern.op2);
		}		
		if (instruction.getOp3() instanceof MachineOperand) {
			coerceOperandType(instruction, (BaseMachineOperand) instruction.getOp3(), pattern.op3);
		}		
		
	}
	private static void coerceOperandType(RawInstruction instruction, BaseMachineOperand mop, int op) {
		switch (op) {
		case NONE:
			break;
		case IMM:
			if (mop.type == MachineOperandMFP201.OP_REG)
				mop.type = MachineOperandMFP201.OP_IMM;
			break;
		case CNT:
			if (mop.type == MachineOperandMFP201.OP_REG
					|| mop.type == MachineOperandMFP201.OP_IMM
					|| mop.type == MachineOperandMFP201.OP_REG0_SHIFT_COUNT)
				mop.type = MachineOperandMFP201.OP_CNT;
			if (mop.val == 16)
				mop.val = 0;
			break;
		case OFF:
			if (mop.type == MachineOperandMFP201.OP_IMM) {
				// convert address to offset from this inst
				mop.type = MachineOperandMFP201.OP_PCREL;
				mop.val = (mop.val - instruction.pc);
			}
			break;
		case REG:
			if (mop.type == MachineOperandMFP201.OP_IMM)
				mop.type = MachineOperandMFP201.OP_REG;
			break;
		case GEN:
			if (mop.type == MachineOperandMFP201.OP_IMM) {
				// immediate is *PC+
				mop.type = MachineOperandMFP201.OP_INC;
				mop.val = MachineOperandMFP201.PC;
			}
			else if (mop.type == MachineOperandMFP201.OP_PCREL) {
				// PC-rel is @x(PC)
				mop.type = MachineOperandMFP201.OP_OFFS;
				mop.immed = (short) mop.immed;
				mop.val = MachineOperandMFP201.PC;
			}
			break;
		}
	}
	
	public static int coerceInstructionOpcode(int inst, int opcode) {
		Integer mask = instMasks.get(inst);
		if (mask == null)
			return opcode;
		return opcode & mask;
	}
	public static InstPatternMFP201 lookupEncodePattern(int inst) {
		return instEntries.get(inst);
	}
	
	public static Map<String, Integer> nameToInst = new HashMap<String, Integer>();
	public static Map<Integer, String> instToName = new HashMap<Integer, String>();
	
	static { registerInstruction(InstTableCommon.Idata, "data"); }
	static { registerInstruction(InstTableCommon.Ibyte, "byte"); }
	static { registerInstruction(InstMFP201.Ibkpt, "bkpt"); }
	static { registerInstruction(InstMFP201.Iret, "ret"); }
	static { registerInstruction(InstMFP201.Ireti, "reti"); }
	static { registerInstruction(InstMFP201.Ibr, "br"); }
	static { registerInstruction(InstMFP201.Ibra, "bra"); }
	static { registerInstruction(InstMFP201.Icall, "call"); }
	static { registerInstruction(InstMFP201.Icalla, "calla"); }
	
	static { registerInstruction(InstMFP201.Iorc, "orc"); }
	static { registerInstruction(InstMFP201.Iandc, "andc"); }
	static { registerInstruction(InstMFP201.Inandc, "nandc"); }
	static { registerInstruction(InstMFP201.Ixorc, "xorc"); }
	static { registerInstruction(InstMFP201.Iaddc, "addc"); }
	static { registerInstruction(InstMFP201.Isubc, "subc"); }
	static { registerInstruction(InstMFP201.Iadcc, "adcc"); }
	static { registerInstruction(InstMFP201.Ildc, "ldc"); }
	
	static { registerInstruction(InstMFP201.Iorcq, "orc?"); }
	static { registerInstruction(InstMFP201.Itstc, "tstc"); }
	static { registerInstruction(InstMFP201.Itstnc, "tstnc"); }
	static { registerInstruction(InstMFP201.Ixorcq, "xorc?"); }
	static { registerInstruction(InstMFP201.Iaddcq, "addc?"); }
	static { registerInstruction(InstMFP201.Icmpc, "cmpc"); }
	static { registerInstruction(InstMFP201.Iadccq, "adcc?"); }
	static { registerInstruction(InstMFP201.Ildcq, "ldc?"); }
	
	static { registerInstruction(InstTableCommon.Idsr, "dsr"); }
	static { registerInstruction(InstTableCommon.Ikysl, "kysl"); }
	static { registerInstruction(InstTableCommon.Iticks, "ticks"); }
	static { registerInstruction(InstTableCommon.Iemitchar, "emitchar"); }
	static { registerInstruction(InstTableCommon.Idbg, "dbg"); }
	static { registerInstruction(InstTableCommon.Idbgf, "dbgf"); }
	static { registerInstruction(InstTableCommon.Ibyte, "byte"); }

	//static { registerAlias(InstMFP201.Ijeq, "je"); }
	//static { registerAlias(InstMFP201.Ijoc, "jc"); }

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
    
    public static void calculateInstructionSize(RawInstruction target) {
    	if (target.getInst() == InstTableCommon.Idata) {
    		target.size = 2;
    		return;
    	} else if (target.getInst() == InstTableCommon.Ibyte) {
    		target.size = 1;
    		return;
    	}
    	target.size = 0;
    	InstPatternMFP201 pattern = lookupEncodePattern(target.getInst());
		if (pattern == null)
			return;
		
		byte[] bytes = encode(target);
		target.size += bytes.length;
    }
    
	public static boolean isJumpInst(int inst) {
		//return inst >= InstMFP201.Ijmp && inst <= InstMFP201.Ijop;
		return false;
	}
	public static boolean isByteInst(int inst) {
		return false;
		/*
		return inst == InstMFP201.Isocb 
		|| inst == InstMFP201.Icb 
		|| inst == InstMFP201.Iab 
		|| inst == InstMFP201.Isb 
		|| inst == InstMFP201.Iszcb 
		|| inst == InstMFP201.Imovb;
		*/
	}
}
