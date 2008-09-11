/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 25, 2006
 *
 */
package v9t9.tools.decomp;

import java.util.Collection;
import java.util.Map;

import v9t9.engine.cpu.Instruction;

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

    /**
     * Get map of LL instructions generated in code.
     * @return
     */
	public Map<Integer, LLInstruction> getLLInstructions();

	public Instruction getInstruction(int addr);

	public LLInstruction disassemble(int addr, int size);

	public Label findOrCreateLabel(int addr);

	public void replaceInstruction(LLInstruction inst);

	public Label getLabel(int pc);


}
