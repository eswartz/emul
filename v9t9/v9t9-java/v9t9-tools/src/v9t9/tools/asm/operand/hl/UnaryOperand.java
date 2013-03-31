/*
  UnaryOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.operand.hl;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.IAssembler;
import v9t9.tools.asm.operand.ll.LLForwardOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class UnaryOperand extends BaseOperand {

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
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return op.isConst();
	}
	
	public LLOperand resolve(IAssembler assembler, IInstruction inst)
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
	


	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.operand.hl.AssemblerOperand, v9t9.tools.asm.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand newOp = op.replaceOperand(src, dst);
		if (newOp != op) {
			return new UnaryOperand(type, newOp);
		}
		return this;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[] { op };
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return new BinaryOperand('+', this, new NumberOperand(i));
	}
}
