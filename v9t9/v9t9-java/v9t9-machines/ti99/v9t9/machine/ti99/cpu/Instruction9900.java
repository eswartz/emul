/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package v9t9.machine.ti99.cpu;



import v9t9.base.utils.Check;
import v9t9.base.utils.HexUtils;
import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstInfo;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.IStatus;
import v9t9.common.memory.IMemoryDomain;

import static v9t9.common.asm.IHighLevelInstruction.*;

/**
 * Implementation of a 9900 instruction which handles most details except executing it.
 * The basic number of cycles to execute an instruction is stored in 'cycles',
 * telling the basic cost of the instruction when it is executed.  Some instructions
 * have dynamic costs based on the values of their operands, however, so other 
 * layers must account for that.  Memory cycle costs are not counted here, but in
 * the memory handlers.
 * @author ejs
 */
public class Instruction9900 extends RawInstruction implements IInstruction {
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getName());
        String opstring;
        opstring = getOp1().toString();
        if (opstring != null) {
            buffer.append(' ');
            buffer.append(opstring);
            opstring = getOp2().toString();
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

    public Instruction9900(RawInstruction inst) {
    	super(inst);
    	completeInstruction(inst.pc);
    }
    public Instruction9900(Instruction9900 inst) {
    	super(inst);
    	this.opcode = inst.opcode;
    	this.setInfo(inst.getInfo());
	}

 
	/**
     * Finish filling in an instruction which is used for
     * higher-level operations.  This also establishes basic
     * cycle counts.
     *
     */
    public void completeInstruction(int Pc) {
    	InstInfo info = getInfo();
    	
	    info.stsetBefore = IStatus.stset_NONE;
	    info.stsetAfter = IStatus.stset_NONE;
	    info.stReads = 0;
	    info.stWrites = 0;
	    info.jump = InstInfo.INST_JUMP_FALSE;
	    info.reads = 0;
	    info.writes = 0;
	
	    MachineOperand9900 mop1 = new MachineOperand9900((MachineOperand9900) getOp1());
	    MachineOperand9900 mop2 = new MachineOperand9900((MachineOperand9900) getOp2());
	    
	    // Initially, this.op?.val is incomplete, and is whatever
	    // raw data from the opcode we can decode;
	    // this.op?.ea is that of the instruction or immediate
	    // if the operand needs it.
	
	    // after decoding the instruction, we complete
	    // the operand, making this.op?.val and this.op?.ea valid.
	
	    if (getInst() == InstTableCommon.Idata) {
	        Check.checkArg((mop1.type == MachineOperand9900.OP_IMMED));
	        Check.checkArg((mop2.type == IMachineOperand.OP_NONE));
	        Pc -= 2;
	        info.cycles += 6;
	    } else if (getInst() >= Inst9900.Ili && getInst() <= Inst9900.Ici) {
	        Check.checkArg((mop1.type == MachineOperand9900.OP_REG));
	        mop2.convertToImmedate();
	        mop1.dest = IOperand.OP_DEST_TRUE;
	        switch (getInst()) {
	        case Inst9900.Ili:
	            info.stsetAfter = Status9900.stset_LAE_1;
	            mop1.dest = IOperand.OP_DEST_KILLED;
	            info.cycles += 12;
	            break;
	        case Inst9900.Iai:
	            info.stsetBefore = Status9900.stset_ADD_LAECO_REV;
	            info.cycles += 14;
	            break;
	        case Inst9900.Iandi:
	            info.stsetAfter = Status9900.stset_LAE_1;
	            info.cycles += 14;
	            break;
	        case Inst9900.Iori:
	            info.stsetAfter = Status9900.stset_LAE_1;
	            info.cycles += 14;
	            break;
	        case Inst9900.Ici:
	            info.stsetAfter = Status9900.stset_CMP;
	            mop1.dest = IOperand.OP_DEST_FALSE;
	            info.cycles += 14;
	            break;
	        }
	
	    } else if (getInst() == Inst9900.Istwp) {
	        Check.checkArg((mop1.type == MachineOperand9900.OP_REG));
	        Check.checkArg((mop2.type == IMachineOperand.OP_NONE));
	        mop1.dest = IOperand.OP_DEST_KILLED;
	        info.reads |= InstInfo.INST_RSRC_WP;
	        info.cycles += 8;
	    } else if (getInst() == Inst9900.Istst) {
	        Check.checkArg((mop1.type == MachineOperand9900.OP_REG));
	        info.reads |= InstInfo.INST_RSRC_ST;
	        info.stReads = 0xffff;
	        mop1.dest = IOperand.OP_DEST_KILLED;
	        
	        Check.checkArg((mop2.type == IMachineOperand.OP_NONE));
	        mop2.type = MachineOperand9900.OP_STATUS;
	        info.cycles += 8;
	    } else if (getInst() == Inst9900.Ilwpi) {
	        Check.checkArg((mop1.type == MachineOperand9900.OP_IMMED));
	        Check.checkArg((mop2.type == IMachineOperand.OP_NONE));
	        info.writes |= InstInfo.INST_RSRC_WP;
	        info.cycles += 10;
	    } else if (getInst() == Inst9900.Ilimi) {
	        Check.checkArg((mop1.type == MachineOperand9900.OP_IMMED));
	        Check.checkArg((mop2.type == IMachineOperand.OP_NONE));
	        info.stsetAfter = IStatus.stset_INT;
	        info.cycles += 16;
	    } else if (getInst() >= Inst9900.Iidle && getInst() <= Inst9900.Ilrex) {
	        Check.checkArg((mop1.type == IMachineOperand.OP_NONE));
	        Check.checkArg((mop2.type == IMachineOperand.OP_NONE));
	        switch (getInst()) {
	        case Inst9900.Iidle:
	            info.writes |= InstInfo.INST_RSRC_IO;
	            info.cycles += 12;
	            break;
	        case Inst9900.Irset:
	            info.stsetAfter = IStatus.stset_INT;
	            info.writes |= InstInfo.INST_RSRC_IO;
	            info.cycles += 12;
	            break;
	        case Inst9900.Irtwp:
	            info.stsetAfter = IStatus.stset_ALL;
	            info.writes |= InstInfo.INST_RSRC_WP + InstInfo.INST_RSRC_ST + InstInfo.INST_RSRC_PC;
	            mop1.type = MachineOperand9900.OP_STATUS;
	            //mop1.dest = Operand.OP_DEST_KILLED;	// compiler doesn't seem to depend on this
	            info.jump = InstInfo.INST_JUMP_TRUE;
	            info.cycles += 14;
	            break;
	        case Inst9900.Ickon:
	            info.writes |= InstInfo.INST_RSRC_IO;
	            info.cycles += 12;
	            break;
	        case Inst9900.Ickof:
	            info.writes |= InstInfo.INST_RSRC_IO;
	            info.cycles += 12;
	            break;
	        case Inst9900.Ilrex:
	            info.writes |= InstInfo.INST_RSRC_IO;
	            info.cycles += 12;
	            break;
	        }
	
	    } else if (getInst() >= Inst9900.Iblwp && getInst() <= Inst9900.Iabs) {
	        Check.checkArg((mop1.type != IMachineOperand.OP_NONE
			&& mop1.type != MachineOperand9900.OP_IMMED));
	        Check.checkArg((mop2.type == IMachineOperand.OP_NONE));
	        mop1.dest = IOperand.OP_DEST_TRUE;
	
	        switch (getInst()) {
	        case Inst9900.Iblwp:
	            //this.stsetBefore = Instruction.st_ALL;
	            info.stReads = 0xffff;
	            info.reads |= InstInfo.INST_RSRC_ST;
	            info.writes |= InstInfo.INST_RSRC_WP + InstInfo.INST_RSRC_PC + InstInfo.INST_RSRC_CTX;
	            mop1.dest = IOperand.OP_DEST_FALSE;
	            mop1.bIsReference = true;
	            info.jump = InstInfo.INST_JUMP_TRUE;
	            info.cycles += 26;
	            break;
	        case Inst9900.Ib:
	            mop1.dest = IOperand.OP_DEST_FALSE;
	            mop1.bIsReference = true;
	            info.jump = InstInfo.INST_JUMP_TRUE;
	            info.cycles += 8;
	            break;
	        case Inst9900.Ix:
	            //this.stsetBefore = Instruction.st_ALL;
	            info.stReads = 0xffff;
	            info.reads |= InstInfo.INST_RSRC_ST;
	            mop1.dest = IOperand.OP_DEST_FALSE;
	            mop2.type = MachineOperand9900.OP_INST;
	            info.cycles += 8;
	            break;
	        case Inst9900.Iclr:
	            mop1.dest = IOperand.OP_DEST_KILLED;
	            info.cycles += 10;
	            break;
	        case Inst9900.Ineg:
	            info.stsetAfter = Status9900.stset_LAEO;
	            info.cycles += 12;
	            break;
	        case Inst9900.Iinv:
	            info.stsetAfter = Status9900.stset_LAE_1;
	            info.cycles += 10;
	            break;
	        case Inst9900.Iinc:
	            info.stsetBefore = Status9900.stset_ADD_LAECO_REV_1;
	            info.cycles += 10;
	            break;
	        case Inst9900.Iinct:
	            info.stsetBefore = Status9900.stset_ADD_LAECO_REV_2;
	            info.cycles += 10;
	            break;
	        case Inst9900.Idec:
	            info.stsetBefore = Status9900.stset_ADD_LAECO_REV_N1;
	            info.cycles += 10;
	            break;
	        case Inst9900.Idect:
	            info.stsetBefore = Status9900.stset_ADD_LAECO_REV_N2;
	            info.cycles += 10;
	            break;
	        case Inst9900.Ibl:
	            mop1.dest = IOperand.OP_DEST_FALSE;
	            mop1.bIsReference = true;
	            info.jump = InstInfo.INST_JUMP_TRUE;
	            info.cycles += 12;
	            break;
	        case Inst9900.Iswpb:
	            info.cycles += 10;
	            break;
	        case Inst9900.Iseto:
	            mop1.dest = IOperand.OP_DEST_KILLED;
	            info.cycles += 10;
	            break;
	        case Inst9900.Iabs:
	            info.stsetBefore = Status9900.stset_LAEO;
	            info.cycles += 12;
	            break;
	        default:
	            mop1.dest = IOperand.OP_DEST_FALSE;
	            break;
	        }
	
	    } else if (getInst() >= Inst9900.Isra && getInst() <= Inst9900.Isrc) {
	        Check.checkArg((mop1.type == MachineOperand9900.OP_REG));
	        Check.checkArg((mop2.type == MachineOperand9900.OP_IMMED || mop2.type == MachineOperand9900.OP_CNT));
	        mop1.dest = IOperand.OP_DEST_TRUE;
	        mop2.type = MachineOperand9900.OP_CNT;
	
	        // shift of zero comes from R0
	        if (mop2.val == 0) {
	            mop2.type = MachineOperand9900.OP_REG0_SHIFT_COUNT;
	            info.cycles += 20;
	        } else {
	            info.cycles += 12;
	        }
	
	        switch (getInst()) {
	        case Inst9900.Isra:
	            info.stsetBefore = Status9900.stset_SHIFT_RIGHT_C;
	            info.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Isrl:
	            info.stsetBefore = Status9900.stset_SHIFT_RIGHT_C;
	            info.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Isla:
	            info.stsetBefore = Status9900.stset_SHIFT_LEFT_CO;
	            info.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Isrc:
	            info.stsetBefore = Status9900.stset_SHIFT_RIGHT_C;
	            info.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        }
	
	    } else if (false) {
	        // TODO: extended instructions
	    } else if (getInst() >= Inst9900.Ijmp && getInst() <= Inst9900.Itb) {
	        if (getInst() < Inst9900.Isbo) {
	        	Check.checkArg((mop2.type == IMachineOperand.OP_NONE));
	        	if (mop1.type == MachineOperand9900.OP_IMMED) {
	                mop1.type = MachineOperand9900.OP_JUMP;
	                mop1.val = mop1.val - pc;
	        	} else if (mop1.type != MachineOperand9900.OP_JUMP){
	        		Check.checkArg(false);
	        	}
	            mop1.bIsReference = true;
	            //this.stsetBefore = Instruction.st_ALL;
	            info.reads |= InstInfo.INST_RSRC_ST;
	            mop2.type = MachineOperand9900.OP_STATUS;
	            //((MachineOperand) this.op2).val = st.flatten();
	            info.jump = getInst() == Inst9900.Ijmp ? InstInfo.INST_JUMP_TRUE
	                    : InstInfo.INST_JUMP_COND;
	            info.cycles += 8;
	        } else {
	            mop1.type = MachineOperand9900.OP_OFFS_R12;
	            mop1.val <<= 1;
	            info.cycles += 12;
	        }
	
	        switch (getInst()) {
	        case Inst9900.Ijmp:
	            info.reads &= ~InstInfo.INST_RSRC_ST;
	            break;
	        case Inst9900.Ijlt:
	            info.stReads = Status9900.ST_A + Status9900.ST_E;
	            break;
	        case Inst9900.Ijle:
	            info.stReads = Status9900.ST_A + Status9900.ST_E;
	            break;
	        case Inst9900.Ijeq:
	            info.stReads = Status9900.ST_E;
	            break;
	        case Inst9900.Ijhe:
	            info.stReads = Status9900.ST_L + Status9900.ST_E;
	            break;
	        case Inst9900.Ijgt:
	            info.stReads = Status9900.ST_L +Status9900.ST_E;
	            break;
	        case Inst9900.Ijne:
	            info.stReads = Status9900.ST_E;
	            break;
	        case Inst9900.Ijnc:
	            info.stReads = Status9900.ST_C;
	            break;
	        case Inst9900.Ijoc:
	            info.stReads = Status9900.ST_C;
	            break;
	        case Inst9900.Ijno:
	            info.stReads = Status9900.ST_O;
	            break;
	        case Inst9900.Ijl:
	            info.stReads = Status9900.ST_L + Status9900.ST_E;
	            break;
	        case Inst9900.Ijh:
	            info.stReads = Status9900.ST_L + Status9900.ST_E;
	            break;
	        case Inst9900.Ijop:
	            info.stReads = Status9900.ST_P;
	            break;
	        case Inst9900.Isbo:
	            info.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        case Inst9900.Isbz:
	            info.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        case Inst9900.Itb:
	            info.stsetAfter = Status9900.stset_CMP;
	            info.reads |= InstInfo.INST_RSRC_IO;
	            break;
	        }
	
	    } else if (getInst() > 0 && getInst() < Inst9900.Iszc && getInst() != Inst9900.Ildcr && getInst() != Inst9900.Istcr) {
	        Check.checkArg((mop1.type != IMachineOperand.OP_NONE
			&& mop1.type != MachineOperand9900.OP_IMMED));
	        Check.checkArg((mop2.type == MachineOperand9900.OP_REG));
	        mop1.dest = IOperand.OP_DEST_FALSE;
	        mop2.dest = IOperand.OP_DEST_TRUE;
	
	        switch (getInst()) {
	        case Inst9900.Icoc:
	            info.stsetAfter = Status9900.stset_CMP;
	            mop2.dest = IOperand.OP_DEST_FALSE;
	            info.cycles += 14;
	            break;
	        case Inst9900.Iczc:
	            info.stsetAfter = Status9900.stset_CMP;
	            mop2.dest = IOperand.OP_DEST_FALSE;
	            info.cycles += 14;
	            break;
	        case Inst9900.Ixor:
	            info.stsetAfter = Status9900.stset_LAE;
	            info.cycles += 14;
	            break;
	        case Inst9900.Ixop:
	        	info.stReads = 0xffff;
	            info.reads |= InstInfo.INST_RSRC_ST;
	            info.writes |= InstInfo.INST_RSRC_WP + InstInfo.INST_RSRC_PC + InstInfo.INST_RSRC_CTX;
	            mop2.bIsReference = true;
	            mop2.type = MachineOperand9900.OP_CNT;
	            mop2.dest = IOperand.OP_DEST_FALSE;
	            info.stsetAfter = Status9900.stset_XOP;
	            info.jump = InstInfo.INST_JUMP_TRUE;
	            info.cycles += 36;
	            break;
	        case Inst9900.Impy:
	            //              ((MachineOperand) this.op2).type = MachineOperand.OP_MPY;
	            info.cycles += 52;
	            break;
	        case Inst9900.Idiv:
	            info.stsetBefore = Status9900.stset_DIV_O;
	            //              ((MachineOperand) this.op2).type = MachineOperand.OP_DIV;
	            info.cycles += 124;
	            break;
	        }
	
	    } else if (getInst() == Inst9900.Ildcr || getInst() == Inst9900.Istcr) {
	        Check.checkArg((mop1.type != IMachineOperand.OP_NONE
			&& mop1.type != MachineOperand9900.OP_IMMED));
	        Check.checkArg((mop2.type == MachineOperand9900.OP_IMMED || mop2.type == MachineOperand9900.OP_CNT));
	        mop2.type = MachineOperand9900.OP_CNT;
	        if (mop2.val == 0) {
				mop2.val = 16;
			}
	        mop1.byteop = mop2.val <= 8;
	
	        if (getInst() == Inst9900.Ildcr) {
	            info.stsetBefore = mop1.byteop ? Status9900.stset_BYTE_LAEP_1
	                    : Status9900.stset_LAE_1;
	            mop1.dest = IOperand.OP_DEST_FALSE;
	            info.cycles += (20 + 2 * mop1.val);
	            info.writes |= InstInfo.INST_RSRC_IO;
	        } else {
	            info.stsetAfter = mop1.byteop ? Status9900.stset_BYTE_LAEP_1
	                    : Status9900.stset_LAE_1;
	            mop1.dest = IOperand.OP_DEST_TRUE;
	            info.cycles += (mop1.val < 8 ? 42
				: mop1.val == 8 ? 44 : 58);
	            info.reads |= InstInfo.INST_RSRC_IO;
	        }
	
	    } else if (getInst() == InstTableCommon.Idsr) {
	    	
	    } else if (getInst() == InstTableCommon.Iticks) {
	    	mop1.dest = IOperand.OP_DEST_TRUE;
	    	info.reads |= InstInfo.INST_RSRC_IO;
	    	info.cycles += 6;
	    } else {
	        Check.checkArg((mop1.type != IMachineOperand.OP_NONE
			&& mop1.type != MachineOperand9900.OP_IMMED));
	        Check.checkArg((mop1.type != IMachineOperand.OP_NONE
			&& mop1.type != MachineOperand9900.OP_IMMED));
	        mop2.dest = IOperand.OP_DEST_TRUE;
	        mop1.byteop = mop2.byteop = (getInst() - Inst9900.Iszc & 1) != 0;
	
	        switch (getInst()) {
	        case Inst9900.Iszc:
	            info.stsetAfter = Status9900.stset_LAE;
	            info.cycles += 14;
	            break;
	        case Inst9900.Iszcb:
	            info.stsetAfter = Status9900.stset_BYTE_LAEP;
	            info.cycles += 14;
	            break;
	        case Inst9900.Is:
	            info.stsetBefore = Status9900.stset_SUB_LAECO;
	            info.cycles += 14;
	            break;
	        case Inst9900.Isb:
	            info.stsetBefore = Status9900.stset_SUB_BYTE_LAECOP;
	            info.cycles += 14;
	            break;
	        case Inst9900.Ic:
	            info.stsetAfter = Status9900.stset_CMP;
	            mop2.dest = IOperand.OP_DEST_FALSE;
	            info.cycles += 14;
	            break;
	        case Inst9900.Icb:
	            info.stsetAfter = Status9900.stset_BYTE_CMP;
	            mop2.dest = IOperand.OP_DEST_FALSE;
	            info.cycles += 14;
	            break;
	        case Inst9900.Ia:
	            info.stsetBefore = Status9900.stset_ADD_LAECO;
	            info.cycles += 14;
	            break;
	        case Inst9900.Iab:
	            info.stsetBefore = Status9900.stset_ADD_BYTE_LAECOP;
	            info.cycles += 14;
	            break;
	        case Inst9900.Imov:
	            info.stsetAfter = Status9900.stset_LAE;
	            mop2.dest = IOperand.OP_DEST_KILLED;
	            info.cycles += 14;
	            break;
	        case Inst9900.Imovb:
	            info.stsetAfter = Status9900.stset_BYTE_LAEP;
	            mop2.dest = IOperand.OP_DEST_KILLED;
	            info.cycles += 14;
	            break;
	        case Inst9900.Isoc:
	            info.stsetAfter = Status9900.stset_LAE;
	            info.cycles += 14;
	            break;
	        case Inst9900.Isocb:
	            info.stsetAfter = Status9900.stset_BYTE_LAEP;
	            info.cycles += 14;
	            break;
	        }
	    }
	
	    info.stWrites = Status9900.getStatusBits(info.stsetBefore)
        	| Status9900.getStatusBits(info.stsetAfter);
    
	    // synthesize bits from other info
	    if (info.jump != InstInfo.INST_JUMP_FALSE) {
	        info.writes |= InstInfo.INST_RSRC_PC;
	        info.reads |= InstInfo.INST_RSRC_PC;
	    }
	    if (getInfo().stsetBefore != IStatus.stset_NONE || getInfo().stsetAfter != IStatus.stset_NONE) {
	        info.writes |= InstInfo.INST_RSRC_ST;
	    }
	    if (mop1.isRegisterReference() || mop2.isRegisterReference()) {
	        info.reads |= InstInfo.INST_RSRC_WP;
	    }
	
	    if (getInst() != InstTableCommon.Idata && getInst() != InstTableCommon.Ibyte) {
			 // Finish reading operand immediates
		    Pc += 2; // instruction itself
		    Pc = ((IMachineOperand) getOp1()).advancePc((short)Pc);
		    Pc = ((IMachineOperand) getOp2()).advancePc((short)Pc);
		    this.setSize((Pc & 0xffff) - (this.pc & 0xffff));		
	    }
	    
	    this.setOp1(mop1);
	    this.setOp2(mop2);
	    //super.completeInstruction(Pc);
	   
	}

    /**
     * Get static effects of an instruction.
     *
     */
    /*
    public static Effects getInstructionEffects(int inst) {
    	Effects fx = new Effects();
    	
	    fx.stsetBefore = IStatus.stset_NONE;
	    fx.stsetAfter = IStatus.stset_NONE;
	    fx.stReads = 0;
	    fx.jump = InstInfo.INST_JUMP_FALSE;
	    fx.reads = 0;
	    fx.writes = 0;
	    fx.byteop = false;
	
	    if (inst == InstTableCommon.Idata) {
	    } else if (inst >= Inst9900.Ili && inst <= Inst9900.Ici) {
	        fx.mop1_dest = IOperand.OP_DEST_TRUE;
	        switch (inst) {
	        case Inst9900.Ili:
	            fx.stsetAfter = Status9900.stset_LAE_1;
	            fx.mop1_dest = IOperand.OP_DEST_KILLED;
	            break;
	        case Inst9900.Iai:
	            fx.stsetBefore = Status9900.stset_ADD_LAECO_REV;
	            break;
	        case Inst9900.Iandi:
	            fx.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Iori:
	            fx.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Ici:
	            fx.stsetAfter = Status9900.stset_CMP;
	            fx.mop1_dest = IOperand.OP_DEST_FALSE;
	            break;
	        }
	
	    } else if (inst == Inst9900.Istwp) {
	        fx.mop1_dest = IOperand.OP_DEST_KILLED;
	        fx.reads |= InstInfo.INST_RSRC_WP;
	    } else if (inst == Inst9900.Istst) {
	        fx.reads |= InstInfo.INST_RSRC_ST;
	        fx.stReads = 0xffff;
	        fx.mop1_dest = IOperand.OP_DEST_KILLED;
	    } else if (inst == Inst9900.Ilwpi) {
	        fx.writes |= InstInfo.INST_RSRC_WP;
	    } else if (inst == Inst9900.Ilimi) {
	        fx.stsetAfter = IStatus.stset_INT;
	    } else if (inst >= Inst9900.Iidle && inst <= Inst9900.Ilrex) {
	        switch (inst) {
	        case Inst9900.Iidle:
	            fx.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        case Inst9900.Irset:
	            fx.stsetAfter = IStatus.stset_INT;
	            fx.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        case Inst9900.Irtwp:
	            fx.stsetAfter = IStatus.stset_ALL;
	            fx.writes |= InstInfo.INST_RSRC_WP + InstInfo.INST_RSRC_ST + InstInfo.INST_RSRC_PC;
	            //fx.mop1_dest = Operand.OP_DEST_KILLED;	// compiler doesn't seem to depend on this
	            fx.jump = InstInfo.INST_JUMP_TRUE;
	            break;
	        case Inst9900.Ickon:
	            fx.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        case Inst9900.Ickof:
	            fx.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        case Inst9900.Ilrex:
	            fx.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        }
	
	    } else if (inst >= Inst9900.Iblwp && inst <= Inst9900.Iabs) {
	        fx.mop1_dest = IOperand.OP_DEST_TRUE;
	
	        switch (inst) {
	        case Inst9900.Iblwp:
	            //fx.stsetBefore = Instruction.st_ALL;
	            fx.stReads = 0xffff;
	            fx.reads |= InstInfo.INST_RSRC_ST;
	            fx.writes |= InstInfo.INST_RSRC_WP + InstInfo.INST_RSRC_PC + InstInfo.INST_RSRC_CTX;
	            fx.mop1_dest = IOperand.OP_DEST_FALSE;
	            //mop1.bIsCodeDest = true;
	            fx.jump = InstInfo.INST_JUMP_TRUE;
	            break;
	        case Inst9900.Ib:
	            fx.mop1_dest = IOperand.OP_DEST_FALSE;
	            //mop1.bIsCodeDest = true;
	            fx.jump = InstInfo.INST_JUMP_TRUE;
	            break;
	        case Inst9900.Ix:
	            //fx.stsetBefore = Instruction.st_ALL;
	            fx.stReads = 0xffff;
	            fx.reads |= InstInfo.INST_RSRC_ST;
	            fx.mop1_dest = IOperand.OP_DEST_FALSE;
	            //mop2.type = MachineOperand.OP_INST;
	            break;
	        case Inst9900.Iclr:
	            fx.mop1_dest = IOperand.OP_DEST_KILLED;
	            break;
	        case Inst9900.Ineg:
	            fx.stsetAfter = Status9900.stset_LAEO;
	            break;
	        case Inst9900.Iinv:
	            fx.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Iinc:
	            fx.stsetBefore = Status9900.stset_ADD_LAECO_REV_1;
	            break;
	        case Inst9900.Iinct:
	            fx.stsetBefore = Status9900.stset_ADD_LAECO_REV_2;
	            break;
	        case Inst9900.Idec:
	            fx.stsetBefore = Status9900.stset_ADD_LAECO_REV_N1;
	            break;
	        case Inst9900.Idect:
	            fx.stsetBefore = Status9900.stset_ADD_LAECO_REV_N2;
	            break;
	        case Inst9900.Ibl:
	            fx.mop1_dest = IOperand.OP_DEST_FALSE;
	            //mop1.bIsCodeDest = true;
	            fx.jump = InstInfo.INST_JUMP_TRUE;
	            break;
	        case Inst9900.Iswpb:
	            break;
	        case Inst9900.Iseto:
	            fx.mop1_dest = IOperand.OP_DEST_KILLED;
	            break;
	        case Inst9900.Iabs:
	            fx.stsetBefore = Status9900.stset_LAEO;
	            break;
	        default:
	            fx.mop1_dest = IOperand.OP_DEST_FALSE;
	            break;
	        }
	
	    } else if (inst >= Inst9900.Isra && inst <= Inst9900.Isrc) {
	        fx.mop1_dest = IOperand.OP_DEST_TRUE;
	
	        switch (inst) {
	        case Inst9900.Isra:
	            fx.stsetBefore = Status9900.stset_SHIFT_RIGHT_C;
	            fx.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Isrl:
	            fx.stsetBefore = Status9900.stset_SHIFT_RIGHT_C;
	            fx.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Isla:
	            fx.stsetBefore = Status9900.stset_SHIFT_LEFT_CO;
	            fx.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        case Inst9900.Isrc:
	            fx.stsetBefore = Status9900.stset_SHIFT_RIGHT_C;
	            fx.stsetAfter = Status9900.stset_LAE_1;
	            break;
	        }
	
	    } else if (false) {
	        // TODO: extended instructions
	    } else if (inst >= Inst9900.Ijmp && inst <= Inst9900.Itb) {
	        if (inst < Inst9900.Isbo) {
	            fx.reads |= InstInfo.INST_RSRC_ST;
	            fx.jump = inst == Inst9900.Ijmp ? InstInfo.INST_JUMP_TRUE
	                    : InstInfo.INST_JUMP_COND;
	        }
	
	        switch (inst) {
	        case Inst9900.Ijmp:
	            fx.reads &= ~InstInfo.INST_RSRC_ST;
	            break;
	        case Inst9900.Ijlt:
	            fx.stReads = Status9900.ST_A + Status9900.ST_E;
	            break;
	        case Inst9900.Ijle:
	            fx.stReads = Status9900.ST_A + Status9900.ST_E;
	            break;
	        case Inst9900.Ijeq:
	            fx.stReads = Status9900.ST_E;
	            break;
	        case Inst9900.Ijhe:
	            fx.stReads = Status9900.ST_L + Status9900.ST_E;
	            break;
	        case Inst9900.Ijgt:
	            fx.stReads = Status9900.ST_L +Status9900.ST_E;
	            break;
	        case Inst9900.Ijne:
	            fx.stReads = Status9900.ST_E;
	            break;
	        case Inst9900.Ijnc:
	            fx.stReads = Status9900.ST_C;
	            break;
	        case Inst9900.Ijoc:
	            fx.stReads = Status9900.ST_C;
	            break;
	        case Inst9900.Ijno:
	            fx.stReads = Status9900.ST_O;
	            break;
	        case Inst9900.Ijl:
	            fx.stReads = Status9900.ST_L + Status9900.ST_E;
	            break;
	        case Inst9900.Ijh:
	            fx.stReads = Status9900.ST_L + Status9900.ST_E;
	            break;
	        case Inst9900.Ijop:
	            fx.stReads = Status9900.ST_P;
	            break;
	        case Inst9900.Isbo:
	            fx.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        case Inst9900.Isbz:
	            fx.writes |= InstInfo.INST_RSRC_IO;
	            break;
	        case Inst9900.Itb:
	            fx.stsetAfter = Status9900.stset_CMP;
	            fx.reads |= InstInfo.INST_RSRC_IO;
	            break;
	        }
	
	    } else if (inst > 0 && inst < Inst9900.Iszc && inst != Inst9900.Ildcr && inst != Inst9900.Istcr) {
	        fx.mop1_dest = IOperand.OP_DEST_FALSE;
	        fx.mop2_dest = IOperand.OP_DEST_TRUE;
	
	        switch (inst) {
	        case Inst9900.Icoc:
	            fx.stsetAfter = Status9900.stset_CMP;
	            fx.mop2_dest = IOperand.OP_DEST_FALSE;
	            break;
	        case Inst9900.Iczc:
	            fx.stsetAfter = Status9900.stset_CMP;
	            fx.mop2_dest = IOperand.OP_DEST_FALSE;
	            break;
	        case Inst9900.Ixor:
	            fx.stsetAfter = Status9900.stset_LAE;
	            break;
	        case Inst9900.Ixop:
	        	fx.stReads = 0xffff;
	            fx.reads |= InstInfo.INST_RSRC_ST;
	            fx.writes |= InstInfo.INST_RSRC_WP + InstInfo.INST_RSRC_PC + InstInfo.INST_RSRC_CTX;
	            //mop2.bIsCodeDest = true;
	            fx.mop2_dest = IOperand.OP_DEST_FALSE;
	            fx.stsetAfter = Status9900.stset_XOP;
	            fx.jump = InstInfo.INST_JUMP_TRUE;
	            break;
	        case Inst9900.Impy:
	        	fx.mop3_dest = IOperand.OP_DEST_KILLED;
	            break;
	        case Inst9900.Idiv:
	            fx.stsetBefore = Status9900.stset_DIV_O;
	            fx.mop3_dest = IOperand.OP_DEST_KILLED;
	            break;
	        }
	
	    } else if (inst == Inst9900.Ildcr || inst == Inst9900.Istcr) {
	        if (inst == Inst9900.Ildcr) {
	            fx.stsetBefore = true //mop1.byteop 
    					//? Status9900.stset_BYTE_LAEP_1
	                    //: Status9900.stset_LAE_1
	                     ;
	            fx.mop1_dest = IOperand.OP_DEST_FALSE;
	            fx.writes |= InstInfo.INST_RSRC_IO;
	        } else {
	            fx.stsetAfter = true //mop1.byteop 
    					//? Status9900.stset_BYTE_LAEP_1
	                    //: Status9900.stset_LAE_1
	                    ;
	            fx.mop1_dest = IOperand.OP_DEST_TRUE;
	            fx.reads |= InstInfo.INST_RSRC_IO;
	        }
	
	    } else if (inst == InstTableCommon.Idsr) {
	    	
	    } else if (inst == InstTableCommon.Iticks) {
	    	fx.mop1_dest = IOperand.OP_DEST_TRUE;
	    	fx.reads |= InstInfo.INST_RSRC_IO;
	    } else if (inst > 0 && inst < InstTableCommon.Iuser) {
	        fx.mop2_dest = IOperand.OP_DEST_TRUE;
	
	        switch (inst) {
	        case Inst9900.Iszc:
	            fx.stsetAfter = Status9900.stset_LAE;
	            break;
	        case Inst9900.Iszcb:
	            fx.stsetAfter = Status9900.stset_BYTE_LAEP;
	            fx.byteop = true;
	            break;
	        case Inst9900.Is:
	            fx.stsetBefore = Status9900.stset_SUB_LAECO;
	            break;
	        case Inst9900.Isb:
	            fx.stsetBefore = Status9900.stset_SUB_BYTE_LAECOP;
	            fx.byteop = true;
	            break;
	        case Inst9900.Ic:
	            fx.stsetAfter = Status9900.stset_CMP;
	            fx.mop2_dest = IOperand.OP_DEST_FALSE;
	            break;
	        case Inst9900.Icb:
	            fx.stsetAfter = Status9900.stset_BYTE_CMP;
	            fx.mop2_dest = IOperand.OP_DEST_FALSE;
	            fx.byteop = true;
	            break;
	        case Inst9900.Ia:
	            fx.stsetBefore = Status9900.stset_ADD_LAECO;
	            break;
	        case Inst9900.Iab:
	            fx.stsetBefore = Status9900.stset_ADD_BYTE_LAECOP;
	            fx.byteop = true;
	            break;
	        case Inst9900.Imov:
	            fx.stsetAfter = Status9900.stset_LAE;
	            fx.mop2_dest = IOperand.OP_DEST_KILLED;
	            break;
	        case Inst9900.Imovb:
	            fx.stsetAfter = Status9900.stset_BYTE_LAEP;
	            fx.mop2_dest = IOperand.OP_DEST_KILLED;
	            fx.byteop = true;
	            break;
	        case Inst9900.Isoc:
	            fx.stsetAfter = Status9900.stset_LAE;
	            break;
	        case Inst9900.Isocb:
	            fx.stsetAfter = Status9900.stset_BYTE_LAEP;
	            fx.byteop = true;
	            break;
	        }
	    } else {
	    	return null;
	    }
	
	    // synthesize bits from other info
	    if (fx.jump != InstInfo.INST_JUMP_FALSE) {
	        fx.writes |= InstInfo.INST_RSRC_PC;
	        fx.reads |= InstInfo.INST_RSRC_PC;
	    }
	    if (fx.stsetBefore != IStatus.stset_NONE || fx.stsetAfter != IStatus.stset_NONE) {
	        fx.writes |= InstInfo.INST_RSRC_ST;
	    }
	    //if (mop1.isRegisterReference() || mop2.isRegisterReference()) {
	        //fx.reads |= INST_RSRC_WP;
	    //}
	    return fx;
    }
	*/
    
	/** 
     * Update a previously decoded instruction, only rebuilding it
     * if its memory changed (self-modifying code).
     * @param pc2
     * @param wp2
     * @param status2
     */
    public Instruction9900 update(short op, int thePc, IMemoryDomain domain) {
    	boolean isSame = true;
    	// obvious changes: this usually happens due to an X instruction and its generated instruction
        if (this.opcode != op || this.pc != thePc) {
            //if (this.pc != thePc) {
			//	throw new AssertionError("wrong PC? " + v9t9.utils.Utils.toHex4(this.pc) + " != " + v9t9.utils.Utils.toHex4(thePc));
			//}
            isSame = false;
        } else {
        	// check for modified immediates (the other kind of self-modifying code)
        	int pcStep = (thePc + 2) & 0xfffe;
        	MachineOperand9900 mop1 = (MachineOperand9900)getOp1();
        	if (getInst() != InstTableCommon.Idata && mop1.type != IMachineOperand.OP_NONE) {
        		mop1.cycles = 0;
				if (mop1.hasImmediate()) {
					if (domain.readWord(pcStep) != mop1.immed)
						isSame = false;
					pcStep += 2;
        		} else {
        			MachineOperand9900 mop2 = (MachineOperand9900)getOp2();
        			if (mop2.type != IMachineOperand.OP_NONE && mop2.hasImmediate()) {
        				if (domain.readWord(pcStep) != mop2.immed) {
        					isSame = false;
        				}
        				mop2.cycles = 0;
        			}
        		}
        	}
        }
        
        if (isSame) {
        	return this;
        }
        
        if (false) 
        	System.out.println("need to regenerate instruction: >" + HexUtils.toHex4(thePc) + " "+ this);
        
        return new Instruction9900(InstTable9900.decodeInstruction(op, thePc, domain));

    }

    public int compareTo(Instruction9900 o) {
    	return pc - o.pc;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getInst();
		result = prime * result + ((getOp1() == null) ? 0 : getOp1().hashCode());
		result = prime * result + ((getOp2() == null) ? 0 : getOp2().hashCode());
		result = prime * result + pc;
		result = prime * result + getSize();
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
		Instruction9900 other = (Instruction9900) obj;
		if (getInst() != other.getInst()) {
			return false;
		}
		if (getOp1() == null) {
			if (other.getOp1() != null) {
				return false;
			}
		} else if (!getOp1().equals(other.getOp1())) {
			return false;
		}
		if (getOp2() == null) {
			if (other.getOp2() != null) {
				return false;
			}
		} else if (!getOp2().equals(other.getOp2())) {
			return false;
		}
		if (pc != other.pc) {
			return false;
		}
		if (getSize() != other.getSize()) {
			return false;
		}
		return true;
	}


	public static int getInstructionFlags(RawInstruction inst) {
		int flags = 0;
        if (inst.getInfo().jump != 0) {
        	flags |=  fEndsBlock;
            if (inst.getInst() == Inst9900.Ibl || inst.getInst() == Inst9900.Iblwp) {
				flags |= fIsCall+fIsBranch;
			} else if (inst.getInst() == Inst9900.Irtwp) {
				flags |= fIsReturn+fIsBranch+fNotFallThrough; /* B *R11 detected later */
			} else if (inst.getInfo().jump == InstInfo.INST_JUMP_COND) {
				flags |= fIsCondBranch+fIsBranch;
			} else {
				//if (inst == Ib && op1 instanceof MachineOperand 
                 //       && ((MachineOperand)op1).type == MachineOperand.OP_ADDR) {
			//		flags |= fIsBranch+fCheckLater+fNotFallThrough;
				flags |= fIsBranch+fNotFallThrough;
			}
        }
        if (inst.getInst() == Inst9900.Imovb || inst.getInst() == Inst9900.Isocb || inst.getInst() == Inst9900.Iab || inst.getInst() == Inst9900.Isb
        		|| inst.getInst() == Inst9900.Icb || inst.getInst() == Inst9900.Iszcb) {
        	flags |= fByteOp;
        } else if ((inst.getInst() == Inst9900.Istcr || inst.getInst() == Inst9900.Ildcr)
        		&& inst.getOp2() instanceof IMachineOperand
        		&& ((BaseMachineOperand) inst.getOp2()).val <= 8) {
        	flags |= fByteOp;
        } else if (inst.getInst() == Inst9900.Ilimi) {
        	if (((BaseMachineOperand) inst.getOp1()).immed != 0) {
        		// likely block end
        		flags |= fEndsBlock;
        	}
        }
        return flags;
    }
}