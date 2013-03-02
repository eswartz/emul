/*
  LLRegDecOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.operand.ll;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.ResolveException;

/**
 * @author Ed
 *
 */
public class LLRegDecOperand extends LLNonImmediateOperand implements IOperand {

	int register;
	
	public LLRegDecOperand(int reg) {
		super(null);
		setRegister(reg);
	}

	@Override
	public String toString() {
		return "*R" + register + "-";
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LLRegDecOperand other = (LLRegDecOperand) obj;
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

	public int getRegister() {
		return register;
	}


	public void setRegister(int number) {
		this.register = number;
	}

	@Override
	public IMachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createRegDecOperand(this);
	}
}
