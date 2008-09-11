/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.decomp;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.MachineOperand;

public class LinkedRoutine extends Routine {

    public int returnReg;

    public LinkedRoutine() {
        super();
        returnReg = 11;
    }
    
    @Override
    public boolean isReturn(LLInstruction inst) {
        return inst.inst == Instruction.Ib
        	&& inst.op1 instanceof MachineOperand
            && ((MachineOperand)inst.op1).type == MachineOperand.OP_IND
            && ((MachineOperand)inst.op1).val == returnReg;
    }
    
    @Override
    public void examineEntryCode() {
    	dataWords = 0;
    	
    	int entryDataBytes = 0;
    	
    	returnReg = 11;
    	for (Block block : getEntries()) {
    		LLInstruction inst = block.getFirst();
    		entryDataBytes = 0;
	        while (inst != null && !inst.isCall()) {
	        	if (returnReg == 11) {
		            if (inst.inst == Instruction.Imov
		            		&& inst.op1 instanceof MachineOperand
		                    && ((MachineOperand)inst.op1).isRegister(11)
		                    && ((MachineOperand)inst.op2).isRegister()) {
		            	int reg = ((MachineOperand)inst.op2).val;
		            	if (returnReg != reg && returnReg != 11)
		            		System.out.println("??? inconsistent register saving from " + returnReg + " to " + reg);
		                returnReg = reg;
		            }
	        	}
	        	
	        	// look for uses of parameter words; ignore any branching
	        	if (inst.op1 instanceof MachineOperand) {
	                MachineOperand mop1 = (MachineOperand) inst.op1;
	                if (mop1.isMemory() && mop1.type == MachineOperand.OP_INC 
	                        && mop1.val == returnReg) {
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
