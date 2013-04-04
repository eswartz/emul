/*
  RawInstructionFactory9900.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.asm;

import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.ti99.cpu.InstTable9900;

/**
 * @author Ed
 *
 */
public class RawInstructionFactory9900 implements IRawInstructionFactory {

	final static public RawInstructionFactory9900 INSTANCE = new RawInstructionFactory9900();
	
	/* (non-Javadoc)
	 * @see v9t9.tools.asm.assembler.IInstructionFactory#decodeInstruction(int, v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public RawInstruction decodeInstruction(int pc, IMemoryDomain domain) {
		return InstTable9900.decodeInstruction(domain.readWord(pc), pc, domain);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IRawInstructionFactory#getChunkSize()
	 */
	@Override
	public int getChunkSize() {
		return 2;
	}
}
