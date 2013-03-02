/*
  LLOffsetOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.operand.ll;


import ejs.base.utils.HexUtils;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.ResolveException;

/**
 * An offset from R12
 * @author Ed
 *
 */
public class LLOffsetOperand extends LLNonImmediateOperand {

	int offset;
	public LLOffsetOperand(int value) {
		super(null);
		setOffset(value);
	}
	
	@Override
	public String toString() {
		return ">" + HexUtils.toHex4(offset);
	}


	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + offset;
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
		LLOffsetOperand other = (LLOffsetOperand) obj;
		if (offset != other.offset)
			return false;
		return true;
	}


	@Override
	public boolean isMemory() {
		return false;
	}
	@Override
	public boolean isRegister() {
		return false;
	}

	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int count) {
		this.offset = count;
	}

	@Override
	public IMachineOperand createMachineOperand(IAsmMachineOperandFactory opFactory) throws ResolveException {
		return opFactory.createOffsetOperand(this);
	}
}
