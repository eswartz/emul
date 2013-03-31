/*
  HLInstruction.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.ResolveException;



/**
 * @author Ed
 *
 */
public class HLInstruction extends AssemblerInstruction {

	/**
	 * @param factory
	 */
	public HLInstruction(IInstructionFactory factory) {
		super(factory);
	}

	@Override
	public byte[] getBytes(IAsmInstructionFactory factory) throws ResolveException {
		throw new ResolveException(this, null, "Cannot resolve high-level instruction");
	}

	
}
