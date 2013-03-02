/*
  RegDecOperand.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.operand.hl;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegDecOperand;
import v9t9.tools.asm.assembler.operand.ll.LLRegisterOperand;

/**
 * *Rx- / @Rx-
 * @author ejs
 *
 */
public class RegDecOperand extends RegisterOperand {

	public RegDecOperand(AssemblerOperand reg) {
		super(reg);
	}

	@Override
	public String toString() {
		return "*" + super.toString() + "-";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getReg() == null) ? 0 : getReg().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RegDecOperand other = (RegDecOperand) obj;
		if (getReg() == null) {
			if (other.getReg() != null) {
				return false;
			}
		} else if (!getReg().equals(other.getReg())) {
			return false;
		}
		return true;
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
	
	public LLOperand resolve(IAssembler assembler, IInstruction inst)
			throws ResolveException {
		LLRegisterOperand regRes = (LLRegisterOperand) super.resolve(assembler, inst);
		return new LLRegDecOperand(regRes.getRegister());
	}
	

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.operand.hl.BaseOperand#replaceOperand(v9t9.tools.asm.assembler.operand.hl.AssemblerOperand, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public AssemblerOperand replaceOperand(AssemblerOperand src,
			AssemblerOperand dst) {
		if (src.equals(this))
			return dst;
		AssemblerOperand newReg = getReg().replaceOperand(src, dst);
		if (newReg != getReg()) {
			if (newReg.isRegister() || newReg instanceof NumberOperand) {
				return new RegDecOperand(newReg);
			} else {
				assert false;
			}
		}
		return this;
	}
	
}
