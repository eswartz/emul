/*
  LLNonImmediateOperand.java

  (c) 2008-2011 Edward Swartz

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

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public abstract class LLNonImmediateOperand extends LLOperand {
	
	/**
	 * @param original
	 */
	public LLNonImmediateOperand(AssemblerOperand original) {
		super(original);
	}

	
	@Override
	public int hashCode() {
		int result = 1;
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
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#getImmediate()
	 */
	@Override
	public final int getImmediate() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#getSize()
	 */
	@Override
	public final int getSize() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#hasImmediate()
	 */
	@Override
	public final boolean hasImmediate() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}

}
