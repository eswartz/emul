/*
  LLRegIndOperand.java

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

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public class LLRegIndOperand extends LLOperand implements IOperand {

	int register;
	
	public LLRegIndOperand(int reg) {
		super(null);
		setRegister(reg);
	}
	public LLRegIndOperand(AssemblerOperand original, int reg) {
		super(original);
		setRegister(reg);
	}

	@Override
	public String toString() {
		return "*R" + register;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		LLRegIndOperand other = (LLRegIndOperand) obj;
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

	@Override
	public boolean hasImmediate() {
		return false;
	}
	
	@Override
	public int getSize() {
		return 0;
	}
	
	@Override
	public int getImmediate() {
		return 0;
	}
	
	@Override
	public IMachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createRegIndOperand(this);
	}
}
