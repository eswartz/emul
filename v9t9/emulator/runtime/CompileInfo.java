/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 2, 2005
 *
 */
package v9t9.emulator.runtime;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.TABLESWITCH;

import v9t9.engine.memory.MemoryDomain;


public class CompileInfo {
    InstructionFactory ifact;
    public CompileInfo(ConstantPoolGen pgen, InstructionFactory ifact) {
        this.pgen = pgen;
        this.ifact = ifact;
    }
    
    // compile-time info
    InstructionList ilist;
	public InstructionList breakList;
	public TABLESWITCH sw;
    InstructionHandle doneInst, breakInst, switchInst;
    ConstantPoolGen pgen;
    
    // indexes of useful variables in generated class
    int memoryIndex, cpuIndex, nInstructionsIndex;
    int cruIndex;
    int vdpIndex, gplIndex;
    public int executionTokenIndex;
    
    // indexes of our locals in generated method
    int localMemory;
    int localPc, localWp, localStatus;
    int localEa1, localEa2;
    int localVal1, localVal2, localVal3;
    int localInsts;

    // only set if Compiler.settingOptimizeRegAccess is on
    int localWpWordMemory;
    int localWpOffset;
    int localTemp; // 16-bit
    
    //v9t9.memory.Memory memory;
    MemoryDomain memory;
}