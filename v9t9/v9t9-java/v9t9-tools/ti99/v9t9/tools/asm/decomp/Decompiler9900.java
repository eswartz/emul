/*
  Decompiler9900.java

  (c) 2011 Edward Swartz

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
package v9t9.tools.asm.decomp;



import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.machine.ti99.cpu.CpuState9900;
import v9t9.tools.asm.assembler.inst9900.AsmInstructionFactory9900;

public class Decompiler9900 extends Decompiler {
	static IMemory memory = new Memory();
	static {
		memory.addDomain(IMemoryDomain.NAME_CPU, new MemoryDomain(IMemoryDomain.NAME_CPU));
	}
    public Decompiler9900() {
		super(memory, AsmInstructionFactory9900.INSTANCE, 
				new CpuState9900(memory.getDomain(IMemoryDomain.NAME_CPU)));
    }
}
