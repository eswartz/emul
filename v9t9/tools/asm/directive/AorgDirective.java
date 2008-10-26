/**
 * 
 */
package v9t9.tools.asm.directive;

import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

/**
 * @author Ed
 *
 */
public class AorgDirective extends AssemblerDirective {

	private Operand op;

	public AorgDirective(List<Operand> ops) {
		this.op = ops.get(0);
	}
	
	@Override
	public String toString() {
		return "AORG " + op;
	}

	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		MachineOperand mop = op.resolve(assembler, this); 
		if (mop.type != MachineOperand.OP_IMMED)
			throw new ResolveException(op, "Expected number");
		op = mop;
		assembler.setPc(mop.immed);
		setPc(mop.immed);
		return new IInstruction[] { this };
	}
}
