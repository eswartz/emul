/*
  IAsmInstructionFactory.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
