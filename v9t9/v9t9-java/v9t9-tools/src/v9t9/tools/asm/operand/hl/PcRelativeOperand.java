/*
  PcRelativeOperand.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.operand.hl;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.IAssembler;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * Operand indicating the base of a PC-relative jump ("$").
 * Its resolved value is the PC.
 * @author ejs
 *
 */
public class PcRelativeOperand extends BaseOperand {

	public PcRelativeOperand() {
	}
	
	@Override
	public String toString() {
		return "$";
	}
	@Override
	public int hashCode() {
		//final int prime = 31;
		int result = 1;
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
		//PcRelativeOperand other = (PcRelativeOperand) obj;
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isMemory()
	 */
	@Override
	public boolean isMemory() {
		return false;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isRegister()
	 */
	@Override
	public boolean isRegister() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.cpu.AssemblerOperand#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.Instruction)
	 */
	public LLOperand resolve(IAssembler assembler, IInstruction inst)
			throws ResolveException {
		int pc = assembler.getPc();
		if (inst != null)
			pc = inst.getPc();
		LLImmedOperand op = new LLImmedOperand(pc);
		return op;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#getChildren()
	 */
	@Override
	public AssemblerOperand[] getChildren() {
		return new AssemblerOperand[0];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.operand.hl.AssemblerOperand#addOffset(int)
	 */
	@Override
	public AssemblerOperand addOffset(int i) {
		return null;
	}
}
