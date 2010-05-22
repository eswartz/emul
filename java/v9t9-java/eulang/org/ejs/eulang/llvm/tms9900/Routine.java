package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ejs.eulang.llvm.directives.LLDefineDirective;
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
    
	private ISymbol name;
	
	private Locals locals;
	private final LLDefineDirective def;
	private boolean hasBlCalls;
	private Block entry;
	private Block exit;
    
    public Routine(LLDefineDirective def) {
        this.def = def;
		this.name = def.getName();
		blocks = new ArrayList<Block>();
		entry = null; 
		exit = null;
		locals = new Locals(def);
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

	public ISymbol getName() {
		return name;
	}


    /**
	 * @return the locals
	 */
	public Locals getLocals() {
		return locals;
	}
	
    abstract public boolean isReturn(HLInstruction inst);
    abstract public HLInstruction[] generateReturn();


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

	public void addBlock(Block block) {
		blocks.add(block);
	}
	
	public void setEntry(Block block) {
		assert entry == null && blocks.contains(block);
		entry = block;
	}
	public void setExit(Block block) {
		assert exit == null && blocks.contains(block);
		exit = block;
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	/**
	 * @return
	 */
	public LLDefineDirective getDefinition() {
		return def;
	}

	/**
	 * @param b
	 */
	public void setHasBlCalls(boolean b) {
		this.hasBlCalls = b;
	}
	
	/**
	 * @return the hasCalls
	 */
	public boolean hasBlCalls() {
		return hasBlCalls;
	}

}
