/**
 * 
 */
package v9t9.tools.asm.operand;

import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

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
	

	public MachineOperand resolve(Assembler assembler, IInstruction inst)
			throws ResolveException {
		MachineOperand resOp = op.resolve(assembler, inst);
		if (resOp.type != MachineOperand.OP_IMMED)
			throw new ResolveException(op, "Expected an immediate");
		if (resOp.symbol != null)
			throw new ResolveException(op, "Cannot apply operand to forward symbol");
		if (type == '-') {
			resOp.immed = (short) -resOp.immed;
		} else {
			throw new IllegalStateException("Unhandled operator: " + (char)type);
		}
		return resOp;
	}
}
