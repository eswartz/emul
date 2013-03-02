/*
  CompilerBase.java

  (c) 2008-2011 Edward Swartz

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
package v9t9.engine.compiler;

import java.util.HashMap;
import java.util.Map;


import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.compiler.ICompiler;
import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemoryArea;
import v9t9.common.memory.IMemoryEntry;

/**
 * @author Ed
 *
 */
public abstract class CompilerBase implements ICompiler {
	public static final boolean DEBUG = false;
	

	public Map<IMemoryArea, IDecompileInfo> highLevelCodeInfoMap = new HashMap<IMemoryArea, IDecompileInfo>();
	private IInstructionFactory instructionFactory;
	private ICpuState cpuState;

	/**
	 * 
	 */
	public CompilerBase(ICpuState cpuState, IInstructionFactory instructionFactory) {
		this.cpuState = cpuState;
		this.instructionFactory = instructionFactory;
	}
    /* (non-Javadoc)
	 * @see v9t9.engine.compiler.ICompiler#compile(java.lang.String, java.lang.String, v9t9.common.asm.IDecompileInfo, v9t9.common.asm.RawInstruction[], short[])
	 */
	@Override
	abstract public byte[] compile(String uniqueClassName, String baseName,
			IDecompileInfo highLevel, RawInstruction[] insts, short[] entries);
	/* (non-Javadoc)
	 * @see v9t9.engine.compiler.ICompiler#validCpuState()
	 */
	@Override
	abstract public boolean validCpuState();

	abstract public void generateInstruction(int pc, RawInstruction rawins,
            CompileInfo info, CompiledInstInfo ii);
	
    /* (non-Javadoc)
	 * @see v9t9.engine.compiler.ICompiler#getHighLevelCode(v9t9.common.memory.IMemoryEntry)
	 */
    @Override
	public IDecompileInfo getHighLevelCode(IMemoryEntry entry) {
    	IMemoryArea area = entry.getArea();
    	IDecompileInfo highLevel = highLevelCodeInfoMap.get(area);
    	if (highLevel == null) {
    		System.out.println("Initializing high level info for " + entry + " / " + area);
    		highLevel = instructionFactory.createDecompileInfo(cpuState);
    		highLevel.disassemble(entry.getAddr(), entry.getSize());
    		highLevelCodeInfoMap.put(area, highLevel);
    	}
    	return highLevel;
    }
}
