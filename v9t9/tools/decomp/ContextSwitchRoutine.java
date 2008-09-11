/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.decomp;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.MachineOperand;

public class ContextSwitchRoutine extends Routine {
    short wp;
    public ContextSwitchRoutine(int wp) {
        super();
        this.wp = (short) wp;
    }

    @Override
    public boolean isReturn(LLInstruction inst) {
        return inst.inst == Instruction.Irtwp;
    }

    @Override
    public void examineEntryCode() {
        // TODO: look for stuff like LWPI
    	
    	dataWords = 0;
    	
    	int entryDataBytes = 0;
    	
    	for (Block block : getEntries()) {
    		LLInstruction inst = block.getFirst();
    		entryDataBytes = 0;
	        while (inst != null && !inst.isCall()) {
	        	// look for uses of parameter words; ignore any branching
	        	if (inst.op1 instanceof MachineOperand) {
	                MachineOperand mop1 = (MachineOperand) inst.op1;
	                if (mop1.isMemory() && mop1.type == MachineOperand.OP_INC 
	                        && mop1.val == 14) {
	                	if ((inst.flags & LLInstruction.fByteOp) != 0) {
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

}
