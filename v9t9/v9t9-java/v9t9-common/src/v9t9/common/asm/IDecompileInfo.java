/*
  IDecompileInfo.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

import java.util.Collection;
import java.util.Map;


public interface IDecompileInfo {
    public MemoryRanges getMemoryRanges();
    
    /**
     * Get shared label map
     */
    public Map<Block, Label> getLabelMap();

    /**
     * Get shared labels
     */
    public Collection<Label> getLabels();

    /**
     * Get shared block map 
     */
    public Map<Integer, Block> getBlockMap();

    /**
     * Get shared block map 
     */
    public Collection<Block> getBlocks();

    /**
     * Get shared routine map
     */
    public Map<Label, Routine> getRoutineMap();
    
    public Collection<Routine> getRoutines();

    public Map<Integer, RawInstruction> getInstructions();
    /**
     * Get map of LL instructions generated in code.
     * @return
     */
	public Map<Integer, IHighLevelInstruction> getLLInstructions();

	public RawInstruction getInstruction(int addr);

	public IHighLevelInstruction disassemble(int addr, int size);

	public Label findOrCreateLabel(int addr);

	public void replaceInstruction(IHighLevelInstruction inst);

	public Label getLabel(int pc);

	/**
	 * 
	 */
	public void analyze();

	/**
	 * 
	 */
	public void reset();


}
