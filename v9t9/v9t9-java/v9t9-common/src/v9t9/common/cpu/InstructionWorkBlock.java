/*
  InstructionWorkBlock.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.IMemoryDomain;

/**
 * This block contains variables being modified
 * during execution of an instruction.  Its fields
 * are public for efficiency.
 * @author ejs
 *
 */
public class InstructionWorkBlock  {
	/** the CPU */
	final public ICpuState cpu;
    /** our CPU memory */
    final public IMemoryDomain domain;
    /** the instruction (in) */
    public RawInstruction inst;	
    /** values (in: original, out: changed, if needed) */
    public short pc;
    /** status word (in/out) */
    public short st;
    /** cycle count */
    public int cycles;
    
    public InstructionWorkBlock(ICpuState cpu) {
    	this.cpu = cpu;
    	this.domain = cpu.getConsole();
    	this.pc = cpu.getPC();
	}
    
    public void copyTo(InstructionWorkBlock copy) {
    	copy.inst = inst;
    	copy.pc = pc;
    	copy.st = st;
    	copy.cycles = cycles;
    }

	public InstructionWorkBlock copy() {
		InstructionWorkBlock block = new InstructionWorkBlock(cpu);
		this.copyTo(block);
		return block;
	}

	/**
	 * @param i
	 * @return
	 */
	public String formatOpChange(int i, InstructionWorkBlock after) {
		return "";
	}
}