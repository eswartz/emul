/*
  Directive.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.assembler.directive;

import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.ResolveException;
import v9t9.tools.asm.assembler.BaseAssemblerInstruction;
import v9t9.tools.asm.assembler.IAsmInstructionFactory;

/**
 * @author Ed
 *
 */
public abstract class Directive extends BaseAssemblerInstruction {
	protected static RawInstruction[] NO_INSTRUCTIONS = new RawInstruction[0];
	public Directive() {
	}
	
	@Override
	public byte[] getBytes(IAsmInstructionFactory factory) throws ResolveException {
		return NO_BYTES;
	}
	
	public boolean isByteOp() {
		return false;
	}
}
