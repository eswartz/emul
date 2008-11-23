/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.llinst;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.utils.Check;
import v9t9.utils.Utils;

/**
 * Instruction augmented with decompiler info
 * @author ejs
 */
public class HighLevelInstruction extends Instruction {
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
    
    public HighLevelInstruction(int wp, Instruction inst) {
    	super(inst);
        this.setWp((short) wp);
    	setFlags();
	}

	private void setFlags() {
        if (jump != 0) {
        	flags |= fEndsBlock;
            if (inst == InstructionTable.Ibl || inst == InstructionTable.Iblwp) {
				flags |= fIsCall+fIsBranch;
			} else if (inst == InstructionTable.Irtwp) {
				flags |= fIsReturn+fIsBranch+fNotFallThrough; /* B *R11 detected later */
			} else if (jump == INST_JUMP_COND) {
				flags |= fIsCondBranch+fIsBranch;
			} else {
				//if (inst == Ib && op1 instanceof MachineOperand 
                 //       && ((MachineOperand)op1).type == MachineOperand.OP_ADDR) {
			//		flags |= fIsBranch+fCheckLater+fNotFallThrough;
				flags |= fIsBranch+fNotFallThrough;
			}
        }
        if (inst == InstructionTable.Imovb || inst == InstructionTable.Isocb || inst == InstructionTable.Iab || inst == InstructionTable.Isb
        		|| inst == InstructionTable.Icb || inst == InstructionTable.Iszcb) {
        	flags |= fByteOp;
        } else if ((inst == InstructionTable.Istcr || inst == InstructionTable.Ildcr)
        		&& op2 instanceof MachineOperand
        		&& ((MachineOperand) op2).val <= 8) {
        	flags |= fByteOp;
        } else if (inst == InstructionTable.Ilimi) {
        	if (((MachineOperand) op1).immed != 0) {
        		// likely block end
        		flags |= fEndsBlock;
        	}
        }
    }
    
     @Override
    public String toString() {
        return Utils.toHex4(pc) + " " + super.toString();
    }
    
    public String format(boolean showOpcodeAddr, boolean showComments) {
        String str = super.toString();
        if (showOpcodeAddr) {
			str = ">" + Utils.toHex4(pc) + " " + str;
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
		inst = InstructionTable.Idata;
		size = 2;
		setName("DATA");
		op1 = new MachineOperand(MachineOperand.OP_IMMED);
		((MachineOperand)op1).immed = opcode;
		op2 = new MachineOperand(MachineOperand.OP_NONE);
	}

	public Collection<Block> getReferencedBlocks() {
		Set<Block> blocks = new TreeSet<Block>();
		if (op1 instanceof LabelOperand) {
			blocks.add(((LabelOperand)op1).label.getBlock());
		} else if (op1 instanceof RoutineOperand) {
			blocks.add(((RoutineOperand)op1).routine.getMainLabel().getBlock());
		} else if (op1 instanceof MachineOperand) {
			
		} else if (op1 instanceof DataWordListOperand) {
			
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

}
