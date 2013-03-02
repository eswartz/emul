/*
  NumberOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.operand.hl;


import ejs.base.utils.HexUtils;
import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author ejs
 *
 */
public class NumberOperand extends BaseOperand {

	private int i;

	public NumberOperand(int i) {
		this.i = i;
	}
	
	public void negate() {
		i = -i;
	}
	@Override
	public String toString() {
		return ">" + HexUtils.toHex4(i);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
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
		NumberOperand other = (NumberOperand) obj;
		if (i != other.i) {
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
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return true;
	}
	

	public LLOperand resolve(IAssembler assembler, IInstruction inst) throws ResolveException {
		LLImmedOperand op = new LLImmedOperand(i);
		return op;
	}

	public int getValue() {
		return i;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[0];
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return new NumberOperand(this.i + i);
	}
}
