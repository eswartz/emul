/**
 * 
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.engine.cpu.IInstruction;
import v9t9.tools.asm.assembler.Assembler;
import v9t9.tools.asm.assembler.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class ImmediateOperand implements AssemblerOperand {

	protected final AssemblerOperand immed;

	public ImmediateOperand(AssemblerOperand immed) {
		this.immed = immed;
	}

	@Override
	public String toString() {
		return immed.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((immed == null) ? 0 : immed.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ImmediateOperand other = (ImmediateOperand) obj;
		if (immed == null) {
			if (other.immed != null) {
				return false;
			}
		} else if (!immed.equals(other.immed)) {
			return false;
		}
		return true;
	}
	
	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		LLOperand op = immed.resolve(assembler, inst);
		if (op instanceof LLForwardOperand)
			return new LLForwardOperand(this, 2);
		if (!(op instanceof LLImmedOperand)) {
			throw new ResolveException(op, "Expected an immediate");
		}
		return op;
	}
	
}
