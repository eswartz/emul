/*
  AssemblerOperand.java

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

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.IAssembler;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;


/**
 * @author ejs
 *
 */
public interface AssemblerOperand extends IOperand {
	/** 
	 * Resolve self to an LLOperand
	 * @param inst
	 * @return new LLOperand or self
	 * @throws ResolveException if cannot resolve
	 */
	LLOperand resolve(IAssembler assembler, IInstruction inst) throws ResolveException;

	/**
	 * Is this classified as a register?
	 */
	boolean isRegister();
	
	/**
	 * Is this classified as memory?
	 */
	boolean isMemory();
	
	boolean isConst();
	
	/**
	 * Replace the src with dst and return this or a new operand.
	 * @param src
	 * @param dst
	 * @return
	 */
	AssemblerOperand replaceOperand(AssemblerOperand src, AssemblerOperand dst);
	
	AssemblerOperand[] getChildren();

	/**
	 * @param i
	 * @return
	 */
	AssemblerOperand addOffset(int i);
	
	void accept(IOperandVisitor visitor);
}
