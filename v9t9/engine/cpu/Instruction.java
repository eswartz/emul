/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.engine.cpu;


import v9t9.engine.memory.MemoryDomain;
import v9t9.utils.Check;

/**
 * @author ejs
 */
public class Instruction extends RawInstruction implements IInstruction {
    // Status setting flags
    public static final int st_NONE = 0; // status not affected

    public static final int st_ALL = 1; // all bits changed

    public static final int st_INT = 2; // interrupt mask

    public static final int st_XOP = 3; // xop bits changed

    public static final int st_CMP = 4; // comparison

    public static final int st_BYTE_CMP = 5; // with bytes

    public static final int st_LAE = 6; // arithmetic...

    public static final int st_LAEO = 7;

    public static final int st_O = 8;

    public static final int st_E = 11;

    public static final int st_BYTE_LAEP = 12;

    public static final int st_SUB_LAECO = 13;

    public static final int st_SUB_BYTE_LAECOP = 14;

    public static final int st_ADD_LAECO = 15;

    public static final int st_ADD_BYTE_LAECOP = 16;

    public static final int st_SHIFT_RIGHT_C = 17;

    public static final int st_SHIFT_LEFT_CO = 18;

    public static final int st_DIV_O = 19;

    public static final int st_LAE_1 = 20;

    public static final int st_BYTE_LAEP_1 = 21;

    public static final int st_ADD_LAECO_REV = 22;

    /** Get the status bits that 'st' (st_XXX) modifies */
    public static int getStatusBits(int st) {
        switch (st) {
        case st_NONE: return 0;
        case st_ALL: return 0xffff;
        case st_INT: return Status.ST_INTLEVEL;
        case st_XOP: return Status.ST_X;
        case st_CMP: return Status.ST_L + Status.ST_A + Status.ST_E;
        case st_BYTE_CMP: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_P;
        case st_LAE: return Status.ST_L + Status.ST_A + Status.ST_E;
        case st_LAEO: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_O;
        case st_O: return Status.ST_O;
        case st_E: return Status.ST_E;
        case st_BYTE_LAEP: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_P;
        case st_SUB_LAECO: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_C + Status.ST_O;
        case st_SUB_BYTE_LAECOP: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_C + Status.ST_O + Status.ST_P;
        case st_ADD_LAECO: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_C + Status.ST_O;
        case st_ADD_BYTE_LAECOP: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_C + Status.ST_O + Status.ST_P;
        case st_SHIFT_RIGHT_C: return Status.ST_C;
        case st_SHIFT_LEFT_CO: return Status.ST_C + Status.ST_O;
        case st_DIV_O: return Status.ST_O;
        case st_LAE_1: return Status.ST_L + Status.ST_A + Status.ST_E;
        case st_BYTE_LAEP_1: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_P;
        case st_ADD_LAECO_REV: return Status.ST_L + Status.ST_A + Status.ST_E + Status.ST_C + Status.ST_O;
        default: throw new AssertionError("bad st_XXX value");
        }
    }
    
    // instruction jump flags

    /** Instruction does not jump */
    public static final int INST_JUMP_FALSE = 0;

    /** Instruction always jumps */
    public static final int INST_JUMP_TRUE = 1;

    /** Instruction jumps conditionally */
    public static final int INST_JUMP_COND = 2;

    // instruction reader/writer flags
    
    public static final int INST_RSRC_PC = 1;	// program counter
    public static final int INST_RSRC_WP = 2;	// workspace pointer
    public static final int INST_RSRC_ST = 4;	// status
    public static final int INST_RSRC_IO = 8;	// I/O
    public static final int INST_RSRC_EMU = 16; // emulator itself (builtin)
    public static final int INST_RSRC_CTX = 32;	// context switch (writer only)
    
    // fields

    public Status status; // current status

    public short cycles; // execution cycles (not including memory operands)

    public int stsetBefore; // method status is set after operands parsed, before execution (st_xxx)
    public int stsetAfter; // method status is set after execution (st_xxx)
    public int stReads;     // bits read by instruction (Status.ST_xxx mask)
    /** operand is a jump (INST_JUMP_COND = conditional) */
    public int jump; 

    public int reads, writes;	// what resources (INST_RSRC_xxx) are read and written?
    
    InstructionAction action;

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(name);
        String opstring;
        opstring = op1.toString();
        if (opstring != null) {
            buffer.append(' ');
            buffer.append(opstring);
            opstring = op2.toString();
            if (opstring != null) {
                buffer.append(',');
                buffer.append(opstring);
            }
        }
        return buffer.toString();
    }

    public interface WordReader {
        short readWord(int addr);
    }

    public interface WordWriter {
        void writeWord(int addr, short val);
    }

    public interface ByteReader {
        byte readByte(int addr);
    }

    /*
	thierry times for address decodes:
	"memory access" means if the memory is over the PE box link;
	this is four cycles each!

   	Address mode	Clock cycles 	Memory access
   	Rx				0  				0
   	*Rx 			4 				1
    *Rx+ (byte)		6				2
	     (word)		8				2
    @>xxxx 			8 				1
    @>xxxx(Rx) 		8 				2

	*/
    static public int ramBankCycles(int pc) {
        /* Penalties for accessing memory other than scratch pad or ROMs. */

        final int _mem_cycles[] = { 0, // 0x0000
                4, // 0x2000
                4, // 0x4000
                2, // 0x6000
                0, // 0x8000
                4, // 0xa000
                4, // 0xc000
                4, // 0xe000
        };
        return _mem_cycles[pc >> 13 & 7];
    }

    static public short getMemoryCycles(int pc, int base, int mem) {
        return (short) (base + mem * ramBankCycles(pc));
    }

    static public short getMemoryCycles(int pc) {
        return getMemoryCycles(pc, 0, 0);
    }

    public Instruction(int pc) {
        this.pc = (short) pc;
    }
    
 
    public Instruction(RawInstruction inst) {
    	super(inst);
    	completeInstruction(inst.pc);
    }
    public Instruction(Instruction inst) {
    	super(inst);
    	this.status = inst.status;
    	this.opcode = inst.opcode;
    	this.cycles = inst.cycles;
    	this.stsetBefore = inst.stsetBefore;
    	this.stsetAfter = inst.stsetAfter;
    	this.stReads = inst.stReads;
    	this.jump = inst.jump;
    	this.reads = inst.reads;
    	this.writes = inst.writes;
    	this.action = inst.action;
	}

 
	/**
     * Finish filling in an instruction which is used for
     * higher-level operations.  This also establishes basic
     * cycle counts.
     *
     */
    public void completeInstruction(int Pc) {        
    	this.cycles = getMemoryCycles(Pc);
	    this.stsetBefore = Instruction.st_NONE;
	    this.stsetAfter = Instruction.st_NONE;
	    this.stReads = 0;
	    this.jump = Instruction.INST_JUMP_FALSE;
	    this.reads = 0;
	    this.writes = 0;
	
	    MachineOperand mop1 = (MachineOperand) op1;
	    MachineOperand mop2 = (MachineOperand) op2;
	    
	    // Initially, this.op?.val is incomplete, and is whatever
	    // raw data from the opcode we can decode;
	    // this.op?.ea is that of the instruction or immediate
	    // if the operand needs it.
	
	    // after decoding the instruction, we complete
	    // the operand, making this.op?.val and this.op?.ea valid.
	
	    if (inst == InstructionTable.Idata) {
	        Check.checkArg(mop1.type == MachineOperand.OP_IMMED);
	        Check.checkArg(mop2.type == MachineOperand.OP_NONE);
	        Pc -= 2;
	        this.cycles += getMemoryCycles(Pc, 6, 1);
	    } else if (inst >= InstructionTable.Ili && inst <= InstructionTable.Ici) {
	        Check.checkArg(mop1.type == MachineOperand.OP_REG);
	        mop2.convertToImmedate();
	        mop1.dest = MachineOperand.OP_DEST_TRUE;
	        switch (inst) {
	        case InstructionTable.Ili:
	            this.stsetAfter = Instruction.st_LAE_1;
	            mop1.dest = MachineOperand.OP_DEST_KILLED;
	            this.cycles += getMemoryCycles(Pc, 12, 3);
	            break;
	        case InstructionTable.Iai:
	            this.stsetBefore = Instruction.st_ADD_LAECO_REV;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Iandi:
	            this.stsetAfter = Instruction.st_LAE_1;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Iori:
	            this.stsetAfter = Instruction.st_LAE_1;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Ici:
	            this.stsetAfter = Instruction.st_CMP;
	            mop1.dest = MachineOperand.OP_DEST_FALSE;
	            this.cycles += getMemoryCycles(Pc, 14, 3);
	            break;
	        }
	
	    } else if (inst == InstructionTable.Istwp) {
	        Check.checkArg(mop1.type == MachineOperand.OP_REG);
	        Check.checkArg(mop2.type == MachineOperand.OP_NONE);
	        mop1.dest = MachineOperand.OP_DEST_KILLED;
	        this.reads |= INST_RSRC_WP;
	        this.cycles += getMemoryCycles(Pc, 8, 2);
	    } else if (inst == InstructionTable.Istst) {
	        Check.checkArg(mop1.type == MachineOperand.OP_REG);
	        this.reads |= INST_RSRC_ST;
	        this.stReads = 0xffff;
	        mop1.dest = MachineOperand.OP_DEST_KILLED;
	        
	        Check.checkArg(mop2.type == MachineOperand.OP_NONE);
	        mop2.type = MachineOperand.OP_STATUS;
	        this.cycles += getMemoryCycles(Pc, 8, 2);
	    } else if (inst == InstructionTable.Ilwpi) {
	        Check.checkArg(mop1.type == MachineOperand.OP_IMMED);
	        Check.checkArg(mop2.type == MachineOperand.OP_NONE);
	        this.writes |= INST_RSRC_WP;
	        this.cycles += getMemoryCycles(Pc, 10, 2);
	    } else if (inst == InstructionTable.Ilimi) {
	        Check.checkArg(mop1.type == MachineOperand.OP_IMMED);
	        Check.checkArg(mop2.type == MachineOperand.OP_NONE);
	        this.stsetAfter = Instruction.st_INT;
	        this.cycles += getMemoryCycles(Pc, 16, 2);
	    } else if (inst >= InstructionTable.Iidle && inst <= InstructionTable.Ilrex) {
	        Check.checkArg(mop1.type == MachineOperand.OP_NONE);
	        Check.checkArg(mop2.type == MachineOperand.OP_NONE);
	        switch (inst) {
	        case InstructionTable.Iidle:
	            this.writes |= INST_RSRC_IO;
	            this.cycles += getMemoryCycles(Pc, 12, 1);
	            break;
	        case InstructionTable.Irset:
	            this.stsetAfter = Instruction.st_INT;
	            this.writes |= INST_RSRC_IO;
	            this.cycles += getMemoryCycles(Pc, 12, 1);
	            break;
	        case InstructionTable.Irtwp:
	            this.stsetAfter = Instruction.st_ALL;
	            this.writes |= INST_RSRC_WP + INST_RSRC_ST + INST_RSRC_PC;
	            mop1.type = MachineOperand.OP_STATUS;
	            mop1.dest = MachineOperand.OP_DEST_KILLED;
	            //((MachineOperand) this.op1).val = st.flatten();
	            this.jump = Instruction.INST_JUMP_TRUE;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Ickon:
	            this.writes |= INST_RSRC_IO;
	            this.cycles += getMemoryCycles(Pc, 12, 1);
	            break;
	        case InstructionTable.Ickof:
	            this.writes |= INST_RSRC_IO;
	            this.cycles += getMemoryCycles(Pc, 12, 1);
	            break;
	        case InstructionTable.Ilrex:
	            this.writes |= INST_RSRC_IO;
	            this.cycles += getMemoryCycles(Pc, 12, 1);
	            break;
	        }
	
	    } else if (inst >= InstructionTable.Iblwp && inst <= InstructionTable.Iabs) {
	        Check.checkArg(mop1.type != MachineOperand.OP_NONE
	                && mop1.type != MachineOperand.OP_IMMED);
	        Check.checkArg(mop2.type == MachineOperand.OP_NONE);
	        mop1.dest = MachineOperand.OP_DEST_TRUE;
	
	        switch (inst) {
	        case InstructionTable.Iblwp:
	            //this.stsetBefore = Instruction.st_ALL;
	            this.stReads = 0xffff;
	            this.reads |= INST_RSRC_ST;
	            this.writes |= INST_RSRC_WP + INST_RSRC_PC + INST_RSRC_CTX;
	            mop1.dest = MachineOperand.OP_DEST_FALSE;
	            mop1.bIsCodeDest = true;
	            this.jump = Instruction.INST_JUMP_TRUE;
	            this.cycles += getMemoryCycles(Pc, 26, 6);
	            break;
	        case InstructionTable.Ib:
	            mop1.dest = MachineOperand.OP_DEST_FALSE;
	            mop1.bIsCodeDest = true;
	            this.jump = Instruction.INST_JUMP_TRUE;
	            this.cycles += getMemoryCycles(Pc, 8, 2);
	            break;
	        case InstructionTable.Ix:
	            //this.stsetBefore = Instruction.st_ALL;
	            this.stReads = 0xffff;
	            this.reads |= INST_RSRC_ST;
	            mop1.dest = MachineOperand.OP_DEST_FALSE;
	            mop2.type = MachineOperand.OP_INST;
	            this.cycles += getMemoryCycles(Pc, 8, 2);
	            break;
	        case InstructionTable.Iclr:
	            mop1.dest = MachineOperand.OP_DEST_KILLED;
	            this.cycles += getMemoryCycles(Pc, 10, 3);
	            break;
	        case InstructionTable.Ineg:
	            this.stsetAfter = Instruction.st_LAEO;
	            this.cycles += getMemoryCycles(Pc, 12, 3);
	            break;
	        case InstructionTable.Iinv:
	            this.stsetAfter = Instruction.st_LAE_1;
	            this.cycles += getMemoryCycles(Pc, 10, 3);
	            break;
	        case InstructionTable.Iinc:
	            this.stsetBefore = Instruction.st_ADD_LAECO_REV;
	            mop2.type = MachineOperand.OP_CNT;
	            mop2.val = 1;
	            this.cycles += getMemoryCycles(Pc, 10, 3);
	            break;
	        case InstructionTable.Iinct:
	            this.stsetBefore = Instruction.st_ADD_LAECO_REV;
	            mop2.type = MachineOperand.OP_CNT;
	            mop2.val = 2;
	            this.cycles += getMemoryCycles(Pc, 10, 3);
	            break;
	        case InstructionTable.Idec:
	            this.stsetBefore = Instruction.st_ADD_LAECO_REV;
	            mop2.type = MachineOperand.OP_CNT;
	            mop2.val = -1;
	            this.cycles += getMemoryCycles(Pc, 10, 3);
	            break;
	        case InstructionTable.Idect:
	            this.stsetBefore = Instruction.st_ADD_LAECO_REV;
	            mop2.type = MachineOperand.OP_CNT;
	            mop2.val = -2;
	            this.cycles += getMemoryCycles(Pc, 10, 3);
	            break;
	        case InstructionTable.Ibl:
	            mop1.dest = MachineOperand.OP_DEST_FALSE;
	            mop1.bIsCodeDest = true;
	            this.jump = Instruction.INST_JUMP_TRUE;
	            this.cycles += getMemoryCycles(Pc, 12, 3);
	            break;
	        case InstructionTable.Iswpb:
	            this.cycles += getMemoryCycles(Pc, 10, 3);
	            break;
	        case InstructionTable.Iseto:
	            mop1.dest = MachineOperand.OP_DEST_KILLED;
	            this.cycles += getMemoryCycles(Pc, 10, 3);
	            break;
	        case InstructionTable.Iabs:
	            this.stsetBefore = Instruction.st_LAEO;
	            this.cycles += getMemoryCycles(Pc, 12, 2);
	            break;
	        default:
	            mop1.dest = MachineOperand.OP_DEST_FALSE;
	            break;
	        }
	
	    } else if (inst >= InstructionTable.Isra && inst <= InstructionTable.Isrc) {
	        Check.checkArg(mop1.type == MachineOperand.OP_REG);
	        Check.checkArg(mop2.type == MachineOperand.OP_IMMED || mop2.type == MachineOperand.OP_CNT);
	        mop1.dest = MachineOperand.OP_DEST_TRUE;
	        mop2.type = MachineOperand.OP_CNT;
	
	        // shift of zero comes from R0
	        if (mop2.val == 0) {
	            mop2.type = MachineOperand.OP_REG0_SHIFT_COUNT;
	            this.cycles += getMemoryCycles(Pc, 20, 3);
	        } else {
	            this.cycles += getMemoryCycles(Pc, 12, 4);
	        }
	
	        switch (inst) {
	        case InstructionTable.Isra:
	            this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
	            this.stsetAfter = Instruction.st_LAE_1;
	            break;
	        case InstructionTable.Isrl:
	            this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
	            this.stsetAfter = Instruction.st_LAE_1;
	            break;
	        case InstructionTable.Isla:
	            this.stsetBefore = Instruction.st_SHIFT_LEFT_CO;
	            this.stsetAfter = Instruction.st_LAE_1;
	            break;
	        case InstructionTable.Isrc:
	            this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
	            this.stsetAfter = Instruction.st_LAE_1;
	            break;
	        }
	
	    } else if (false) {
	        // TODO: extended instructions
	    } else if (inst >= InstructionTable.Ijmp && inst <= InstructionTable.Itb) {
	        if (inst < InstructionTable.Isbo) {
	        	Check.checkArg(mop2.type == MachineOperand.OP_NONE);
	        	if (mop1.type == MachineOperand.OP_IMMED) {
	                mop1.type = MachineOperand.OP_JUMP;
	                mop1.val = mop1.val - pc;
	        	} else if (mop1.type != MachineOperand.OP_JUMP){
	        		Check.checkArg(false);
	        	}
	            mop1.bIsCodeDest = true;
	            //this.stsetBefore = Instruction.st_ALL;
	            this.reads |= INST_RSRC_ST;
	            mop2.type = MachineOperand.OP_STATUS;
	            //((MachineOperand) this.op2).val = st.flatten();
	            this.jump = inst == InstructionTable.Ijmp ? Instruction.INST_JUMP_TRUE
	                    : Instruction.INST_JUMP_COND;
	            this.cycles += getMemoryCycles(Pc, 8, 1);
	        } else {
	            mop1.type = MachineOperand.OP_OFFS_R12;
	            this.cycles += getMemoryCycles(Pc, 12, 2);
	        }
	
	        switch (inst) {
	        case InstructionTable.Ijmp:
	            this.reads &= ~INST_RSRC_ST;
	            break;
	        case InstructionTable.Ijlt:
	            this.stReads = Status.ST_A + Status.ST_E;
	            break;
	        case InstructionTable.Ijle:
	            this.stReads = Status.ST_A + Status.ST_E;
	            break;
	        case InstructionTable.Ijeq:
	            this.stReads = Status.ST_E;
	            break;
	        case InstructionTable.Ijhe:
	            this.stReads = Status.ST_L + Status.ST_E;
	            break;
	        case InstructionTable.Ijgt:
	            this.stReads = Status.ST_L +Status.ST_E;
	            break;
	        case InstructionTable.Ijne:
	            this.stReads = Status.ST_E;
	            break;
	        case InstructionTable.Ijnc:
	            this.stReads = Status.ST_C;
	            break;
	        case InstructionTable.Ijoc:
	            this.stReads = Status.ST_C;
	            break;
	        case InstructionTable.Ijno:
	            this.stReads = Status.ST_O;
	            break;
	        case InstructionTable.Ijl:
	            this.stReads = Status.ST_L + Status.ST_E;
	            break;
	        case InstructionTable.Ijh:
	            this.stReads = Status.ST_L + Status.ST_E;
	            break;
	        case InstructionTable.Ijop:
	            this.inst = InstructionTable.Ijop;
	            this.stReads = Status.ST_P;
	            break;
	        case InstructionTable.Isbo:
	            this.writes |= INST_RSRC_IO;
	            break;
	        case InstructionTable.Isbz:
	            this.writes |= INST_RSRC_IO;
	            break;
	        case InstructionTable.Itb:
	            this.stsetAfter = Instruction.st_CMP;
	            this.reads |= INST_RSRC_IO;
	            break;
	        }
	
	    } else if (inst < InstructionTable.Iszc && inst != InstructionTable.Ildcr && inst != InstructionTable.Istcr) {
	        Check.checkArg(mop1.type != MachineOperand.OP_NONE
	                && mop1.type != MachineOperand.OP_IMMED);
	        Check.checkArg(mop2.type == MachineOperand.OP_REG);
	        mop1.dest = MachineOperand.OP_DEST_FALSE;
	        mop2.dest = MachineOperand.OP_DEST_TRUE;
	
	        switch (inst) {
	        case InstructionTable.Icoc:
	            this.stsetAfter = Instruction.st_CMP;
	            mop2.dest = MachineOperand.OP_DEST_FALSE;
	            this.cycles += getMemoryCycles(Pc, 14, 3);
	            break;
	        case InstructionTable.Iczc:
	            this.stsetAfter = Instruction.st_CMP;
	            mop2.dest = MachineOperand.OP_DEST_FALSE;
	            this.cycles += getMemoryCycles(Pc, 14, 3);
	            break;
	        case InstructionTable.Ixor:
	            this.stsetAfter = Instruction.st_LAE;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Ixop:
	            this.reads |= INST_RSRC_ST;
	            this.writes |= INST_RSRC_CTX;
	            //this.stsetBefore = Instruction.st_ALL;
	            this.stsetAfter = Instruction.st_XOP;
	            this.stReads = 0xffff;
	            this.cycles += getMemoryCycles(Pc, 36, 8);
	            break;
	        case InstructionTable.Impy:
	            //              ((MachineOperand) this.op2).type = MachineOperand.OP_MPY;
	            this.cycles += getMemoryCycles(Pc, 52, 5);
	            break;
	        case InstructionTable.Idiv:
	            this.stsetBefore = Instruction.st_DIV_O;
	            //              ((MachineOperand) this.op2).type = MachineOperand.OP_DIV;
	            this.cycles += getMemoryCycles(Pc, 124, 6);
	            break;
	        }
	
	    } else if (inst == InstructionTable.Ildcr || inst == InstructionTable.Istcr) {
	        Check.checkArg(mop1.type != MachineOperand.OP_NONE
	                && mop1.type != MachineOperand.OP_IMMED);
	        Check.checkArg(mop2.type == MachineOperand.OP_IMMED || mop2.type == MachineOperand.OP_CNT);
	        mop2.type = MachineOperand.OP_CNT;
	        if (mop2.val == 0) {
				mop2.val = 16;
			}
	        mop1.byteop = mop2.val <= 8;
	
	        if (inst == InstructionTable.Ildcr) {
	            this.stsetBefore = mop1.byteop ? Instruction.st_BYTE_LAEP_1
	                    : Instruction.st_LAE_1;
	            mop1.dest = MachineOperand.OP_DEST_FALSE;
	            this.cycles += getMemoryCycles(Pc, 20 + 2 * mop1.val, 3);
	            this.writes |= INST_RSRC_IO;
	        } else {
	            this.stsetAfter = mop1.byteop ? Instruction.st_BYTE_LAEP_1
	                    : Instruction.st_LAE_1;
	            mop1.dest = MachineOperand.OP_DEST_TRUE;
	            this.cycles += getMemoryCycles(Pc, mop1.val < 8 ? 42
	                    : mop1.val == 8 ? 44 : 58, 4);
	            this.reads |= INST_RSRC_IO;
	        }
	
	    } else {
	        Check.checkArg(mop1.type != MachineOperand.OP_NONE
	                && mop1.type != MachineOperand.OP_IMMED);
	        Check.checkArg(mop1.type != MachineOperand.OP_NONE
	                && mop1.type != MachineOperand.OP_IMMED);
	        mop2.dest = MachineOperand.OP_DEST_TRUE;
	        mop1.byteop = mop2.byteop = (inst - InstructionTable.Iszc & 1) != 0;
	
	        switch (inst) {
	        case InstructionTable.Iszc:
	            this.stsetAfter = Instruction.st_LAE;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Iszcb:
	            this.stsetAfter = Instruction.st_BYTE_LAEP;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Is:
	            this.stsetBefore = Instruction.st_SUB_LAECO;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Isb:
	            this.stsetBefore = Instruction.st_SUB_BYTE_LAECOP;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Ic:
	            this.stsetAfter = Instruction.st_CMP;
	            mop2.dest = MachineOperand.OP_DEST_FALSE;
	            this.cycles += getMemoryCycles(Pc, 14, 3);
	            break;
	        case InstructionTable.Icb:
	            this.stsetAfter = Instruction.st_BYTE_CMP;
	            mop2.dest = MachineOperand.OP_DEST_FALSE;
	            this.cycles += getMemoryCycles(Pc, 14, 3);
	            break;
	        case InstructionTable.Ia:
	            this.stsetBefore = Instruction.st_ADD_LAECO;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Iab:
	            this.stsetBefore = Instruction.st_ADD_BYTE_LAECOP;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Imov:
	            this.stsetAfter = Instruction.st_LAE;
	            mop2.dest = MachineOperand.OP_DEST_KILLED;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Imovb:
	            this.stsetAfter = Instruction.st_BYTE_LAEP;
	            mop2.dest = MachineOperand.OP_DEST_KILLED;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Isoc:
	            this.stsetAfter = Instruction.st_LAE;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        case InstructionTable.Isocb:
	            this.stsetAfter = Instruction.st_BYTE_LAEP;
	            this.cycles += getMemoryCycles(Pc, 14, 4);
	            break;
	        }
	    }
	
	    
	    // synthesize bits from other info
	    if (this.jump != INST_JUMP_FALSE) {
	        this.writes |= INST_RSRC_PC;
	        this.reads |= INST_RSRC_PC;
	    }
	    if (stsetBefore != st_NONE || stsetAfter != st_NONE) {
	        this.writes |= INST_RSRC_ST;
	    }
	    if (mop1.isRegisterReference() || mop2.isRegisterReference()) {
	        this.reads |= INST_RSRC_WP;
	    }
	
	    if (inst != InstructionTable.Idata && inst != InstructionTable.Ibyte) {
			 // Finish reading operand immediates
		    Pc += 2; // instruction itself
		    Pc = ((MachineOperand) op1).advancePc((short)Pc);
		    Pc = ((MachineOperand) op2).advancePc((short)Pc);
		    this.size = (Pc & 0xffff) - (this.pc & 0xffff);		
	    }
	    //super.completeInstruction(Pc);
	   
	}

	/** 
     * Update a previously decoded instruction, only rebuilding it
     * if its memory changed (self-modifying code).
     * @param pc2
     * @param wp2
     * @param status2
     */
    public Instruction update(short op, short thePc, MemoryDomain domain) {
        if (this.opcode != op || this.pc != thePc 
                //|| ((this.reads & INST_RSRC_WP) != 0 && this.wp != theWp) 
        //        || ((this.reads & INST_RSRC_ST) != 0 && this.status != st)
                ) 
        {
            // TODO: check for modified immediates, i.e. self modifying code
            // out-of-date instruction
            if (this.pc != thePc) {
				throw new AssertionError("wrong PC? " + v9t9.utils.Utils.toHex4(this.pc) + " != " + v9t9.utils.Utils.toHex4(thePc));
			}
            //System.out.println("need to regenerate instruction: >" + v9t9.Utils.toHex4(thePc) + " "+ this);
            return new Instruction(InstructionTable.decodeInstruction(op, thePc, domain));
        }
        //this.status = (Status)st.clone();
        return this;
    }

    public int compareTo(Instruction o) {
    	return pc - o.pc;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + inst;
		result = prime * result + ((op1 == null) ? 0 : op1.hashCode());
		result = prime * result + ((op2 == null) ? 0 : op2.hashCode());
		result = prime * result + pc;
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Instruction other = (Instruction) obj;
		if (inst != other.inst) {
			return false;
		}
		if (op1 == null) {
			if (other.op1 != null) {
				return false;
			}
		} else if (!op1.equals(other.op1)) {
			return false;
		}
		if (op2 == null) {
			if (other.op2 != null) {
				return false;
			}
		} else if (!op2.equals(other.op2)) {
			return false;
		}
		if (pc != other.pc) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}

}