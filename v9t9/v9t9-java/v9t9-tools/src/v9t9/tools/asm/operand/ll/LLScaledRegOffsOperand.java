/*
  LLScaledRegOffsOperand.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.operand.ll;


import ejs.base.utils.HexUtils;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.operand.hl.AssemblerOperand;

/**
 * @author Ed
 *
 */
public class LLScaledRegOffsOperand extends LLOperand {

	private final int addReg;
	private final int register;
	private final int scale;
	private final int offset;

	/** 
	 * 
	 * @param original
	 * @param offset
	 * @param addReg
	 * @param register
	 * @param scale the multiplier
	 */
	public LLScaledRegOffsOperand(AssemblerOperand original,
			int offset, int addReg, int register, int scale) {
		super(original);
		this.offset = offset;
		this.addReg = addReg;
		this.register = register;
		this.scale = scale;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + addReg;
		result = prime * result + offset;
		result = prime * result + register;
		result = prime * result + scale;
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
		LLScaledRegOffsOperand other = (LLScaledRegOffsOperand) obj;
		if (addReg != other.addReg)
			return false;
		if (offset != other.offset)
			return false;
		if (register != other.register)
			return false;
		if (scale != other.scale)
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLScaledOperand#toString()
	 */
	@Override
	public String toString() {
		return "@>" + HexUtils.toHex4(offset) + "(" + 
			addReg + "+" + register + (scale == 1 ? "" : "*" + scale)
			+ ")";
	}
	
	@Override
	public boolean isRegister() {
		return false;
	}
	@Override
	public boolean isConst() {
		return false;
	}
	@Override
	public boolean isMemory() {
		return true;
	}
	/**
	 * @return the addReg
	 */
	public int getAddReg() {
		return addReg;
	}
	/**
	 * @return the register
	 */
	public int getRegister() {
		return register;
	}
	
	/**
	 * @return the scale
	 */
	public int getScale() {
		return scale;
	}
	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.LLOperand#getImmediate()
	 */
	@Override
	public int getImmediate() {
		return offset;
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
		return opFactory.createScaledRegOffsOperand(this);
	}

}
