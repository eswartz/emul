/*
  LLRegOffsOperand.java

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


import ejs.base.utils.HexUtils;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;

/**
 * @author Ed
 *
 */
public class LLRegOffsOperand extends LLOperand implements IOperand {

	int register;
	int offset;
	
	public LLRegOffsOperand(RegOffsOperand original, int reg, int offset) {
		super(original);
		setRegister(reg);
		setOffset(offset);
	}

	@Override
	public String toString() {
		return "@>" + HexUtils.toHex4(offset) + "(R" + register + ")";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + offset;
		result = prime * result + register;
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
		LLRegOffsOperand other = (LLRegOffsOperand) obj;
		if (offset != other.offset)
			return false;
		if (register != other.register)
			return false;
		return true;
	}
	
	@Override
	public boolean isMemory() {
		return true;
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

	
	public int getRegister() {
		return register;
	}


	public void setRegister(int number) {
		this.register = number;
	}


	public int getOffset() {
		return offset;
	}


	public void setOffset(int offset) {
		this.offset = offset;
	}


	@Override
	public boolean hasImmediate() {
		return true;
	}
	
	@Override
	public int getSize() {
		return offset != 0 ? 2 : 0;
	}
	
	@Override
	public int getImmediate() {
		return offset;
	}
	
	@Override
	public IMachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createRegOffsOperand(this);
	}
}
