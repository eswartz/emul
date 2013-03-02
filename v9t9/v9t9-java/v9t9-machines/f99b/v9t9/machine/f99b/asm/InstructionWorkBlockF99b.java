/*
  InstructionWorkBlockF99b.java

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
package v9t9.machine.f99b.asm;

import java.util.Arrays;

import v9t9.common.cpu.ICpuState;
import v9t9.common.cpu.InstructionWorkBlock;

public final class InstructionWorkBlockF99b extends InstructionWorkBlock {
    
	private static short[] NO_ENTRIES = new short[0];
	
    public short sp;
	public short rp;
	public short up;
	public short lp;
	public boolean showSymbol;
	
	public short[] inStack = NO_ENTRIES;
	public short[] inReturnStack = NO_ENTRIES;

	public InstructionWorkBlockF99b(ICpuState cpuState) {
    	super(cpuState);
	}
    
    public void copyTo(InstructionWorkBlockF99b copy) {
    	super.copyTo(copy);
    	copy.sp = sp;
    	copy.rp = rp;
    	copy.up = up;
    	copy.lp = lp;
    	copy.showSymbol = showSymbol;
    	copy.inStack = Arrays.copyOf(inStack, inStack.length);
    	copy.inReturnStack = Arrays.copyOf(inReturnStack, inReturnStack.length);
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.cpu.InstructionWorkBlock#copy()
     */
    @Override
    public InstructionWorkBlock copy() {
    	InstructionWorkBlock block = new InstructionWorkBlockF99b(cpu);
		this.copyTo(block);
		return block;
    }

	/**
	 * @return
	 */
	public int nextByte() {
		return domain.readByte(pc++) & 0xff;
	}
	/**
	 * @return
	 */
	public int nextWord() {
		return (domain.readByte(pc++) << 8) | domain.readByte(pc++) & 0xff;
	}
	
}