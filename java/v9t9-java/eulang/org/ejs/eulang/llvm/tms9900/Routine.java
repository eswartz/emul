/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import org.ejs.eulang.symbols.ISymbol;

import v9t9.tools.asm.assembler.HLInstruction;


/**
 * A routine, i.e. an entry point which is branched to with a return
 * address.
 * @author ejs
 */
public abstract class Routine {
	
	private Block mainBlock;
    private ArrayList<Block> blocks;
    
    /** Routine goes not exit in an expected way */
    public static final int fUnknownExit = 1;
    /** Routine is part of another routine */
    public static final int fSubroutine = 2;
    public int flags;
    
    protected int dataWords;
	private ISymbol name;
    
    public Routine(ISymbol name) {
        this.name = name;
		blocks = new ArrayList<Block>();
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mainBlock == null) ? 0 : mainBlock.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Routine other = (Routine) obj;
		if (mainBlock == null) {
			if (other.mainBlock != null)
				return false;
		} else if (!mainBlock.equals(other.mainBlock))
			return false;
		return true;
	}
    @Override
    public String toString() {
        return "Routine " + name;
    }
    abstract public boolean isReturn(HLInstruction inst);
    abstract public void generateReturn(Block block);
    
    /** Get the blocks spanned by the routine.  Requires that the flowgraph info is complete. */
	public Collection<Block> getSpannedBlocks() {
		Collection<Block> spannedBlocks = new ArrayList<Block>();
		if (blocks.size() == 1) {
			spannedBlocks = mainBlock.getSpannedBlockSet();
		} else {
			for (Block block : blocks) {
				spannedBlocks.addAll(block.getSpannedBlockSet());
			}
		}
		return Collections.unmodifiableCollection(spannedBlocks);
	}

	/**
	 * @param block
	 */
	public void addBlock(Block block) {
		blocks.add(block);
	}

	/**
	 * @return
	 */
	public ISymbol getName() {
		return name;
	}

	/**
	 * 
	 */
	public List<Block> getBlocks() {
		return blocks;
	}

}
