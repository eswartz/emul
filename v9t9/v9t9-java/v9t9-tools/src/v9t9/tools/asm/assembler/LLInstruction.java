/*
  LLInstruction.java

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
package v9t9.tools.asm.assembler;

import v9t9.common.asm.ICpuInstruction;
import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class LLInstruction extends BaseAssemblerInstruction implements ICpuInstruction {
	private final IInstructionFactory factory;

	public LLInstruction(IInstructionFactory factory) {
		super();
		this.factory = factory;
	}

	private int inst;
	private LLOperand op1;
	private LLOperand op2;
	private LLOperand op3;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LLInst ");
		builder.append(factory.getInstName(inst));
		if (op1 != null) {
			builder.append(' ');
			builder.append(op1);
			if (op2 != null) {
				builder.append(',');
				builder.append(op2);
				if (op3 != null) {
					builder.append(',');
					builder.append(op3);
				}
			}
		}
		return builder.toString();
	}

	
	public IInstruction[] resolve(IAssembler assembler, IInstruction previous,
			boolean finalPass) throws ResolveException {
		setPc(assembler.getPc());
		setOp1(op1 != null ? op1.resolve(assembler, this) : null);
		setOp2(op2 != null ? op2.resolve(assembler, this) : null);
		setOp3(op3 != null ? op3.resolve(assembler, this) : null);
		int size = assembler.getInstructionFactory().getInstSize(this);
		assembler.setPc(getPc() + size);
		return new IInstruction[] { this };
	}
	
	/*
	private int calculateInstructionSize() {
		int size = 0;
    	if (getInst() == InstTableCommon.Idata) {
    		size = 2;
    		return size;
    	} else if (getInst() == InstTableCommon.Ibyte) {
    		size = 1;
    		return size;
    	}
    	size = 2;
    	InstEncodePattern pattern = InstTable9900.lookupEncodePattern(getInst());
		if (pattern == null)
			return size;
		
		if (op1 != null)
			size += coerceSize(pattern.op1, getOp1().getSize());
		if (op2 != null)
			size += coerceSize(pattern.op2, getOp2().getSize());
		//if (op3 != null)
		//	size += coerceSize(pattern.op3, getOp3().getSize());
		return size;
	}

	private int coerceSize(int type, int size) {
		if (size > 0) {
			if (type == InstEncodePattern.CNT || type == InstEncodePattern.OFF)
				size = 0;
		}
		return size;
	}
*/
	public byte[] getBytes(IAsmInstructionFactory instFactory) throws ResolveException {
		RawInstruction instruction = instFactory.createRawInstruction(this);
		byte[] bytes = instFactory.encodeInstruction(instruction);
		return bytes;
	}
	
	public void setOp1(LLOperand op1) {
		this.op1 = op1;
	}

	public LLOperand getOp1() {
		return op1;
	}

	public void setOp2(LLOperand op2) {
		this.op2 = op2;
	}

	public LLOperand getOp2() {
		return op2;
	}

	public void setOp3(LLOperand op3) {
		this.op3 = op3;
	}

	public LLOperand getOp3() {
		return op3;
	}
	
	public void setInst(int inst) {
		this.inst = inst;
	}
	
	public int getInst() {
		return inst;
	}
}
