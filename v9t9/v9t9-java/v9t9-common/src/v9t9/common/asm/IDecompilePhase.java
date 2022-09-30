/*
  IDecompilePhase.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * @author ejs
 *
 */
public interface IDecompilePhase {

	void reset();
	
	void run();
	
	Map<Block, Label> getLabels();

	/**
	 * Disassemble all the code.  Required after adding ranges.
	 *
	 */
	Collection<MemoryRange> disassemble();

	/*
	 public MemoryDomain getCPUMemory() {
	        return CPU;
	    }
	
	    public void addInstruction(LLInstruction inst) {
	        instructionMap.put(Integer.valueOf(inst.pc), inst);
	    }
	
	    public LLInstruction getInstruction(int addr) {
	        return instructionMap.get(Integer.valueOf(addr));
	    }
	
	    public Iterator<LLInstruction> instructionIterator() {
	        return instructionMap.values().iterator();
	    }
	
	    public MemoryRanges getRanges() {
	        return ranges;
	    }
	 */

	void dumpInstructions(PrintStream os);

	void dumpInstructions(PrintStream os,
			Collection<MemoryRange> ranges);

	void dumpInstruction(PrintStream os,
			IHighLevelInstruction inst);

	Label getLabel(int addr);

	Routine getRoutine(int addr);

	/**
	 * Add a routine at the given address.  Any label already existing
	 * here is renamed.
	 * @param addr
	 * @param name
	 * @param routine
	 * @return same incoming routine, updated with a label and added to the routines
	 */
	Routine addRoutine(int addr, String name, Routine routine);

	void dumpLabels(PrintStream os);

	void dumpBlocks(PrintStream os);

	void dumpRoutines(PrintStream os);

	void dumpBlock(PrintStream os, Block block);

	void addBlock(Block block);

	Set<Block> getBlocks();

	Collection<Routine> getRoutines();

	/**
	 * 
	 */
	IDecompileInfo getDecompileInfo();

}