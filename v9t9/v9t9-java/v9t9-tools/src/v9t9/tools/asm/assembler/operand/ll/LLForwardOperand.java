/*
  LLForwardOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * A reference to a forward-declared operand: the original operand contains
 * the symbol which not resolved.
 * @author Ed
 *
 */
public class LLForwardOperand extends LLOperand {

	private int size;

	public LLForwardOperand(AssemblerOperand original, int size) {
		super(original);
		if (original == null)
			throw new IllegalArgumentException();
		this.size = size;
	}

	@Override
	public String toString() {
		return "{" + getOriginal() + "}";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLForwardOperand other = (LLForwardOperand) obj;
		if (size != other.size)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#createMachineOperand()
	 */
	@Override
	public IMachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException {
		throw new ResolveException(this, "Unresolved forward reference: " + getOriginal());
	}
	
	@Override
	public LLOperand resolve(IAssembler assembler, IInstruction inst)
			throws ResolveException {
		return getOriginal().resolve(assembler, inst);
	}

	@Override
	public boolean isMemory() {
		return false;
	}
	@Override
	public boolean isRegister() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}

	
	@Override
	public int getImmediate() {
		return 0;
	}

	@Override
	public boolean hasImmediate() {
		return false;
	}
	
	@Override
	public int getSize() {
		return size;
	}

	
}
