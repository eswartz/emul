/*
  InstructionF99b.java

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
