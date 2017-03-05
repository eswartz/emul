/*
  MachineOperandFactory9900.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.inst9900;

import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.ResolveException;
import v9t9.machine.ti99.cpu.MachineOperand9900;
import v9t9.tools.asm.operand.ll.IAsmMachineOperandFactory;
import v9t9.tools.asm.operand.ll.LLAddrOperand;
import v9t9.tools.asm.operand.ll.LLCountOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOffsetOperand;
import v9t9.tools.asm.operand.ll.LLPCRelativeOperand;
import v9t9.tools.asm.operand.ll.LLRegDecOperand;
import v9t9.tools.asm.operand.ll.LLRegIncOperand;
import v9t9.tools.asm.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.operand.ll.LLRegOffsOperand;
import v9t9.tools.asm.operand.ll.LLRegisterOperand;
import v9t9.tools.asm.operand.ll.LLScaledRegOffsOperand;

/**
 * @author Ed
 *
 */
public class MachineOperandFactory9900 implements IAsmMachineOperandFactory {

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createAddressOperand(v9t9.tools.asm.operand.ll.LLAddrOperand)
	 */
	@Override
	public IMachineOperand createAddressOperand(LLAddrOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(
				MachineOperand9900.OP_ADDR, (short) 0, 
				(short) ((LLAddrOperand)op).getAddress());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createCountOperand(v9t9.tools.asm.operand.ll.LLCountOperand)
	 */
	@Override
	public IMachineOperand createCountOperand(LLCountOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_CNT, 
				(short) op.getCount());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createEmptyOperand()
	 */
	@Override
	public IMachineOperand createEmptyOperand() {
		return MachineOperand9900.createEmptyOperand();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createRegisterOperand(v9t9.tools.asm.operand.ll.LLRegisterOperand)
	 */
	@Override
	public IMachineOperand createRegisterOperand(LLRegisterOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_REG, 
				(short) op.getRegister());
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createRegIndOperand(v9t9.tools.asm.operand.ll.LLRegIndOperand)
	 */
	@Override
	public IMachineOperand createRegIndOperand(LLRegIndOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_IND, 
					(short) op.getRegister());

	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createRegIndOperand(v9t9.tools.asm.operand.ll.LLRegIndOperand)
	 */
	@Override
	public IMachineOperand createRegOffsOperand(LLRegOffsOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_ADDR, 
				(short) op.getRegister(), (short) op.getOffset());

	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createRegIncOperand(v9t9.tools.asm.operand.ll.LLRegIncOperand)
	 */
	@Override
	public IMachineOperand createRegIncOperand(LLRegIncOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_INC, 
				(short) op.getRegister());
	}
	
	@Override
	public IMachineOperand createRegDecOperand(LLRegDecOperand op)
			throws ResolveException {
		throw new ResolveException(op, "register decrement not supported");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createOffsetOperand(v9t9.tools.asm.operand.ll.LLOffsetOperand)
	 */
	@Override
	public IMachineOperand createOffsetOperand(LLOffsetOperand op)
			throws ResolveException {
		return MachineOperand9900.createGeneralOperand(MachineOperand9900.OP_OFFS_R12, 
				(short) op.getOffset());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createJumpOperand(v9t9.tools.asm.operand.ll.LLJumpOperand)
	 */
	@Override
	public IMachineOperand createPCRelativeOperand(LLPCRelativeOperand op)
			throws ResolveException {
		//MachineOperand9900 mop = MachineOperand9900.createGeneralOperand(
		//		MachineOperand9900.OP_ADDR, MachineOperand9900.PCREL, (short)op.getOffset());
		MachineOperand9900 mop = new MachineOperand9900(MachineOperand9900.OP_JUMP);
		mop.val = (short) op.getOffset();
		return mop;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createImmedOperand(v9t9.tools.asm.operand.ll.LLImmedOperand)
	 */
	@Override
	public IMachineOperand createImmedOperand(LLImmedOperand op)
			throws ResolveException {
		return MachineOperand9900.createImmediate(op.getValue());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.ll.IMachineOperandFactory#createScaledRegOffsOperand(v9t9.tools.asm.operand.ll.LLScaledRegOffsOperand)
	 */
	@Override
	public IMachineOperand createScaledRegOffsOperand(LLScaledRegOffsOperand op)
			throws ResolveException {
		throw new ResolveException(op, "scaled register offset not supported");
	}
}
