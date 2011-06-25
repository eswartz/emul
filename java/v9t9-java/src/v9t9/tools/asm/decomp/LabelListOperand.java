/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 26, 2006
 *
 */
package v9t9.tools.asm.decomp;

import java.util.Iterator;
import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.common.LabelOperand;

public class LabelListOperand implements Operand {
    public List<LabelOperand> operands;
	private MachineOperand mop;

    public LabelListOperand(MachineOperand mop, List<LabelOperand> operands) {
    	this.mop = mop;
        this.operands = operands;
    }
    
    public Iterator<LabelOperand> iterator() {
        return operands.iterator();
    }
    
    @Override
    public String toString() {
        return mop + " : " + operands.toString();
    }
    
    public MachineOperand getMachineOperand() {
    	return mop;
    }
    
    /**
	 * @param assembler  
     * @param inst 
	 */
    public MachineOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException {
		throw new ResolveException(this, "Unresolvable operand");
	}
}
