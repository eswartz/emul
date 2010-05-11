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
public class UnaryOperand implements AssemblerOperand {

	private final int type;
	private final AssemblerOperand op;

	public UnaryOperand(int type, AssemblerOperand op) {
		this.type = type;
		this.op = op;
	}

	@Override
	public String toString() {
		return ("" + (char)type) + op;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + type;
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
		UnaryOperand other = (UnaryOperand) obj;
		if (op == null) {
			if (other.op != null) {
				return false;
			}
		} else if (!op.equals(other.op)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}

	public LLOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		LLOperand resOp = op.resolve(assembler, inst);
		if (resOp instanceof LLForwardOperand)
			return new LLForwardOperand(this, resOp.getSize());
		
		if (!(resOp instanceof LLImmedOperand))
			throw new ResolveException(op, "Expected an immediate");
		if (type == '-') {
			resOp = new LLImmedOperand(-resOp.getImmediate());
		} else {
			throw new IllegalStateException("Unhandled operator: " + (char)type);
		}
		return resOp;
	}
}
