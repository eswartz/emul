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
public class EquDirective extends AssemblerDirective {

	private Operand op;

	public EquDirective(List<Operand> ops) {
		this.op = ops.get(0);
	}
	
	@Override
	public String toString() {
		return "EQU " + op;
	}

	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		// establish initial PC, for "equ $"
		setPc(assembler.getPc());
		
		MachineOperand mop = op.resolve(assembler, this); 
		if (mop.type != MachineOperand.OP_IMMED)
			throw new ResolveException(op, "Expected number");
		
		op = mop;
		
		// reset, in case it changed
		setPc(mop.immed);
		
		if (previous != null && previous instanceof LabelDirective) {
			((LabelDirective) previous).setPc(mop.immed);
		}
		return new IInstruction[] { this };
	}
}
