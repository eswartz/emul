/*
  ConstPoolDirective.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.directive;

import java.util.List;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.IAsmInstructionFactory;
import v9t9.tools.asm.IAssembler;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.ll.LLForwardOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * Test a condition (once resolved)
 * @author Ed
 *
 */
public class AssertDirective extends Directive {

	private AssemblerOperand op;
	public AssertDirective(List<AssemblerOperand> ops) {
		this.op = ops.get(0);
	}
	@Override
	public String toString() {
		return op.toString();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.BaseAssemblerInstruction#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.IInstruction, boolean)
	 */
	@Override
	public IInstruction[] resolve(IAssembler assembler, IInstruction previous,
			boolean finalPass) throws ResolveException {

		LLOperand lop = op.resolve(assembler, this); 
		if (lop instanceof LLForwardOperand)
			throw new ResolveException(op, "Values not known for assert");
		if (!(lop instanceof LLImmedOperand))
			throw new ResolveException(op, "Expected number for assert");
		
		if (((LLImmedOperand) lop).getValue() == 0) {
			throw new ResolveException(op, "Assertion failed");
		}
		
		return new IInstruction[] { this };
	}
	
	public byte[] getBytes(IAsmInstructionFactory factory) {
		return new byte[0];
	}
	
}
