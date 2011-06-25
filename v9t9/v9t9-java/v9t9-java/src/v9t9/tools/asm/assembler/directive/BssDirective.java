/**
 * 
 */
package v9t9.tools.asm.assembler.directive;

import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class BssDirective extends Directive {

	private AssemblerOperand op;

	public BssDirective(List<AssemblerOperand> ops) {
		this.op = ops.get(0);
	}
	
	@Override
	public String toString() {
		return "BSS " + op;
	}

	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		LLOperand lop = op.resolve(assembler, this); 
		if (lop instanceof LLForwardOperand)
			throw new ResolveException(op, "Cannot allocate size for forward-declared symbol");
		if (!(lop instanceof LLImmedOperand))
			throw new ResolveException(op, "Expected number");
		op = lop;
		setPc(assembler.getPc());
		assembler.setPc((assembler.getPc() + lop.getImmediate()));
		return new IInstruction[] { this };
	}
	
	
}
