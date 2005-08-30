/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 2, 2005
 *
 */
package v9t9.cpu;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;


class CompileInfo {
    InstructionFactory ifact;
    public CompileInfo(ConstantPoolGen pgen, InstructionFactory ifact) {
        this.pgen = pgen;
        this.ifact = ifact;
    }
    
    // compile-time info
    InstructionList ilist;
    InstructionHandle doneInst, breakInst, switchInst;
    ConstantPoolGen pgen;
    
    // indexes of useful variables in generated class
    int memoryIndex, cpuIndex, nInstructionsIndex;
    int cruIndex;
    int vdpIndex, gplIndex;
    
    // indexes of our locals in generated method
    int localMemory;
    int localPc, localWp, localStatus;
    int localEa1, localEa2;
    int localVal1, localVal2, localVal3;
    int localInsts;
    
    v9t9.Memory memory;
}