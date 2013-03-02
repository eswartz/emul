/*
  RawInstructionFactory9900.java

  (c) 2011-2012 Edward Swartz

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
		return InstTable9900.decodeInstruction(domain.flatReadWord(pc), pc, domain);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IRawInstructionFactory#getChunkSize()
	 */
	@Override
	public int getChunkSize() {
		return 2;
	}
}
