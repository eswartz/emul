/**
 * 
 */
package v9t9.tools.asm.directive;

import java.util.List;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;
import v9t9.tools.asm.Symbol;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class EquDirective extends Directive {

	private AssemblerOperand op;

	public EquDirective(List<AssemblerOperand> ops) {
		this.op = ops.get(0);
	}
	
	@Override
	public String toString() {
		return "EQU " + op;
	}

	public IInstruction[] resolve(Assembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		// establish initial PC, for "equ $"
		setPc(assembler.getPc());
		
		LLOperand lop = op.resolve(assembler, this); 
		if (!(lop instanceof LLImmedOperand))
			throw new ResolveException(op, "Expected number");
		
		op = lop;
		
		// reset, in case it changed
		setPc(lop.getImmediate());
		
		if (previous != null && previous instanceof LabelDirective) {
			LabelDirective label = (LabelDirective) previous;
			Symbol symbol = label.getSymbol();
			label.setPc(lop.getImmediate());
			symbol.setDefined(true);
		}
		return new IInstruction[] { this };
	}
}
