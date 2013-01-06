/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.machine.ti99.asm;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import ejs.base.utils.Check;
import ejs.base.utils.HexUtils;


import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.Block;
import v9t9.common.asm.DataWordListOperand;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.LabelOperand;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.RoutineOperand;
import v9t9.machine.ti99.cpu.MachineOperand9900;

/**
 * Instruction augmented with decompiler info
 * @author ejs
 */
public class HighLevelInstruction  implements Comparable<IHighLevelInstruction>, IHighLevelInstruction {

    public int flags;
    
    /** Next physical instruction */
    private IHighLevelInstruction next;
    /** Previous instruction, whose next points to this */
    private IHighLevelInstruction prev;

    /** The block that owns the instruction */
    private Block block;

	private short wp;

	private RawInstruction inst;
    
    public HighLevelInstruction(int wp, RawInstruction inst, int flags) {
    	this.inst = inst;
        this.setWp((short) wp);
        this.flags = flags;
	}

    
     @Override
    public String toString() {
        return HexUtils.toHex4(inst.pc) + " " + inst.toString();
    }
    
    public String format(boolean showOpcodeAddr, boolean showComments) {
        String str = inst.toString();
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

	public void setPhysicalNext(IHighLevelInstruction next) {
		if (next == this.next)
			return;
		if (this.next != null)
			((HighLevelInstruction) this.next).setPhysicalPrev(null);
		this.next = next;
		if (next != null) {
			((HighLevelInstruction) next).setPhysicalPrev(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.decomp.IHighLevelInstruction#setPrev(v9t9.tools.asm.decomp.IHighLevelInstruction)
	 */
	public void setPhysicalPrev(IHighLevelInstruction prev) {
		this.prev = prev;
	}

	public IHighLevelInstruction getPhysicalNext() {
		return next;
	}
	
	public IHighLevelInstruction getPhysicalPrev() {
		return prev;
	}
    
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IHighLevelInstruction#getLogicalNext()
	 */
	@Override
	public IHighLevelInstruction getLogicalNext() {
		int target = inst.getPc() + inst.getSize();
		IHighLevelInstruction inst = next;
		while (inst != null) {
			if (inst.getInst().getPc() == target)
				return inst;
			inst = inst.getPhysicalNext();
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IHighLevelInstruction#getLogicalPrev()
	 */
	@Override
	public IHighLevelInstruction getLogicalPrev() {
		int target = inst.getPc();
		IHighLevelInstruction inst = prev;
		IHighLevelInstruction cand = null;
		while (inst != null) {
			if (inst.getInst().getPc() + inst.getInst().getSize() < target)
				break;
			if (inst.getInst().getPc() + inst.getInst().getSize() == target)
				cand = inst;
			inst = inst.getPhysicalPrev();
		}
		return cand;
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
		inst.setSize(2);
		inst.setName("DATA");
		inst.setOp1(new MachineOperand9900(MachineOperand9900.OP_IMMED));
		((BaseMachineOperand)inst.getOp1()).immed = (short) inst.opcode;
		inst.setOp2(new MachineOperand9900(IMachineOperand.OP_NONE));
	}

	public Collection<Block> getReferencedBlocks() {
		Set<Block> blocks = new TreeSet<Block>();
		if (inst.getOp1() instanceof LabelOperand) {
			blocks.add(((LabelOperand)inst.getOp1()).label.getBlock());
		} else if (inst.getOp1() instanceof RoutineOperand) {
			blocks.add(((RoutineOperand)inst.getOp1()).routine.getMainLabel().getBlock());
		} else if (inst.getOp1() instanceof IMachineOperand) {
			
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
	public int compareTo(IHighLevelInstruction o) {
		return inst.compareTo(o.getInst());
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.decomp.IHighLevelInstruction#getFlags()
	 */
	@Override
	public int getFlags() {
		return flags;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.decomp.IHighLevelInstruction#setFlags(int)
	 */
	@Override
	public void setFlags(int i) {
		this.flags = i;
	}
}
