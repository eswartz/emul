/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.asm.decomp;

import v9t9.engine.cpu.Inst9900;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperand9900;

public class ContextSwitchRoutine extends Routine {
    private short wp;
    public ContextSwitchRoutine(int wp) {
        super();
        this.setWp((short) wp);
    }

    @Override
    public boolean isReturn(HighLevelInstruction inst) {
        return inst.inst == Inst9900.Irtwp;
    }

    @Override
    public void examineEntryCode() {
        // TODO: look for stuff like LWPI
    	
    	dataWords = 0;
    	
    	int entryDataBytes = 0;
    	
    	for (Block block : getEntries()) {
    		HighLevelInstruction inst = block.getFirst();
    		entryDataBytes = 0;
	        while (inst != null && !inst.isCall()) {
	        	// look for uses of parameter words; ignore any branching
	        	if (inst.getOp1() instanceof MachineOperand) {
	                MachineOperand9900 mop1 = (MachineOperand9900) inst.getOp1();
	                if (mop1.isMemory() && mop1.type == InstTable9900.OP_INC 
	                        && mop1.val == 14) {
	                	if ((inst.flags & HighLevelInstruction.fByteOp) != 0) {
	                		entryDataBytes++;
	                	} else {
	                		entryDataBytes += 2;
	                	}
	                }
	        	}
	        	
	            if (inst == block.getLast())
	            	break;
	            inst = inst.getNext();
	        }
	        
	        if (entryDataBytes / 2 > dataWords) {
	        	dataWords = entryDataBytes / 2;
	        }
    	}
    	
        if (dataWords > 0) {
			System.out.println("call site " + this + " seems to use " + dataWords + " data words");
		}
        
    }

	public void setWp(short wp) {
		this.wp = wp;
	}

	public short getWp() {
		return wp;
	}

}
