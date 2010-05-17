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
public class BinaryOperand extends BaseOperand {

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
		LLOperand leftRes = left.resolve(assembler, inst);
		LLOperand rightRes = right.resolve(assembler, inst);
		if (leftRes instanceof LLForwardOperand || rightRes instanceof LLForwardOperand) {
			return new LLForwardOperand(this, leftRes.getSize() | rightRes.getSize());
		}
		if (leftRes instanceof LLImmedOperand) {
			if (rightRes instanceof LLImmedOperand) {
				int immed = doOp(inst, leftRes.getImmediate(), rightRes.getImmediate());
				return new LLImmedOperand(this, immed);
			} else {
				throw new ResolveException(rightRes, "Expected immediate");
			}
		}
		else
			throw new ResolveException(leftRes, "Expected immediate");
		
	}

	/**
	 * @param inst  
	 */
	private int doOp(IInstruction inst, int l, int r) throws ResolveException {
		switch (type) {
		case '+': return (l + r);
		case '-': return (l - r);
		case '*': return (l * r);
		case '/': if (r != 0) return (l / r); 
			else throw new ResolveException(this, "Division by zero");
		}
		throw new IllegalStateException("unknown operation: " + (char)type);
	}

	public int getKind() {
		return type;
	}

	public AssemblerOperand getLeft() {
		return left;
	}

	public AssemblerOperand getRight() {
		return right;
	}
	
	
}
