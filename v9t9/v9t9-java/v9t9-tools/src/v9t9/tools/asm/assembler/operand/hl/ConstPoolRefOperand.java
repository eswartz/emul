/*
  ConstPoolRefOperand.java

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
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.common.asm.ICpuInstruction;
import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * A request for a const
 * @author Ed
 *
 */
public class ConstPoolRefOperand extends ImmediateOperand {

	public ConstPoolRefOperand(AssemblerOperand op) {
		super(op);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.ImmediateOperand#toString()
	 */
	@Override
	public String toString() {
		return "#" + super.toString();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return true;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}
	public LLOperand resolve(IAssembler assembler, IInstruction inst) throws ResolveException {
		LLOperand op = immed.resolve(assembler, inst);
		if (op instanceof LLForwardOperand)
			return new LLForwardOperand(this, 2);
		if (!(op instanceof LLImmedOperand)) {
			throw new ResolveException(op, "Expected an immediate");
		}
		
		int value = op.getImmediate();
		AssemblerOperand addr;
		boolean isByte = inst instanceof ICpuInstruction &&
			assembler.getInstructionFactory().isByteInst(((ICpuInstruction) inst).getInst());
		if (isByte) {
			addr = assembler.getConstPool().allocateByte(value);
		} else {
			addr = assembler.getConstPool().allocateWord(value);
		}
		
		LLOperand resOp = addr.resolve(assembler, inst);
		resOp.setOriginal(this);
		return resOp;
	}

	/**
	 * @return
	 */
	public Integer getValue() {
		return ((NumberOperand) immed).getValue();
	}
	

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand newVal = immed.replaceOperand(src, dst);
		if (newVal != immed) {
			return new ConstPoolRefOperand(newVal);
		}
		return this;
	}
	
}
