/*
  ICodeProvider.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
