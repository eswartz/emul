/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 2, 2005
 *
 */
package v9t9.engine.compiler;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.TABLESWITCH;

import v9t9.common.memory.MemoryDomain;


public class CompileInfo {
	public InstructionFactory ifact;
    public CompileInfo(ConstantPoolGen pgen, InstructionFactory ifact) {
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
    public int memoryIndex, cpuIndex, nInstructionsIndex, nCyclesIndex;
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
    public MemoryDomain memory;
}