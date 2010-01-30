/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.llinst;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

public class LabelOperand implements Operand {

    public Label label;

    public LabelOperand(Label label) {
    	org.ejs.emul.core.utils.Check.checkArg(label);
        this.label = label;
    }
    
    @Override
    public String toString() {
        return label.toString();
    }

    public MachineOperand resolve(Assembler assembler, IInstruction inst) throws ResolveException {
		throw new ResolveException(this, "Unresolvable operand");
	}

}
