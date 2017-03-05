/*
  LLPositionalOperand.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.operand.ll;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.ResolveException;

final class LLPositionalOperand extends LLOperand {
	private final int position;
	/**
	 * @param original
	 */
	public LLPositionalOperand(int position) {
		super(null);
		this.position = position;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + position;
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLPositionalOperand other = (LLPositionalOperand) obj;
		if (position != other.position)
			return false;
		return true;
	}


	@Override
	public IMachineOperand createMachineOperand(
			IAsmMachineOperandFactory opFactory) throws ResolveException {
		throw new ResolveException(this, "should be replaced");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "<positional operand " + position + ">";
	}
	

	@Override
	public int getImmediate() {
		return 0;
	}

	@Override
	public int getSize() {
		return 0;
	}
	@Override
	public boolean hasImmediate() {
		return false;
	}
	@Override
	public boolean isConst() {
		return false;
	}
	@Override
	public boolean isMemory() {
		return false;
	}
	@Override
	public boolean isRegister() {
		return false;
	}
}