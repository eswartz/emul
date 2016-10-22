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
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.ti99.cpu.InstTable9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;

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
		int op = domain.readWord(pc) & 0xffff;
		RawInstruction inst = InstTable9900.decodeInstruction(op, pc, domain);
		if (inst.getInst() != InstTableCommon.Idata) {
			if (InstTable9900.coerceInstructionOpcode(inst.getInst(), op) != op) {
				// extra bits are set, which might get lost -- may actually be data
				inst.setInst(InstTableCommon.Idata);
				inst.setName("DATA");
				inst.setOp1(MachineOperand9900.createImmediate(op));
				inst.setOp2(null);
				inst.setOp3(null);
				inst.setSize(2);
			}
		}
		return inst;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IRawInstructionFactory#getChunkSize()
	 */
	@Override
	public int getChunkSize() {
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IRawInstructionFactory#getMaxInstrLength()
	 */
	@Override
	public int getMaxInstrLength() {
		return 6;
	}
}
