/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.asm.decomp;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.ejs.coffee.core.utils.Check;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.BaseMachineOperand;
import v9t9.engine.cpu.Inst9900;
import v9t9.engine.cpu.InstInfo;
import v9t9.engine.cpu.InstTableCommon;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.engine.cpu.RawInstruction;
import v9t9.tools.asm.common.DataWordListOperand;
import v9t9.tools.asm.common.LabelOperand;

/**
 * Instruction augmented with decompiler info
 * @author ejs
 */
public class HighLevelInstruction  implements Comparable<HighLevelInstruction>{
    /* instruction flags */
	/** changes PC */
    final public static int fIsBranch = 1; /* changes PC */

    /** conditional branch (with fIsBranch) */
    final public static int fIsCondBranch = 2; 

    /** a branch we expect to return (with fIsBranch) */
    final public static int fIsCall = 4; 

    /** return from function */
    final public static int fIsReturn = 8; 

    final public static int fCheckLater = 16; /* guessed flags */

    /** this instruction should end a block */
    final public static int fEndsBlock = 32;

    final public static int fVisited = 64;  /* temporary flag */

    /** unknown behavior */
    public static final int fUnknown = 128;
    
    /** byte operation */
    public static final int fByteOp = 256;

    /** this instruction does not fall through (with fEndsBlock) */
    final public static int fNotFallThrough = 512;

    /** this instruction starts a block (synthetic) */
    final public static int fStartsBlock = 1024;

    public int flags;
    
    /** Next legal instruction, not the next in memory */
    private HighLevelInstruction next;
    /** Previous instruction, whose next points to this */
    private HighLevelInstruction prev;

    /** The block that owns the instruction */
    private Block block;

	private short wp;

	private RawInstruction inst;
    
    public HighLevelInstruction(int wp, RawInstruction inst) {
    	this.inst = inst;
        this.setWp((short) wp);
    	setFlags();
	}

	private void setFlags() {
        if (inst.info.jump != 0) {
        	flags |= fEndsBlock;
            if (inst.getInst() == Inst9900.Ibl || inst.getInst() == Inst9900.Iblwp) {
				flags |= fIsCall+fIsBranch;
			} else if (inst.getInst() == Inst9900.Irtwp) {
				flags |= fIsReturn+fIsBranch+fNotFallThrough; /* B *R11 detected later */
			} else if (inst.info.jump == InstInfo.INST_JUMP_COND) {
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
        		&& inst.getOp2() instanceof MachineOperand
        		&& ((BaseMachineOperand) inst.getOp2()).val <= 8) {
        	flags |= fByteOp;
        } else if (inst.getInst() == Inst9900.Ilimi) {
        	if (((BaseMachineOperand) inst.getOp1()).immed != 0) {
        		// likely block end
        		flags |= fEndsBlock;
        	}
        }
    }
    
     @Override
    public String toString() {
        return HexUtils.toHex4(inst.pc) + " " + inst.toString();
    }
    
    public String format(boolean showOpcodeAddr, boolean showComments) {
        String str = super.toString();
        if (showOpcodeAddr) {
			str = ">" + HexUtils.toHex4(inst.pc) + " " + str;
		}
        if (showComments) {
            String flagStr = getFlagString();
            if (flagStr.length() > 0) {
				str += " ; " + flagStr;
			}
        }
        return str;
    }

    private String getFlagString() {
        if (flags == 0) {
			return "";
		}
        StringBuffer buffer = new StringBuffer();
        if ((flags & fIsBranch) != 0) {
			buffer.append("fIsBranch ");
		}
        if ((flags & fIsCondBranch) != 0) {
			buffer.append("fIsCondBranch ");
		}
        if ((flags & fIsCall) != 0) {
			buffer.append("fIsCall ");
		}
        if ((flags & fIsReturn) != 0) {
			buffer.append("fIsReturn ");
		}
        if ((flags & fCheckLater) != 0) {
			buffer.append("fCheckLater ");
		}
        if ((flags & fEndsBlock) != 0) {
			buffer.append("fEndsBlock ");
        }
        if ((flags & fNotFallThrough) != 0) {
			buffer.append("fNotFallThrough ");
        }
        return buffer.toString();
    }

    public boolean isBranch() {
        return (flags & fIsBranch) != 0;
    }
    
    public boolean isCall() {
        return (flags & fIsCall) != 0;
    }

    public boolean isReturn() {
        return (flags & fIsReturn) != 0;
    }

	public void setNext(HighLevelInstruction next) {
		this.next = next;
		if (next != null) {
			next.prev = this;
		}
	}

	public HighLevelInstruction getNext() {
		return next;
	}
	
	public HighLevelInstruction getPrev() {
		return prev;
	}
    
	public void setBlock(Block block) {
		this.block = block;
	}

	public Block getBlock() {
		return block;
	}

	public void convertToData() {
		flags = 0;
		inst.setInst(InstTableCommon.Idata);
		inst.size = 2;
		inst.setName("DATA");
		inst.setOp1(new MachineOperand9900(MachineOperand9900.OP_IMMED));
		((BaseMachineOperand)inst.getOp1()).immed = inst.opcode;
		inst.setOp2(new MachineOperand9900(MachineOperand.OP_NONE));
	}

	public Collection<Block> getReferencedBlocks() {
		Set<Block> blocks = new TreeSet<Block>();
		if (inst.getOp1() instanceof LabelOperand) {
			blocks.add(((LabelOperand)inst.getOp1()).label.getBlock());
		} else if (inst.getOp1() instanceof RoutineOperand) {
			blocks.add(((RoutineOperand)inst.getOp1()).routine.getMainLabel().getBlock());
		} else if (inst.getOp1() instanceof MachineOperand) {
			
		} else if (inst.getOp1() instanceof DataWordListOperand) {
			
		} else {
			Check.checkState(false);
		}
		return blocks;
	}

	public void setWp(short wp) {
		this.wp = wp;
	}

	public short getWp() {
		return wp;
	}

	/**
	 * @return
	 */
	public RawInstruction getInst() {
		return inst;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(HighLevelInstruction o) {
		return inst.compareTo(o.inst);
	}

}
