/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.cpu;

import v9t9.MemoryDomain;

/**
 * @author ejs
 */
public class Instruction {
    // Opcodes
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
    
    public static final int INST_JUMP_FALSE = 0;

    public static final int INST_JUMP_TRUE = 1;

    public static final int INST_JUMP_COND = 2;

    // instruction reader/writer flags
    
    public static final int INST_RSRC_PC = 1;	// program counter
    public static final int INST_RSRC_WP = 2;	// workspace pointer
    public static final int INST_RSRC_ST = 4;	// status
    public static final int INST_RSRC_IO = 8;	// I/O
    public static final int INST_RSRC_EMU = 16; // emulator itself (builtin)
    public static final int INST_RSRC_CTX = 32;	// context switch (writer only)
    
    // fields

    public String name; // name of instruction

    public short pc; // PC of opcode

    public int size; // size of instruction in bytes

    public short wp; // current WP

    public Status status; // current status

    public short opcode; // opcode (full)

    public int inst; // instruction (Ixxx)

    public Operand op1, op2; // operands of instruction

    public short cycles; // execution cycles

    public int stsetBefore; // method status is set before instruction (ST_xxx)
    public int stsetAfter; // method status is set after instruction (ST_xxx)

    public int jump; // operand is a jump (INST_JUMP_COND = conditional)

    public int reads, writes;	// what resources (INST_RSRC_xxx) are read and written?
    
    Executor.Action action;
    
    public String toString() {
        String buffer = name;
        String opstring;
        opstring = op1.toString();
        if (opstring != null) {
            buffer += " " + opstring;
            opstring = op2.toString();
            if (opstring != null) {
                buffer += "," + opstring;
            }
        }
        return buffer.toUpperCase();
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
        return _mem_cycles[((pc) >> 13) & 7];
    }

    static public short getMemoryCycles(int pc, int base, int mem) {
        return (short) ((base) + (mem) * ramBankCycles(pc));
    }

    static public short getMemoryCycles(short pc) {
        return getMemoryCycles(pc, 0, 0);
    }

    /**
     * Decode instruction with opcode 'op' at 'addr' into 'ins'
     * 
     * @param domain
     *            provides read access to memory, to decode registers and
     *            instructions
     * @return new PC
     */
    public Instruction(int op, short pc, MemoryDomain domain) {
    
        /* deal with it unsigned */
        op &= 0xffff;
    
        this.cycles = getMemoryCycles(pc);
        this.opcode = (short) op;
        this.inst = Instruction.Idata;
        this.size = 0;
        this.name = null;
        this.op1 = new Operand();
        this.op2 = new Operand();
        this.op1.type = Operand.OP_NONE;
        this.op2.type = Operand.OP_NONE;
        this.stsetBefore = Instruction.st_NONE;
        this.stsetAfter = Instruction.st_NONE;
        this.jump = Instruction.INST_JUMP_FALSE;
        this.reads = 0;
        this.writes = 0;
        this.pc = pc;
        //this.wp = wp;
        //this.status = st;
    
        // Collect the instruction name
        // and operand structure.
    
        pc += 2; // point to operands
    
        // Initially, this.op?.val is incomplete, and is whatever
        // raw data from the opcode we can decode;
        // this.op?.ea is that of the instruction or immediate
        // if the operand needs it.
    
        // after decoding the instruction, we complete
        // the operand, making this.op?.val and this.op?.ea valid.
    
        if (op < 0x200) {
            this.cycles += getMemoryCycles(pc, 6, 1);
        } else if (op < 0x2a0) {
            this.op1.type = Operand.OP_REG;
            this.op1.val = op & 15;
            this.op1.dest = Operand.OP_DEST_TRUE;
            this.op2.type = Operand.OP_IMMED;
            switch ((op & 0x1e0) >> 5) {
            case 0:
                this.name = "LI";
                this.inst = Instruction.Ili;
                this.stsetAfter = Instruction.st_LAE_1;
                this.op1.dest = Operand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(pc, 12, 3);
                break;
            case 1:
                this.name = "AI";
                this.inst = Instruction.Iai;
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 2:
                this.name = "ANDI";
                this.inst = Instruction.Iandi;
                this.stsetAfter = Instruction.st_LAE_1;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 3:
                this.name = "ORI";
                this.inst = Instruction.Iori;
                this.stsetAfter = Instruction.st_LAE_1;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 4:
                this.name = "CI";
                this.inst = Instruction.Ici;
                this.stsetAfter = Instruction.st_CMP;
                this.op1.dest = Operand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(pc, 14, 3);
                break;
            }
    
        } else if (op < 0x2e0) {
            this.op1.type = Operand.OP_REG;
            this.op1.val = op & 15;
            this.op1.dest = Operand.OP_DEST_KILLED;
            switch ((op & 0x1e0) >> 5) {
            case 5:
                this.name = "STWP";
                this.inst = Instruction.Istwp;
                this.reads |= INST_RSRC_WP;
                this.cycles += getMemoryCycles(pc, 8, 2);
                break;
            case 6:
                this.name = "STST";
                this.inst = Instruction.Istst;
                this.reads |= INST_RSRC_ST;
                this.op2.type = Operand.OP_STATUS;
                //this.op2.val = st.flatten();
                this.cycles += getMemoryCycles(pc, 8, 2);
                break;
            }
    
        } else if (op < 0x320) {
            this.op1.type = Operand.OP_IMMED;
    
            switch ((op & 0x1e0) >> 5) {
            case 7:
                this.name = "LWPI";
                this.inst = Instruction.Ilwpi;
                this.writes |= INST_RSRC_WP;
                this.cycles += getMemoryCycles(pc, 10, 2);
                break;
            case 8:
                this.name = "LIMI";
                this.inst = Instruction.Ilimi;
                this.stsetAfter = Instruction.st_INT;
                this.cycles += getMemoryCycles(pc, 16, 2);
                break;
            }
    
        } else if (op < 0x400) {
            switch ((op & 0x1e0) >> 5) {
            case 10:
                this.name = "IDLE";
                this.inst = Instruction.Iidle;
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(pc, 12, 1);
                break;
            case 11:
                this.name = "RSET";
                this.inst = Instruction.Irset;
                this.stsetAfter = Instruction.st_INT;
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(pc, 12, 1);
                break;
            case 12:
                this.name = "RTWP";
                this.inst = Instruction.Irtwp;
                this.stsetAfter = Instruction.st_ALL;
                this.writes |= INST_RSRC_WP + INST_RSRC_ST + INST_RSRC_PC;
                this.op1.type = Operand.OP_STATUS;
                this.op1.dest = Operand.OP_DEST_KILLED;
                //this.op1.val = st.flatten();
                this.jump = Instruction.INST_JUMP_TRUE;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 13:
                this.name = "CKON";
                this.inst = Instruction.Ickon;
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(pc, 12, 1);
                break;
            case 14:
                this.name = "CKOF";
                this.inst = Instruction.Ickof;
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(pc, 12, 1);
                break;
            case 15:
                this.name = "LREX";
                this.inst = Instruction.Ilrex;
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(pc, 12, 1);
                break;
            }
    
        } else if (op < 0x800) {
            this.op1.type = (op & 0x30) >> 4;
            this.op1.val = op & 15;
            this.op1.dest = Operand.OP_DEST_TRUE;
    
            switch ((op & 0x3c0) >> 6) {
            case 0:
                this.name = "BLWP";
                this.inst = Instruction.Iblwp;
                this.stsetBefore = Instruction.st_ALL;
                this.reads |= INST_RSRC_ST;
                this.writes |= INST_RSRC_WP + INST_RSRC_PC + INST_RSRC_CTX;
                this.op1.dest = Operand.OP_DEST_FALSE;
                this.op1.bIsAddr = true;
                this.jump = Instruction.INST_JUMP_TRUE;
                this.cycles += getMemoryCycles(pc, 26, 6);
                break;
            case 1:
                this.name = "B";
                this.inst = Instruction.Ib;
                this.op1.dest = Operand.OP_DEST_FALSE;
                this.op1.bIsAddr = true;
                this.jump = Instruction.INST_JUMP_TRUE;
                this.cycles += getMemoryCycles(pc, 8, 2);
                break;
            case 2:
                this.name = "X";
                this.inst = Instruction.Ix;
                this.stsetBefore = Instruction.st_ALL;
                this.reads |= INST_RSRC_ST;
                this.op1.dest = Operand.OP_DEST_FALSE;
                this.op2.type = Operand.OP_INST;
                this.cycles += getMemoryCycles(pc, 8, 2);
                break;
            case 3:
                this.name = "CLR";
                this.inst = Instruction.Iclr;
                this.op1.dest = Operand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(pc, 10, 3);
                break;
            case 4:
                this.name = "NEG";
                this.inst = Instruction.Ineg;
                this.stsetAfter = Instruction.st_LAEO;
                this.cycles += getMemoryCycles(pc, 12, 3);
                break;
            case 5:
                this.name = "INV";
                this.inst = Instruction.Iinv;
                this.stsetAfter = Instruction.st_LAE_1;
                this.cycles += getMemoryCycles(pc, 10, 3);
                break;
            case 6:
                this.name = "INC";
                this.inst = Instruction.Iinc;
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                this.op2.type = Operand.OP_CNT;
                this.op2.val = 1;
                this.cycles += getMemoryCycles(pc, 10, 3);
                break;
            case 7:
                this.name = "INCT";
                this.inst = Instruction.Iinct;
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                this.op2.type = Operand.OP_CNT;
                this.op2.val = 2;
                this.cycles += getMemoryCycles(pc, 10, 3);
                break;
            case 8:
                this.name = "DEC";
                this.inst = Instruction.Idec;
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                this.op2.type = Operand.OP_CNT;
                this.op2.val = -1;
                this.cycles += getMemoryCycles(pc, 10, 3);
                break;
            case 9:
                this.name = "DECT";
                this.inst = Instruction.Idect;
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                this.op2.type = Operand.OP_CNT;
                this.op2.val = -2;
                this.cycles += getMemoryCycles(pc, 10, 3);
                break;
            case 10:
                this.name = "BL";
                this.inst = Instruction.Ibl;
                this.op1.dest = Operand.OP_DEST_FALSE;
                this.op1.bIsAddr = true;
                this.jump = Instruction.INST_JUMP_TRUE;
                this.cycles += getMemoryCycles(pc, 12, 3);
                break;
            case 11:
                this.name = "SWPB";
                this.inst = Instruction.Iswpb;
                this.cycles += getMemoryCycles(pc, 10, 3);
                break;
            case 12:
                this.name = "SETO";
                this.inst = Instruction.Iseto;
                this.op1.dest = Operand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(pc, 10, 3);
                break;
            case 13:
                this.name = "ABS";
                this.inst = Instruction.Iabs;
                this.stsetBefore = Instruction.st_LAEO;
                this.cycles += getMemoryCycles(pc, 12, 2);
                break;
            default:
                this.op1.dest = Operand.OP_DEST_FALSE;
            	break;
            }
    
        } else if (op < 0xc00) {
            this.op1.type = Operand.OP_REG;
            this.op1.val = op & 15;
            this.op1.dest = Operand.OP_DEST_TRUE;
            this.op2.type = Operand.OP_CNT;
            this.op2.val = (op & 0xf0) >> 4;
    
            // shift of zero comes from R0
            if (this.op2.val == 0) {
                this.op2.type = Operand.OP_REG0_SHIFT_COUNT;
                this.op2.val = 0;
                this.cycles += getMemoryCycles(pc, 20, 3);
            } else {
                this.cycles += getMemoryCycles(pc, 12, 4);
            }
    
            switch ((op & 0x700) >> 8) {
            case 0:
                this.name = "SRA";
                this.inst = Instruction.Isra;
                this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
                this.stsetAfter = Instruction.st_LAE_1;
                break;
            case 1:
                this.name = "SRL";
                this.inst = Instruction.Isrl;
                this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
                this.stsetAfter = Instruction.st_LAE_1;
                break;
            case 2:
                this.name = "SLA";
                this.inst = Instruction.Isla;
                this.stsetBefore = Instruction.st_SHIFT_LEFT_CO;
                this.stsetAfter = Instruction.st_LAE_1;
                break;
            case 3:
                this.name = "SRC";
                this.inst = Instruction.Isrc;
                this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
                this.stsetAfter = Instruction.st_LAE_1;
                break;
            }
    
        } else if (op < 0x1000) {
            switch ((op & 0x7e0) >> 5) {
            // TODO: extended instructions
            }
    
        } else if (op < 0x2000) {
            if (op < 0x1d00) {
                this.op1.type = Operand.OP_JUMP;
                this.op1.val = (((byte) (op & 0xff)) << 1) + 2;
                this.op1.bIsAddr = true;
                this.stsetBefore = Instruction.st_ALL;
                this.reads |= INST_RSRC_ST;
                this.op2.type = Operand.OP_STATUS;
                //this.op2.val = st.flatten();
                this.jump = op < 0x1100 ? Instruction.INST_JUMP_TRUE
                        : Instruction.INST_JUMP_COND;
                this.cycles += getMemoryCycles(pc, 8, 1);
            } else {
                this.op1.type = Operand.OP_OFFS_R12;
                this.op1.val = ((byte) (op & 0xff));
                this.cycles += getMemoryCycles(pc, 12, 2);
            }
    
            switch ((op & 0xf00) >> 8) {
            case 0:
                this.name = "JMP";
                this.inst = Instruction.Ijmp;
                this.reads &= ~INST_RSRC_ST;
                break;
            case 1:
                this.name = "JLT";
                this.inst = Instruction.Ijlt;
                break;
            case 2:
                this.name = "JLE";
                this.inst = Instruction.Ijle;
                break;
            case 3:
                this.name = "JEQ";
                this.inst = Instruction.Ijeq;
                break;
            case 4:
                this.name = "JHE";
                this.inst = Instruction.Ijhe;
                break;
            case 5:
                this.name = "JGT";
                this.inst = Instruction.Ijgt;
                break;
            case 6:
                this.name = "JNE";
                this.inst = Instruction.Ijne;
                break;
            case 7:
                this.name = "JNC";
                this.inst = Instruction.Ijnc;
                break;
            case 8:
                this.name = "JOC";
                this.inst = Instruction.Ijoc;
                break;
            case 9:
                this.name = "JNO";
                this.inst = Instruction.Ijno;
                break;
            case 10:
                this.name = "JL";
                this.inst = Instruction.Ijl;
                break;
            case 11:
                this.name = "JH";
                this.inst = Instruction.Ijh;
                break;
            case 12:
                this.name = "JOP";
                this.inst = Instruction.Ijop;
                break;
            case 13:
                this.name = "SBO";
                this.inst = Instruction.Isbo;
                this.writes |= INST_RSRC_IO;
                break;
            case 14:
                this.name = "SBZ";
                this.inst = Instruction.Isbz;
                this.writes |= INST_RSRC_IO;
                break;
            case 15:
                this.name = "TB";
                this.inst = Instruction.Itb;
                this.stsetAfter = Instruction.st_CMP;
                this.reads |= INST_RSRC_IO;
                break;
            }
    
        } else if (op < 0x4000 && !(op >= 0x3000 && op < 0x3800)) {
            this.op1.type = (op & 0x30) >> 4;
            this.op1.val = (op & 15);
            this.op1.dest = Operand.OP_DEST_FALSE;
            this.op2.type = Operand.OP_REG;
            this.op2.val = (op & 0x3c0) >> 6;
            this.op2.dest = Operand.OP_DEST_TRUE;
    
            switch ((op & 0x1c00) >> 10) {
            case 0:
                this.name = "COC";
                this.inst = Instruction.Icoc;
                this.stsetAfter = Instruction.st_CMP;
                this.op2.dest = Operand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(pc, 14, 3);
                break;
            case 1:
                this.name = "CZC";
                this.inst = Instruction.Iczc;
                this.stsetAfter = Instruction.st_CMP;
                this.op2.dest = Operand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(pc, 14, 3);
                break;
            case 2:
                this.name = "XOR";
                this.inst = Instruction.Ixor;
                this.stsetAfter = Instruction.st_LAE;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 3:
                this.name = "XOP";
                this.inst = Instruction.Ixop;
                this.reads |= INST_RSRC_ST;
                this.writes |= INST_RSRC_CTX;
                this.stsetBefore = Instruction.st_ALL;
                this.stsetAfter = Instruction.st_XOP;
                this.cycles += getMemoryCycles(pc, 36, 8);
                break;
            case 6:
                this.name = "MPY";
                this.inst = Instruction.Impy;
                //    			this.op2.type = Operand.OP_MPY;
                this.cycles += getMemoryCycles(pc, 52, 5);
                break;
            case 7:
                this.name = "DIV";
                this.inst = Instruction.Idiv;
                this.stsetBefore = Instruction.st_DIV_O;
                //    			this.op2.type = Operand.OP_DIV;
                this.cycles += getMemoryCycles(pc, 124, 6);
                break;
            }
    
        } else if (op >= 0x3000 && op < 0x3800) {
            this.op1.type = (op & 0x30) >> 4;
            this.op1.val = (op & 15);
            this.op2.type = Operand.OP_CNT;
            this.op2.val = (op & 0x3c0) >> 6;
            if (this.op2.val == 0)
                this.op2.val = 16;
            this.op1.byteop = (this.op2.val <= 8);
    
            if (op < 0x3400) {
                this.name = "LDCR";
                this.inst = Instruction.Ildcr;
                this.stsetBefore = this.op1.byteop ? Instruction.st_BYTE_LAEP_1
                        : Instruction.st_LAE_1;
                this.op1.dest = Operand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(pc, 20 + 2 * this.op1.val, 3);
                this.writes |= INST_RSRC_IO;
            } else {
                this.name = "STCR";
                this.inst = Instruction.Istcr;
                this.stsetAfter = this.op1.byteop ? Instruction.st_BYTE_LAEP_1
                        : Instruction.st_LAE_1;
                this.op1.dest = Operand.OP_DEST_TRUE;
                this.cycles += getMemoryCycles(pc, this.op1.val < 8 ? 42
                        : this.op1.val == 8 ? 44 : 58, 4);
                this.reads |= INST_RSRC_IO;
            }
    
        } else {
            this.op1.type = (op & 0x30) >> 4;
            this.op1.val = (op & 15);
            this.op2.type = (op & 0x0c00) >> 10;
            this.op2.val = (op & 0x3c0) >> 6;
            this.op2.dest = Operand.OP_DEST_TRUE;
            this.op1.byteop = this.op2.byteop = ((op & 0x1000) != 0);
    
            switch ((op & 0xf000) >> 12) {
            case 4:
                this.name = "SZC";
                this.inst = Instruction.Iszc;
                this.stsetAfter = Instruction.st_LAE;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 5:
                this.name = "SZCB";
                this.inst = Instruction.Iszcb;
                this.stsetAfter = Instruction.st_BYTE_LAEP;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 6:
                this.name = "S";
                this.inst = Instruction.Is;
                this.stsetBefore = Instruction.st_SUB_LAECO;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 7:
                this.name = "SB";
                this.inst = Instruction.Isb;
                this.stsetBefore = Instruction.st_SUB_BYTE_LAECOP;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 8:
                this.name = "C";
                this.inst = Instruction.Ic;
                this.stsetAfter = Instruction.st_CMP;
                this.op2.dest = Operand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(pc, 14, 3);
                break;
            case 9:
                this.name = "CB";
                this.inst = Instruction.Icb;
                this.stsetAfter = Instruction.st_BYTE_CMP;
                this.op2.dest = Operand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(pc, 14, 3);
                break;
            case 10:
                this.name = "A";
                this.inst = Instruction.Ia;
                this.stsetBefore = Instruction.st_ADD_LAECO;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 11:
                this.name = "AB";
                this.inst = Instruction.Iab;
                this.stsetBefore = Instruction.st_ADD_BYTE_LAECOP;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 12:
                this.name = "MOV";
                this.inst = Instruction.Imov;
                this.stsetAfter = Instruction.st_LAE;
                this.op2.dest = Operand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 13:
                this.name = "MOVB";
                this.inst = Instruction.Imovb;
                this.stsetAfter = Instruction.st_BYTE_LAEP;
                this.op2.dest = Operand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 14:
                this.name = "SOC";
                this.inst = Instruction.Isoc;
                this.stsetAfter = Instruction.st_LAE;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            case 15:
                this.name = "SOCB";
                this.inst = Instruction.Isocb;
                this.stsetAfter = Instruction.st_BYTE_LAEP;
                this.cycles += getMemoryCycles(pc, 14, 4);
                break;
            }
        }
    
        if (this.name == null) // data
        {
            this.op1.type = Operand.OP_IMMED;
            pc -= 2; // instruction itself is value
            this.name = "DATA";
        }
        
        // synthesize bits from other info
        if (this.jump != INST_JUMP_FALSE) {
            this.writes |= INST_RSRC_PC;
            this.reads |= INST_RSRC_PC;
        }
        if (stsetBefore != st_NONE || stsetAfter != st_NONE) {
            this.writes |= INST_RSRC_ST;
        }
        if (op1.isRegister() || op2.isRegister()) {
            this.reads |= INST_RSRC_WP;
        }
    
       	// Finish reading operand immediates
        pc = this.op1.fetchOperandImmediates(domain, pc);
        pc = this.op2.fetchOperandImmediates(domain, pc);
        this.size = pc - this.pc;
    }

    /**	Update a previously decoded instruction.
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
            if (this.pc != thePc)
                throw new AssertionError("wrong PC? " + v9t9.Globals.toHex4(this.pc) + " != " + v9t9.Globals.toHex4(thePc));
            System.out.println("need to regenerate instruction: >" + v9t9.Globals.toHex4(thePc) + " "+ this);
            return new Instruction(op, thePc, domain);
        }
        //this.status = (Status)st.clone();
        return this;
    }

}