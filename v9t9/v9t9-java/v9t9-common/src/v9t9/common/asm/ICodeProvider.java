/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.common.asm;

import v9t9.common.memory.IMemoryDomain;

public interface ICodeProvider {
    /**
     * Get CPU memory
     */
    public IMemoryDomain getCPUMemory();
    
    /**
     * Add a new instruction
     */
    //public void addInstruction(LLInstruction inst);
    
    //public LLInstruction getInstruction(int addr);
    
    /**
     * Get an iterator over LLInstruction
     * @return
     */
    //public Iterator<LLInstruction> instructionIterator();

    /**
     * Get shared label map
     */
    //public Map<Integer, Label> getLabelMap();
    
    /**
     * Get shared routine map
     */
    //public Set<Routine> getRoutineMap();

}
