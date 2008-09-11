/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.engine.cpu;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.engine.memory.MemoryDomain;
import v9t9.utils.Check;

/**
 * @author ejs
 */
public class Instruction implements Comparable<Instruction> {
    static Map<String, Integer> nameToInst = new HashMap<String, Integer>();
    static Map<Integer, String> instToName = new HashMap<Integer, String>();

    // Opcodes: ORDERING MATTERS!
    public static final int Idata = 0; static { registerInstruction(Idata, "data"); }
    
    public static final int Ili = 1; static { registerInstruction(Ili, "li"); }
    
    public static final int Iai = 2; static { registerInstruction(Iai, "ai"); }
    
    public static final int Iandi = 3; static { registerInstruction(Iandi, "andi"); }
    
    public static final int Iori = 4; static { registerInstruction(Iori, "ori"); }
    
    public static final int Ici = 5; static { registerInstruction(Ici, "ci"); }

    public static final int Istwp = 6; static { registerInstruction(Istwp, "stwp"); }

    public static final int Istst = 7; static { registerInstruction(Istst, "stst"); }

    public static final int Ilwpi = 8; static { registerInstruction(Ilwpi, "lwpi"); }

    public static final int Ilimi = 9; static { registerInstruction(Ilimi, "limi"); }

    public static final int Iidle = 10; static { registerInstruction(Iidle, "idle"); }

    public static final int Irset = 11; static { registerInstruction(Irset, "rset"); }

    public static final int Irtwp = 12; static { registerInstruction(Irtwp, "rtwp"); }

    public static final int Ickon = 13; static { registerInstruction(Ickon, "ckon"); }

    public static final int Ickof = 14; static { registerInstruction(Ickof, "ckof"); }

    public static final int Ilrex = 15; static { registerInstruction(Ilrex, "lrex"); }

    public static final int Iblwp = 16; static { registerInstruction(Iblwp, "blwp"); }

    public static final int Ib = 17; static { registerInstruction(Ib, "b"); }

    public static final int Ix = 18; static { registerInstruction(Ix, "x"); }

    public static final int Iclr = 19; static { registerInstruction(Iclr, "clr"); }

    public static final int Ineg = 20; static { registerInstruction(Ineg, "neg"); }

    public static final int Iinv = 21; static { registerInstruction(Iinv, "inv"); }

    public static final int Iinc = 22; static { registerInstruction(Iinc, "inc"); }

    public static final int Iinct = 23; static { registerInstruction(Iinct, "inct"); }

    public static final int Idec = 24; static { registerInstruction(Idec, "dec"); }

    public static final int Idect = 25; static { registerInstruction(Idect, "dect"); }

    public static final int Ibl = 26; static { registerInstruction(Ibl, "bl"); }

    public static final int Iswpb = 27; static { registerInstruction(Iswpb, "swpb"); }

    public static final int Iseto = 28; static { registerInstruction(Iseto, "seto"); }

    public static final int Iabs = 29; static { registerInstruction(Iabs, "abs"); }

    public static final int Isra = 30; static { registerInstruction(Isra, "sra"); }

    public static final int Isrl = 31; static { registerInstruction(Isrl, "srl"); }

    public static final int Isla = 32; static { registerInstruction(Isla, "sla"); }

    public static final int Isrc = 33; static { registerInstruction(Isrc, "src"); }

    public static final int Ijmp = 34; static { registerInstruction(Ijmp, "jmp"); }

    public static final int Ijlt = 35; static { registerInstruction(Ijlt, "jlt"); }

    public static final int Ijle = 36; static { registerInstruction(Ijle, "jle"); }

    public static final int Ijeq = 37; static { registerInstruction(Ijeq, "jeq"); }

    public static final int Ijhe = 38; static { registerInstruction(Ijhe, "jhe"); }

    public static final int Ijgt = 39; static { registerInstruction(Ijgt, "jgt"); }

    public static final int Ijne = 40; static { registerInstruction(Ijne, "jne"); }

    public static final int Ijnc = 41; static { registerInstruction(Ijnc, "jnc"); }

    public static final int Ijoc = 42; static { registerInstruction(Ijoc, "joc"); }

    public static final int Ijno = 43; static { registerInstruction(Ijno, "jno"); }

    public static final int Ijl = 44; static { registerInstruction(Ijl, "jl"); }

    public static final int Ijh = 45; static { registerInstruction(Ijh, "jh"); }

    public static final int Ijop = 46; static { registerInstruction(Ijop, "jop"); }

    public static final int Isbo = 47; static { registerInstruction(Isbo, "sbo"); }

    public static final int Isbz = 48; static { registerInstruction(Isbz, "sbz"); }

    public static final int Itb = 49; static { registerInstruction(Itb, "tb"); }

    public static final int Icoc = 50; static { registerInstruction(Icoc, "coc"); }

    public static final int Iczc = 51; static { registerInstruction(Iczc, "czc"); }

    public static final int Ixor = 52; static { registerInstruction(Ixor, "xor"); }

    public static final int Ixop = 53; static { registerInstruction(Ixop, "xop"); }

    public static final int Impy = 54; static { registerInstruction(Impy, "mpy"); }

    public static final int Idiv = 55; static { registerInstruction(Idiv, "div"); }

    public static final int Ildcr = 56; static { registerInstruction(Ildcr, "ldcr"); }

    public static final int Istcr = 57; static { registerInstruction(Istcr, "stcr"); }

    public static final int Iszc = 58; static { registerInstruction(Iszc, "szc"); }

    public static final int Iszcb = 59; static { registerInstruction(Iszcb, "szcb"); }

    public static final int Is = 60; static { registerInstruction(Is, "s"); }

    public static final int Isb = 61; static { registerInstruction(Isb, "sb"); }

    public static final int Ic = 62; static { registerInstruction(Ic, "c"); }

    public static final int Icb = 63; static { registerInstruction(Icb, "cb"); }

    public static final int Ia = 64; static { registerInstruction(Ia, "a"); }

    public static final int Iab = 65; static { registerInstruction(Iab, "ab"); }

    public static final int Imov = 66; static { registerInstruction(Imov, "mov"); }

    public static final int Imovb = 67; static { registerInstruction(Imovb, "movb"); }

    public static final int Isoc = 68; static { registerInstruction(Isoc, "soc"); }

    public static final int Isocb = 69; static { registerInstruction(Isocb, "socb"); }

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

    public String name; // name of instruction

    public int pc; // PC of opcode

    public int size; // size of instruction in bytes

    //public short wp; // current WP

    public Status status; // current status

    public short opcode; // opcode (full)

    public int inst; // instruction (Ixxx)

    public Operand op1, op2; // operands of instruction

    public short cycles; // execution cycles

    public int stsetBefore; // method status is set after operands parsed, before execution (st_xxx)
    public int stsetAfter; // method status is set after execution (st_xxx)
    public int stReads;     // bits read by instruction (Status.ST_xxx mask)
    /** operand is a jump (INST_JUMP_COND = conditional) */
    public int jump; 

    public int reads, writes;	// what resources (INST_RSRC_xxx) are read and written?
    
    InstructionAction action;

    private static void registerInstruction(int inst, String str) {
        Integer i = new Integer(inst);
        nameToInst.put(str.toUpperCase(), i);
        instToName.put(i, str.toUpperCase());
    }

    /**
     * Get the instruction code for the given instruction
     * @param str
     * @return
     */
    public static int lookupInst(String str) {
    	Integer inst = nameToInst.get(str.toUpperCase());
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
    
    /**
     * Decode instruction with opcode 'op' at 'addr' into 'ins'.
     * 
     * @param domain
     *            provides read access to memory, to decode registers and
     *            instructions
     * @return new PC
     */
    public Instruction(int op, int pc, MemoryDomain domain) {
        this(pc);
        
        /* deal with it unsigned */
        op &= 0xffff;
    
        this.opcode = (short) op;
        this.inst = Instruction.Idata;
        this.size = 0;
        this.name = null;
        MachineOperand mop1 = new MachineOperand(MachineOperand.OP_NONE);
        MachineOperand mop2 = new MachineOperand(MachineOperand.OP_NONE);
        this.op1 = mop1;
        this.op2 = mop2;
    
        // Collect the instruction name
        // and operand structure.
    
        // Initially, this.op?.val is incomplete, and is whatever
        // raw data from the opcode we can decode;
        // this.op?.ea is that of the instruction or immediate
        // if the operand needs it.
    
        // after decoding the instruction, we complete
        // the operand, making this.op?.val and this.op?.ea valid.
    
        if (op < 0x200) {
            
        } else if (op < 0x2a0) {
            mop1.type = MachineOperand.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperand.OP_IMMED;
            switch ((op & 0x1e0) >> 5) {
            case 0:
                this.name = "LI";
                this.inst = Instruction.Ili;
                break;
            case 1:
                this.name = "AI";
                this.inst = Instruction.Iai;
                break;
            case 2:
                this.name = "ANDI";
                this.inst = Instruction.Iandi;
                break;
            case 3:
                this.name = "ORI";
                this.inst = Instruction.Iori;
                break;
            case 4:
                this.name = "CI";
                this.inst = Instruction.Ici;
                break;
            }
    
        } else if (op < 0x2e0) {
            mop1.type = MachineOperand.OP_REG;
            mop1.val = op & 15;
            mop1.dest = MachineOperand.OP_DEST_KILLED;
            switch ((op & 0x1e0) >> 5) {
            case 5:
                this.name = "STWP";
                this.inst = Instruction.Istwp;
                break;
            case 6:
                this.name = "STST";
                this.inst = Instruction.Istst;
                break;
            }
    
        } else if (op < 0x320) {
            mop1.type = MachineOperand.OP_IMMED;
    
            switch ((op & 0x1e0) >> 5) {
            case 7:
                this.name = "LWPI";
                this.inst = Instruction.Ilwpi;
                break;
            case 8:
                this.name = "LIMI";
                this.inst = Instruction.Ilimi;
                break;
            }
    
        } else if (op < 0x400) {
            switch ((op & 0x1e0) >> 5) {
            case 10:
                this.name = "IDLE";
                this.inst = Instruction.Iidle;
                break;
            case 11:
                this.name = "RSET";
                this.inst = Instruction.Irset;
                break;
            case 12:
                this.name = "RTWP";
                this.inst = Instruction.Irtwp;
                break;
            case 13:
                this.name = "CKON";
                this.inst = Instruction.Ickon;
                break;
            case 14:
                this.name = "CKOF";
                this.inst = Instruction.Ickof;
                break;
            case 15:
                this.name = "LREX";
                this.inst = Instruction.Ilrex;
                break;
            }
    
        } else if (op < 0x800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
    
            switch ((op & 0x3c0) >> 6) {
            case 0:
                this.name = "BLWP";
                this.inst = Instruction.Iblwp;
                break;
            case 1:
                this.name = "B";
                this.inst = Instruction.Ib;
                break;
            case 2:
                this.name = "X";
                this.inst = Instruction.Ix;
                break;
            case 3:
                this.name = "CLR";
                this.inst = Instruction.Iclr;
                break;
            case 4:
                this.name = "NEG";
                this.inst = Instruction.Ineg;
                break;
            case 5:
                this.name = "INV";
                this.inst = Instruction.Iinv;
                break;
            case 6:
                this.name = "INC";
                this.inst = Instruction.Iinc;
                break;
            case 7:
                this.name = "INCT";
                this.inst = Instruction.Iinct;
                break;
            case 8:
                this.name = "DEC";
                this.inst = Instruction.Idec;
                break;
            case 9:
                this.name = "DECT";
                this.inst = Instruction.Idect;
                break;
            case 10:
                this.name = "BL";
                this.inst = Instruction.Ibl;
                break;
            case 11:
                this.name = "SWPB";
                this.inst = Instruction.Iswpb;
                break;
            case 12:
                this.name = "SETO";
                this.inst = Instruction.Iseto;
                break;
            case 13:
                this.name = "ABS";
                this.inst = Instruction.Iabs;
                break;
            }
    
        } else if (op < 0xc00) {
            mop1.type = MachineOperand.OP_REG;
            mop1.val = op & 15;
            mop2.type = MachineOperand.OP_IMMED;
            mop2.val = (op & 0xf0) >> 4;
    
            switch ((op & 0x700) >> 8) {
            case 0:
                this.name = "SRA";
                this.inst = Instruction.Isra;
                break;
            case 1:
                this.name = "SRL";
                this.inst = Instruction.Isrl;
                break;
            case 2:
                this.name = "SLA";
                this.inst = Instruction.Isla;
                break;
            case 3:
                this.name = "SRC";
                this.inst = Instruction.Isrc;
                break;
            }
    
        } else if (op < 0x1000) {
            switch ((op & 0x7e0) >> 5) {
            // TODO: extended instructions
            }
    
        } else if (op < 0x2000) {
            mop1.type = MachineOperand.OP_IMMED;
            mop1.val = (byte) (op & 0xff);
            if (op < 0x1D00) {
                mop1.val = (mop1.val << 1) + pc + 2;
            }
    
            switch ((op & 0xf00) >> 8) {
            case 0:
                this.name = "JMP";
                this.inst = Instruction.Ijmp;
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
                break;
            case 14:
                this.name = "SBZ";
                this.inst = Instruction.Isbz;
                break;
            case 15:
                this.name = "TB";
                this.inst = Instruction.Itb;
                break;
            }
    
        } else if (op < 0x4000 && !(op >= 0x3000 && op < 0x3800)) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = MachineOperand.OP_REG;
            mop2.val = (op & 0x3c0) >> 6;
    
            switch ((op & 0x1c00) >> 10) {
            case 0:
                this.name = "COC";
                this.inst = Instruction.Icoc;
                break;
            case 1:
                this.name = "CZC";
                this.inst = Instruction.Iczc;
                break;
            case 2:
                this.name = "XOR";
                this.inst = Instruction.Ixor;
                break;
            case 3:
                this.name = "XOP";
                this.inst = Instruction.Ixop;
                break;
            case 6:
                this.name = "MPY";
                this.inst = Instruction.Impy;
                break;
            case 7:
                this.name = "DIV";
                this.inst = Instruction.Idiv;
                break;
            }
    
        } else if (op >= 0x3000 && op < 0x3800) {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = MachineOperand.OP_IMMED;
            mop2.val = (op & 0x3c0) >> 6;
    
            if (op < 0x3400) {
                this.name = "LDCR";
                this.inst = Instruction.Ildcr;
            } else {
                this.name = "STCR";
                this.inst = Instruction.Istcr;
            }
    
        } else {
            mop1.type = (op & 0x30) >> 4;
            mop1.val = op & 15;
            mop2.type = (op & 0x0c00) >> 10;
            mop2.val = (op & 0x3c0) >> 6;
    
            switch ((op & 0xf000) >> 12) {
            case 4:
                this.name = "SZC";
                this.inst = Instruction.Iszc;
                break;
            case 5:
                this.name = "SZCB";
                this.inst = Instruction.Iszcb;
                break;
            case 6:
                this.name = "S";
                this.inst = Instruction.Is;
                break;
            case 7:
                this.name = "SB";
                this.inst = Instruction.Isb;
                break;
            case 8:
                this.name = "C";
                this.inst = Instruction.Ic;
                break;
            case 9:
                this.name = "CB";
                this.inst = Instruction.Icb;
                break;
            case 10:
                this.name = "A";
                this.inst = Instruction.Ia;
                break;
            case 11:
                this.name = "AB";
                this.inst = Instruction.Iab;
                break;
            case 12:
                this.name = "MOV";
                this.inst = Instruction.Imov;
                break;
            case 13:
                this.name = "MOVB";
                this.inst = Instruction.Imovb;
                break;
            case 14:
                this.name = "SOC";
                this.inst = Instruction.Isoc;
                break;
            case 15:
                this.name = "SOCB";
                this.inst = Instruction.Isocb;
                break;
            }
        }
    
        if (this.name == null) // data
        {
            mop1.type = MachineOperand.OP_IMMED;
            this.name = "DATA";
            this.size = 2;
        } else {
            completeInstruction(pc);
            // Finish reading operand immediates
            pc += 2;
            pc = mop1.fetchOperandImmediates(domain, (short)pc);
            pc = mop2.fetchOperandImmediates(domain, (short)pc);
        }
    
    }

    public static final String OP_MATCHER = "([^,]+)"; 
    public static final Pattern INSTR_PATTERN = Pattern.compile(
            //     1
            "\\s*([a-zA-Z0-9]+)"+
                            // 2
                "(?:\\s+" + OP_MATCHER + "\\s*" +
                                  // 3
                    "(?:,\\s*" + OP_MATCHER +")?" +
                ")?" +
            ".*"
            );
    
    /**
     * Create an instruction from a string.
     */
    public Instruction(int pc, String string) {
        this(pc);
        Matcher matcher = INSTR_PATTERN.matcher(string);
        Check.checkArg(matcher.matches());
        this.name = matcher.group(1).toUpperCase();
        MachineOperand mop1, mop2;
        if (this.name.equals("RT")) {
            this.name = "B";
            this.inst = Ib;
            mop1 = new MachineOperand(MachineOperand.OP_IND);
            mop1.val = 11;
            mop2 = new MachineOperand(MachineOperand.OP_NONE);
        } else if (this.name.equals("NOP")) {
            this.name = "JMP";
            this.inst = Ijmp;
            mop1 = new MachineOperand(MachineOperand.OP_IMMED);
            mop1.val = pc + 2;
            mop2 = new MachineOperand(MachineOperand.OP_NONE);
        } else {
            this.inst = lookupInst(this.name);
            if (this.inst < 0)
            	throw new IllegalArgumentException("Unknown instruction: " + name);
            mop1 = new MachineOperand(matcher.group(2));
            mop2 = new MachineOperand(matcher.group(3));
        }
        this.op1 = mop1;
        this.op2 = mop2;
        completeInstruction(pc);
        //calculateOpcode();
    }
    

    public Instruction(Instruction inst) {
    	this.name = inst.name;
    	this.pc = inst.pc;
    	this.size = inst.size;
    	this.status = inst.status;
    	this.opcode = inst.opcode;
    	this.inst = inst.inst;
    	this.op1 = inst.op1;
    	this.op2 = inst.op2;
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
     * Finish filling in an instruction
     *
     */
    private void completeInstruction(int Pc) {
        this.cycles = getMemoryCycles(Pc);
        this.size = 0;
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
    
        if (inst == Idata) {
            Check.checkState(mop1.type == MachineOperand.OP_IMMED);
            Check.checkState(mop2.type == MachineOperand.OP_NONE);
            Pc -= 2;
            this.cycles += getMemoryCycles(Pc, 6, 1);
        } else if (inst >= Ili && inst <= Ici) {
            Check.checkState(mop1.type == MachineOperand.OP_REG);
            mop2.convertToImmedate();
            mop1.dest = MachineOperand.OP_DEST_TRUE;
            switch (inst) {
            case Ili:
                this.stsetAfter = Instruction.st_LAE_1;
                mop1.dest = MachineOperand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(Pc, 12, 3);
                break;
            case Iai:
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Iandi:
                this.stsetAfter = Instruction.st_LAE_1;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Iori:
                this.stsetAfter = Instruction.st_LAE_1;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Ici:
                this.stsetAfter = Instruction.st_CMP;
                mop1.dest = MachineOperand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(Pc, 14, 3);
                break;
            }
    
        } else if (inst == Istwp) {
            Check.checkState(mop1.type == MachineOperand.OP_REG);
            Check.checkState(mop2.type == MachineOperand.OP_NONE);
            mop1.dest = MachineOperand.OP_DEST_KILLED;
            this.reads |= INST_RSRC_WP;
            this.cycles += getMemoryCycles(Pc, 8, 2);
        } else if (inst == Istst) {
            Check.checkState(mop1.type == MachineOperand.OP_REG);
            this.reads |= INST_RSRC_ST;
            this.stReads = 0xffff;
            
            Check.checkState(mop2.type == MachineOperand.OP_NONE);
            mop2.type = MachineOperand.OP_STATUS;
            this.cycles += getMemoryCycles(Pc, 8, 2);
        } else if (inst == Ilwpi) {
            Check.checkState(mop1.type == MachineOperand.OP_IMMED);
            Check.checkState(mop2.type == MachineOperand.OP_NONE);
            this.writes |= INST_RSRC_WP;
            this.cycles += getMemoryCycles(Pc, 10, 2);
        } else if (inst == Ilimi) {
            Check.checkState(mop1.type == MachineOperand.OP_IMMED);
            Check.checkState(mop2.type == MachineOperand.OP_NONE);
            this.stsetAfter = Instruction.st_INT;
            this.cycles += getMemoryCycles(Pc, 16, 2);
        } else if (inst >= Iidle && inst <= Ilrex) {
            Check.checkState(mop1.type == MachineOperand.OP_NONE);
            Check.checkState(mop2.type == MachineOperand.OP_NONE);
            switch (inst) {
            case Iidle:
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(Pc, 12, 1);
                break;
            case Irset:
                this.stsetAfter = Instruction.st_INT;
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(Pc, 12, 1);
                break;
            case Irtwp:
                this.stsetAfter = Instruction.st_ALL;
                this.writes |= INST_RSRC_WP + INST_RSRC_ST + INST_RSRC_PC;
                mop1.type = MachineOperand.OP_STATUS;
                mop1.dest = MachineOperand.OP_DEST_KILLED;
                //((MachineOperand) this.op1).val = st.flatten();
                this.jump = Instruction.INST_JUMP_TRUE;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Ickon:
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(Pc, 12, 1);
                break;
            case Ickof:
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(Pc, 12, 1);
                break;
            case Ilrex:
                this.writes |= INST_RSRC_IO;
                this.cycles += getMemoryCycles(Pc, 12, 1);
                break;
            }
    
        } else if (inst >= Iblwp && inst <= Iabs) {
            Check.checkState(mop1.type != MachineOperand.OP_NONE
                    && mop1.type != MachineOperand.OP_IMMED);
            Check.checkState(mop2.type == MachineOperand.OP_NONE);
            mop1.dest = MachineOperand.OP_DEST_TRUE;
    
            switch (inst) {
            case Iblwp:
                //this.stsetBefore = Instruction.st_ALL;
                this.stReads = 0xffff;
                this.reads |= INST_RSRC_ST;
                this.writes |= INST_RSRC_WP + INST_RSRC_PC + INST_RSRC_CTX;
                mop1.dest = MachineOperand.OP_DEST_FALSE;
                mop1.bIsCodeDest = true;
                this.jump = Instruction.INST_JUMP_TRUE;
                this.cycles += getMemoryCycles(Pc, 26, 6);
                break;
            case Ib:
                mop1.dest = MachineOperand.OP_DEST_FALSE;
                mop1.bIsCodeDest = true;
                this.jump = Instruction.INST_JUMP_TRUE;
                this.cycles += getMemoryCycles(Pc, 8, 2);
                break;
            case Ix:
                //this.stsetBefore = Instruction.st_ALL;
                this.stReads = 0xffff;
                this.reads |= INST_RSRC_ST;
                mop1.dest = MachineOperand.OP_DEST_FALSE;
                mop2.type = MachineOperand.OP_INST;
                this.cycles += getMemoryCycles(Pc, 8, 2);
                break;
            case Iclr:
                mop1.dest = MachineOperand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(Pc, 10, 3);
                break;
            case Ineg:
                this.stsetAfter = Instruction.st_LAEO;
                this.cycles += getMemoryCycles(Pc, 12, 3);
                break;
            case Iinv:
                this.stsetAfter = Instruction.st_LAE_1;
                this.cycles += getMemoryCycles(Pc, 10, 3);
                break;
            case Iinc:
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                mop2.type = MachineOperand.OP_CNT;
                mop2.val = 1;
                this.cycles += getMemoryCycles(Pc, 10, 3);
                break;
            case Iinct:
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                mop2.type = MachineOperand.OP_CNT;
                mop2.val = 2;
                this.cycles += getMemoryCycles(Pc, 10, 3);
                break;
            case Idec:
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                mop2.type = MachineOperand.OP_CNT;
                mop2.val = -1;
                this.cycles += getMemoryCycles(Pc, 10, 3);
                break;
            case Idect:
                this.stsetBefore = Instruction.st_ADD_LAECO_REV;
                mop2.type = MachineOperand.OP_CNT;
                mop2.val = -2;
                this.cycles += getMemoryCycles(Pc, 10, 3);
                break;
            case Ibl:
                mop1.dest = MachineOperand.OP_DEST_FALSE;
                mop1.bIsCodeDest = true;
                this.jump = Instruction.INST_JUMP_TRUE;
                this.cycles += getMemoryCycles(Pc, 12, 3);
                break;
            case Iswpb:
                this.cycles += getMemoryCycles(Pc, 10, 3);
                break;
            case Iseto:
                mop1.dest = MachineOperand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(Pc, 10, 3);
                break;
            case Iabs:
                this.stsetBefore = Instruction.st_LAEO;
                this.cycles += getMemoryCycles(Pc, 12, 2);
                break;
            default:
                mop1.dest = MachineOperand.OP_DEST_FALSE;
                break;
            }
    
        } else if (inst >= Isra && inst <= Isrc) {
            Check.checkState(mop1.type == MachineOperand.OP_REG);
            Check.checkState(mop2.type == MachineOperand.OP_IMMED);
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
            case Isra:
                this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
                this.stsetAfter = Instruction.st_LAE_1;
                break;
            case Isrl:
                this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
                this.stsetAfter = Instruction.st_LAE_1;
                break;
            case Isla:
                this.stsetBefore = Instruction.st_SHIFT_LEFT_CO;
                this.stsetAfter = Instruction.st_LAE_1;
                break;
            case Isrc:
                this.stsetBefore = Instruction.st_SHIFT_RIGHT_C;
                this.stsetAfter = Instruction.st_LAE_1;
                break;
            }
    
        } else if (false) {
            // TODO: extended instructions
        } else if (inst >= Ijmp && inst <= Itb) {
            if (inst < Isbo) {
            	Check.checkState(mop2.type == MachineOperand.OP_NONE);
            	if (mop1.type == MachineOperand.OP_IMMED) {
	                mop1.type = MachineOperand.OP_JUMP;
	                mop1.val = mop1.val - pc;
            	} else if (mop1.type != MachineOperand.OP_JUMP){
            		Check.checkState(false);
            	}
                mop1.bIsCodeDest = true;
                //this.stsetBefore = Instruction.st_ALL;
                this.reads |= INST_RSRC_ST;
                mop2.type = MachineOperand.OP_STATUS;
                //((MachineOperand) this.op2).val = st.flatten();
                this.jump = inst == Ijmp ? Instruction.INST_JUMP_TRUE
                        : Instruction.INST_JUMP_COND;
                this.cycles += getMemoryCycles(Pc, 8, 1);
            } else {
                mop1.type = MachineOperand.OP_OFFS_R12;
                this.cycles += getMemoryCycles(Pc, 12, 2);
            }
    
            switch (inst) {
            case Ijmp:
                this.reads &= ~INST_RSRC_ST;
                break;
            case Ijlt:
                this.stReads = Status.ST_A + Status.ST_E;
                break;
            case Ijle:
                this.stReads = Status.ST_A + Status.ST_E;
                break;
            case Ijeq:
                this.stReads = Status.ST_E;
                break;
            case Ijhe:
                this.stReads = Status.ST_L + Status.ST_E;
                break;
            case Ijgt:
                this.stReads = Status.ST_L +Status.ST_E;
                break;
            case Ijne:
                this.stReads = Status.ST_E;
                break;
            case Ijnc:
                this.stReads = Status.ST_C;
                break;
            case Ijoc:
                this.stReads = Status.ST_C;
                break;
            case Ijno:
                this.stReads = Status.ST_O;
                break;
            case Ijl:
                this.stReads = Status.ST_L + Status.ST_E;
                break;
            case Ijh:
                this.stReads = Status.ST_L + Status.ST_E;
                break;
            case Ijop:
                this.inst = Instruction.Ijop;
                this.stReads = Status.ST_P;
                break;
            case Isbo:
                this.writes |= INST_RSRC_IO;
                break;
            case Isbz:
                this.writes |= INST_RSRC_IO;
                break;
            case Itb:
                this.stsetAfter = Instruction.st_CMP;
                this.reads |= INST_RSRC_IO;
                break;
            }
    
        } else if (inst < Iszc && inst != Ildcr && inst != Istcr) {
            Check.checkState(mop1.type != MachineOperand.OP_NONE
                    && mop1.type != MachineOperand.OP_IMMED);
            Check.checkState(mop2.type == MachineOperand.OP_REG);
            mop1.dest = MachineOperand.OP_DEST_FALSE;
            mop2.dest = MachineOperand.OP_DEST_TRUE;
    
            switch (inst) {
            case Icoc:
                this.stsetAfter = Instruction.st_CMP;
                mop2.dest = MachineOperand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(Pc, 14, 3);
                break;
            case Iczc:
                this.stsetAfter = Instruction.st_CMP;
                mop2.dest = MachineOperand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(Pc, 14, 3);
                break;
            case Ixor:
                this.stsetAfter = Instruction.st_LAE;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Ixop:
                this.reads |= INST_RSRC_ST;
                this.writes |= INST_RSRC_CTX;
                //this.stsetBefore = Instruction.st_ALL;
                this.stsetAfter = Instruction.st_XOP;
                this.stReads = 0xffff;
                this.cycles += getMemoryCycles(Pc, 36, 8);
                break;
            case Impy:
                //              ((MachineOperand) this.op2).type = MachineOperand.OP_MPY;
                this.cycles += getMemoryCycles(Pc, 52, 5);
                break;
            case Idiv:
                this.stsetBefore = Instruction.st_DIV_O;
                //              ((MachineOperand) this.op2).type = MachineOperand.OP_DIV;
                this.cycles += getMemoryCycles(Pc, 124, 6);
                break;
            }
    
        } else if (inst == Ildcr || inst == Istcr) {
            Check.checkState(mop1.type != MachineOperand.OP_NONE
                    && mop1.type != MachineOperand.OP_IMMED);
            Check.checkState(mop2.type == MachineOperand.OP_IMMED);
            mop2.type = MachineOperand.OP_CNT;
            if (mop2.val == 0) {
				mop2.val = 16;
			}
            mop1.byteop = mop2.val <= 8;
    
            if (inst == Ildcr) {
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
            Check.checkState(mop1.type != MachineOperand.OP_NONE
                    && mop1.type != MachineOperand.OP_IMMED);
            Check.checkState(mop1.type != MachineOperand.OP_NONE
                    && mop1.type != MachineOperand.OP_IMMED);
            mop2.dest = MachineOperand.OP_DEST_TRUE;
            mop1.byteop = mop2.byteop = (inst - Iszc & 1) != 0;
    
            switch (inst) {
            case Iszc:
                this.stsetAfter = Instruction.st_LAE;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Iszcb:
                this.stsetAfter = Instruction.st_BYTE_LAEP;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Is:
                this.stsetBefore = Instruction.st_SUB_LAECO;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Isb:
                this.stsetBefore = Instruction.st_SUB_BYTE_LAECOP;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Ic:
                this.stsetAfter = Instruction.st_CMP;
                mop2.dest = MachineOperand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(Pc, 14, 3);
                break;
            case Icb:
                this.stsetAfter = Instruction.st_BYTE_CMP;
                mop2.dest = MachineOperand.OP_DEST_FALSE;
                this.cycles += getMemoryCycles(Pc, 14, 3);
                break;
            case Ia:
                this.stsetBefore = Instruction.st_ADD_LAECO;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Iab:
                this.stsetBefore = Instruction.st_ADD_BYTE_LAECOP;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Imov:
                this.stsetAfter = Instruction.st_LAE;
                mop2.dest = MachineOperand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Imovb:
                this.stsetAfter = Instruction.st_BYTE_LAEP;
                mop2.dest = MachineOperand.OP_DEST_KILLED;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Isoc:
                this.stsetAfter = Instruction.st_LAE;
                this.cycles += getMemoryCycles(Pc, 14, 4);
                break;
            case Isocb:
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
    
        // Finish reading operand immediates
        Pc += 2; // instruction itself
        Pc = mop1.advancePc((short)Pc);
        Pc = mop2.advancePc((short)Pc);
        this.size = (Pc & 0xffff) - (this.pc & 0xffff);
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
            return new Instruction(op, thePc, domain);
        }
        //this.status = (Status)st.clone();
        return this;
    }

    public int compareTo(Instruction o) {
    	return pc - o.pc;
    }

}