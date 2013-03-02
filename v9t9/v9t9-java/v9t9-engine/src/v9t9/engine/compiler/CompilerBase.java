/*
  CompilerBase.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
