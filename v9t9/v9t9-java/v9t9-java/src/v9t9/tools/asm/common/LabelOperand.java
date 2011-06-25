/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.asm.common;

import org.ejs.coffee.core.utils.Check;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.decomp.Label;

public class LabelOperand implements Operand {

    public Label label;

    public LabelOperand(Label label) {
    	Check.checkArg(label);
        this.label = label;
    }
    
    @Override
    public String toString() {
        return label.toString();
    }
    /**
	 * @param assembler  
	 * @param inst 
	 */
    public MachineOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException {
		throw new ResolveException(this, "Unresolvable operand");
	}

}
