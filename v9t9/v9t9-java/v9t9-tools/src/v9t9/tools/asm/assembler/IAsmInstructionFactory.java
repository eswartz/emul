/*
  IAsmInstructionFactory.java

  (c) 2010-2011 Edward Swartz

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

import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author ejs
 *
 */
public interface IAsmInstructionFactory extends IInstructionFactory {

	RawInstruction createRawInstruction(LLInstruction inst) throws ResolveException;
	boolean supportsOp(int inst, int num, AssemblerOperand op);
	/** Get the expected size of the instruction.  If operands are unresolved,
	 * aim high.
	 * @param target
	 * @return
	 */
	int getInstSize(LLInstruction ins);
}
