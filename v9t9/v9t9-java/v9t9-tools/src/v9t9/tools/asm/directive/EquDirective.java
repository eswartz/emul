/*
  EquDirective.java

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
import v9t9.tools.asm.IAssembler;
import v9t9.tools.asm.Symbol;
import v9t9.tools.asm.operand.hl.AssemblerOperand;
import v9t9.tools.asm.operand.ll.LLForwardOperand;
import v9t9.tools.asm.operand.ll.LLImmedOperand;
import v9t9.tools.asm.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class EquDirective extends Directive {

	private AssemblerOperand op;

	public EquDirective(List<AssemblerOperand> ops) {
		this.op = ops.get(0);
	}
	
	@Override
	public String toString() {
		return "EQU " + op;
	}

	public IInstruction[] resolve(IAssembler assembler, IInstruction previous, boolean finalPass) throws ResolveException {
		// establish initial PC, for "equ $"
		setPc(assembler.getPc());
		
		LLOperand lop = op.resolve(assembler, this);
		if (lop instanceof LLForwardOperand)
			return new IInstruction[] { this };
		
		if (!(lop instanceof LLImmedOperand))
			throw new ResolveException(op, "Expected number");

		// don't change this directive's Pc, in case we depend on $ and get resolved multiple times
		
		if (previous != null && previous instanceof LabelDirective) {
			LabelDirective label = (LabelDirective) previous;
			Symbol symbol = label.getSymbol();
			label.setPc(lop.getImmediate());
			symbol.setDefined(true);
		}
		return new IInstruction[] { this };
	}
	


}
