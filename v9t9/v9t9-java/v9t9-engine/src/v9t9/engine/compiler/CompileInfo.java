/*
  CompileInfo.java

  (c) 2005-2011 Edward Swartz

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

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.TABLESWITCH;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.compiler.ICompiler;
import v9t9.common.memory.IMemoryDomain;


public class CompileInfo {
	public boolean optimize;
	public boolean optimizeRegAccess;
	
	public InstructionFactory ifact;
	public ISettingsHandler settings;
	
    public CompileInfo(ISettingsHandler settings, ConstantPoolGen pgen, InstructionFactory ifact) {
    	this.settings = settings;
    	this.optimize = settings.get(ICompiler.settingOptimize).getBoolean();
    	this.optimizeRegAccess = optimize && 
    		settings.get(ICompiler.settingOptimizeRegAccess).getBoolean();
    	
        this.pgen = pgen;
        this.ifact = ifact;
    }
    
    // compile-time info
    public InstructionList ilist;
	public InstructionList breakList;
	public TABLESWITCH sw;
	public InstructionHandle doneInst, breakInst, switchInst;
	public ConstantPoolGen pgen;
    
    // indexes of useful variables in generated class
    public int memoryIndex, cpuIndex, cpuStateIndex, nInstructionsIndex, nCyclesIndex;
    public int cruIndex;
    public int vdpIndex, gplIndex;
    public int executionTokenIndex;
    
    // indexes of our locals in generated method
    /** MemoryDomain */
    public int localMemory;
    public int localPc, localWp, localStatus;
    public int localEa1, localEa2;
    public int localVal1, localVal2, localVal3;
    public int localInsts, localCycles;

    // only set if Compiler.settingOptimizeRegAccess is on
    public int localWpWordMemory;
    public int localWpOffset;
    public int localTemp; // 16-bit
    
    //v9t9.memory.Memory memory;
    public IMemoryDomain memory;
}