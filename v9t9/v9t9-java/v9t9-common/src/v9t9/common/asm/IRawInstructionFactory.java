/*
  IRawInstructionFactory.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

import v9t9.common.memory.IMemoryDomain;

/**
 * @author Ed
 *
 */
public interface IRawInstructionFactory {
	/** Get the basic instruction size in bytes */
	int getChunkSize();
	/** Get the maximum instruction size in bytes */
	int getMaxInstrLength();
	/** Fetch and decode an instruction without examining operands */
	RawInstruction decodeInstruction(int pc, IMemoryDomain domain);
}
