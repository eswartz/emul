/*
  BssDirective.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.directive;

import java.util.List;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.ll.LLForwardOperand;
import v9t9.tools.asm.assembler.operand.ll.LLImmedOperand;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class BssDirective extends Directive {

	private AssemblerOperand op;

	public BssDirective(List<AssemblerOperand> ops) {
		this.op = ops.get(0);
	}
	
	@Override
	public String toString() {
		return "BSS " + op;
	}

	public IInstruction[] resolve(IAssembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		LLOperand lop = op.resolve(assembler, this); 
		if (lop instanceof LLForwardOperand)
			throw new ResolveException(op, "Cannot allocate size for forward-declared symbol");
		if (!(lop instanceof LLImmedOperand))
			throw new ResolveException(op, "Expected number");
		op = lop;
		setPc(assembler.getPc());
		assembler.setPc((assembler.getPc() + lop.getImmediate()));
		return new IInstruction[] { this };
	}
	
	
}
