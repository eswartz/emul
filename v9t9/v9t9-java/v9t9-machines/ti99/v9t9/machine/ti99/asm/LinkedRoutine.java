/*
  LinkedRoutine.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.asm;

import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.Block;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.Routine;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;

public class LinkedRoutine extends Routine {

    public int returnReg;

    public LinkedRoutine() {
        super();
        returnReg = 11;
    }
    
    @Override
    public boolean isReturn(IHighLevelInstruction inst) {
        return inst.getInst().getInst() == Inst9900.Ib
        	&& inst.getInst().getOp1() instanceof IMachineOperand
            && ((BaseMachineOperand)inst.getInst().getOp1()).type == MachineOperand9900.OP_IND
            && ((BaseMachineOperand)inst.getInst().getOp1()).val == returnReg;
    }
    
    @Override
    public void examineEntryCode() {
    	dataWords = 0;
    	
    	int entryDataBytes = 0;
    	
    	returnReg = 11;
    	for (Block block : getEntries()) {
    		IHighLevelInstruction inst = block.getFirst();
    		entryDataBytes = 0;
	        while (inst != null && !inst.isCall()) {
	        	if (returnReg == 11) {
		            if (inst.getInst().getInst() == Inst9900.Imov
		            		&& inst.getInst().getOp1() instanceof IMachineOperand
		                    && ((IMachineOperand)inst.getInst().getOp1()).isRegister(11)
		                    && ((IMachineOperand)inst.getInst().getOp2()).isRegister()) {
		            	int reg = ((BaseMachineOperand)inst.getInst().getOp2()).val;
		            	if (returnReg != reg && returnReg != 11)
		            		System.out.println("??? inconsistent register saving from " + returnReg + " to " + reg);
		                returnReg = reg;
		            }
	        	}
	        	
	        	// look for uses of parameter words; ignore any branching
	        	if (inst.getInst().getOp1() instanceof IMachineOperand) {
	                MachineOperand9900 mop1 = (MachineOperand9900) inst.getInst().getOp1();
	                if (mop1.isMemory() && mop1.type == MachineOperand9900.OP_INC 
	                        && mop1.val == returnReg) {
	                	if ((inst.getFlags() & IHighLevelInstruction.fByteOp) != 0) {
	                		entryDataBytes++;
	                	} else {
	                		entryDataBytes += 2;
	                	}
	                }
	        	}
	        	
	            if (inst == block.getLast())
	            	break;
	            inst = inst.getLogicalNext();
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
