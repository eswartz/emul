/*
  ContextSwitchRoutine.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.ti99.asm;

import v9t9.common.asm.Block;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.Routine;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;

public class ContextSwitchRoutine extends Routine {
    private short wp;
    public ContextSwitchRoutine(int wp) {
        super();
        this.setWp((short) wp);
    }

    @Override
    public boolean isReturn(IHighLevelInstruction inst) {
        return inst.getInst().getInst() == Inst9900.Irtwp;
    }

    @Override
    public void examineEntryCode() {
        // TODO: look for stuff like LWPI
    	
    	dataWords = 0;
    	
    	int entryDataBytes = 0;
    	
    	for (Block block : getEntries()) {
    		IHighLevelInstruction inst = block.getFirst();
    		entryDataBytes = 0;
	        while (inst != null && !inst.isCall()) {
	        	// look for uses of parameter words; ignore any branching
	        	if (inst.getInst().getOp1() instanceof IMachineOperand) {
	                MachineOperand9900 mop1 = (MachineOperand9900) inst.getInst().getOp1();
	                if (mop1.isMemory() && mop1.type == MachineOperand9900.OP_INC 
	                        && mop1.val == 14) {
	                	if ((inst.getFlags() & HighLevelInstruction.fByteOp) != 0) {
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

	public void setWp(short wp) {
		this.wp = wp;
	}

	public short getWp() {
		return wp;
	}

}
