/**
 * 
 */
package v9t9.tools.asm.operand;

import v9t9.engine.cpu.AssemblerOperand;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.MachineOperand;
import v9t9.tools.asm.Assembler;
import v9t9.tools.asm.ResolveException;

/**
 * @author ejs
 *
 */
public class BinaryOperand implements AssemblerOperand {

	private final int type;
	private final AssemblerOperand left;
	private final AssemblerOperand right;

	public BinaryOperand(int type, AssemblerOperand left, AssemblerOperand right) {
		this.type = type;
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return left + (" " + (char)type + " " ) + right;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		BinaryOperand other = (BinaryOperand) obj;
		if (left == null) {
			if (other.left != null) {
				return false;
			}
		} else if (!left.equals(other.left)) {
			return false;
		}
		if (right == null) {
			if (other.right != null) {
				return false;
			}
		} else if (!right.equals(other.right)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}
	
	public MachineOperand resolve(Assembler assembler, Instruction inst)
			throws ResolveException {
		MachineOperand leftRes = left.resolve(assembler, inst);
		MachineOperand rightRes = right.resolve(assembler, inst);
		if (leftRes.type == MachineOperand.OP_IMMED) {
			if (rightRes.type == MachineOperand.OP_IMMED) {
				leftRes.immed = doOp(inst, leftRes.immed, rightRes.immed);
				return leftRes;
			} else {
				throw new ResolveException(inst, rightRes, "Expected immediate");
			}
		}
		else
			throw new ResolveException(inst, leftRes, "Expected immediate");
		
	}

	private short doOp(Instruction inst, short l, short r) throws ResolveException {
		switch (type) {
		case '+': return (short) (l + r);
		case '-': return (short) (l - r);
		case '*': return (short) (l * r);
		case '/': if (r != 0) return (short) (l / r); 
			else throw new ResolveException(inst, this, "Division by zero");
		}
		throw new IllegalStateException("unknown operation: " + (char)type);
	}
}
