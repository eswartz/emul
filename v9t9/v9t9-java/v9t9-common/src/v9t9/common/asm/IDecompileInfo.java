/*
  IDecompileInfo.java

  (c) 2008-2012 Edward Swartz

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
