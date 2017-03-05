/*
  IInstructionFactory.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author Ed
 *
 */
public interface IInstructionFactory {

	boolean isByteInst(int inst);

	boolean isJumpInst(int inst);

	String getInstName(int inst);

	/** Fetch and decode an instruction against live operands */
	RawInstruction decodeInstruction(int pc, IMemoryDomain domain);
	byte[] encodeInstruction(RawInstruction instruction);

	int getInstructionFlags(RawInstruction inst);

	IDecompileInfo createDecompileInfo(ICpuState cpuState);
	
}
