/*
  InstructionF99b.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.asm;

import v9t9.common.asm.IOperand;
import v9t9.common.asm.RawInstruction;

/**
 * @author Ed
 *
 */
public class InstructionF99b extends RawInstruction {
	public InstructionF99b() {
		super();
	}

	public InstructionF99b(RawInstruction other) {
		super(other);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#toString()
	 */
	@Override
	public String toString() {
		return super.toString();
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#setInst(int)
	 */
	@Override
	public void setInst(int inst) {
		super.setInst(inst);
		setName(InstF99b.getInstName(inst));
		setSize(0);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#setOp1(v9t9.engine.cpu.Operand)
	 */
	@Override
	public void setOp1(IOperand op1) {
		super.setOp1(op1);
		setSize(0);
	}
	
	@Override
	public void setOp2(IOperand op2) {
		super.setOp2(op2);
		setSize(0);
	}
	
	@Override
	public void setOp3(IOperand op3) {
		super.setOp3(op3);
		setSize(0);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.RawInstruction#getSize()
	 */
	@Override
	public int getSize() {
		return super.getSize();
	}
}
