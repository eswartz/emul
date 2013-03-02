/*
  InstructionWorkBlock.java

  (c) 2010-2012 Edward Swartz

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