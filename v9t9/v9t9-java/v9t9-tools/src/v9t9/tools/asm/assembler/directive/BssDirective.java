/*
  BssDirective.java

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
