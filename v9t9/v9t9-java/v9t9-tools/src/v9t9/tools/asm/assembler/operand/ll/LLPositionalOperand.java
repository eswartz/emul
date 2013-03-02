/*
  LLPositionalOperand.java

  (c) 2010-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.tools.asm.assembler.operand.ll;

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