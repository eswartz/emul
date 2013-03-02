/*
  LLAddrOperand.java

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
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public class LLAddrOperand extends LLOperand implements IOperand {

	int address;
	
	public LLAddrOperand(AssemblerOperand original, int value) {
		super(original);
		setAddress(value);
	}
	
	@Override
	public String toString() {
		return "@>" + HexUtils.toHex4(address);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
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
		LLAddrOperand other = (LLAddrOperand) obj;
		if (address != other.address)
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
	
	public int getAddress() {
		return address;
	}



	public void setAddress(int value) {
		this.address = value;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#getImmediate()
	 */
	@Override
	public int getImmediate() {
		return address;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#getSize()
	 */
	@Override
	public int getSize() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#hasImmediate()
	 */
	@Override
	public boolean hasImmediate() {
		return true;
	}

	@Override
	public IMachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createAddressOperand(this);
	}

}
