/*
  ConstPoolDirective.java

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
import v9t9.tools.asm.assembler.IAsmInstructionFactory;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.Symbol;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.transform.ConstPool;

/**
 * Indicate where the const table should live (automatically added)
 * @author Ed
 *
 */
public class ConstPoolDirective extends Directive {

	private ConstPool constPool;

	/**
	 * @param ops  
	 */
	public ConstPoolDirective(List<AssemblerOperand> ops) {
		
	}
	@Override
	public String toString() {
		return ("$CONSTTABLE");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.BaseAssemblerInstruction#resolve(v9t9.tools.asm.Assembler, v9t9.engine.cpu.IInstruction, boolean)
	 */
	@Override
	public IInstruction[] resolve(IAssembler assembler, IInstruction previous,
			boolean finalPass) throws ResolveException {
		
		this.constPool = assembler.getConstPool();
		Symbol symbol = constPool.getTableAddr();
		if (symbol == null) {
			return NO_INSTRUCTIONS;
		}
		
		// go even
		assembler.setPc((assembler.getPc() + 1) & 0xfffe);
		setPc(assembler.getPc());

		symbol.setAddr(assembler.getPc());

		// skip table
		assembler.setPc((assembler.getPc() + constPool.getSize() + 1) & 0xfffe);

		return new IInstruction[] { this };
	}
	
	public byte[] getBytes(IAsmInstructionFactory factory) {
		return constPool.getBytes();
	}
	
}
