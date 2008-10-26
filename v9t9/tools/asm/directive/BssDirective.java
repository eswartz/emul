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
public class BssDirective extends AssemblerDirective {

	private Operand op;

	public BssDirective(List<Operand> ops) {
		this.op = ops.get(0);
	}
	
	@Override
	public String toString() {
		return "BSS " + op;
	}

	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		MachineOperand mop = op.resolve(assembler, this); 
		if (mop.type != MachineOperand.OP_IMMED)
			throw new ResolveException(op, "Expected number");
		if (mop.symbol != null)
			throw new ResolveException(op, "Cannot allocate size for forward-declared symbol");
		op = mop;
		setPc(assembler.getPc());
		assembler.setPc((assembler.getPc() + mop.immed));
		return new IInstruction[] { this };
	}
	
}
