package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.symbols.ISymbol;


/**
 * A routine, i.e. an entry point which is branched to with a return
 * address.
 * @author ejs
 */
public abstract class Routine {
	private Map<ISymbol, Block> labelBlockMap = new HashMap<ISymbol, Block>();
	private Block mainBlock;
    private ArrayList<Block> blocks;
    
    /** Routine goes not exit in an expected way */
    public static final int fUnknownExit = 1;
    /** Routine is part of another routine */
    public static final int fSubroutine = 2;
    public int flags;
    
	private ISymbol name;
	
	private StackFrame stackFrame;
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
		stackFrame = new StackFrame(def.getTarget());
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
	public StackFrame getStackFrame() {
		return stackFrame;
	}
	
    abstract public boolean isReturn(AsmInstruction inst);
    abstract public AsmInstruction[] generateReturn();


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
		assert !labelBlockMap.containsKey(block);
		blocks.add(block);
		labelBlockMap.put(block.getLabel(), block);
	}
	
	/**
	 * @return the entry
	 */
	public Block getEntry() {
		return entry;
	}
	public void setEntry(Block block) {
		assert entry == null && blocks.contains(block);
		entry = block;
	}
	/**
	 * @return the exit
	 */
	public Block getExit() {
		return exit;
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

	/**
	 * @param visitor
	 */
	public void accept(ICodeVisitor visitor) {
		LinkedList<Block> toVisit = new LinkedList<Block>();
		
		if (visitor.enterRoutine(this)) {
			switch (visitor.getWalk()) {
			case LINEAR:
				for (Block block : blocks) {
					block.accept(visitor);
				}
				break;
			case SUCCESSOR: {
				assert entry != null;
				toVisit.add(entry);
				
				Set<Block> visited = new HashSet<Block>();
				while (!toVisit.isEmpty()) {
					Block block = toVisit.pop();
					for (Block succ : block.getSucc()) {
						if (!visited.contains(succ))
							toVisit.add(succ);
					}
					visited.add(block);
					block.accept(visitor);
				}
				break;
			}
			case DOMINATOR:
				assert entry != null;
				toVisit.add(entry);
				
				while (!toVisit.isEmpty()) {
					Block block = toVisit.pop();
					for (Block child : block.getDominatedChildren()) {
						toVisit.add(child);
					}
					block.accept(visitor);
				}
				break;
			case DOMINATOR_PATHS: {
				assert entry != null ;
				
				List<List<Block>> paths = new ArrayList<List<Block>>();
				
				visitPaths(paths);
				
				for (List<Block> path : paths) {
					for (Block block : path) {
						block.accept(visitor);
					}
				}
				
				break;
			}
			}
			visitor.exitRoutine(this);
		}
	}

	/**
	 * Get all the paths through the dominator tree.
	 * @param paths
	 */
	private void visitPaths(List<List<Block>> paths) {
		
		LinkedList<Pair<Block, List<Block>>> toVisit = new LinkedList<Pair<Block,List<Block>>>();
		toVisit.add(new Pair<Block, List<Block>>(entry, new ArrayList<Block>()));
		
		while (!toVisit.isEmpty()) {
			Pair<Block, List<Block>> pair = toVisit.pop();
			Block block = pair.first;
			List<Block> path = pair.second;
			path.add(block);
			if (!block.getDominatedChildren().isEmpty()) {
				for (Block succ : block.getDominatedChildren()) {
					toVisit.add(new Pair<Block, List<Block>>(succ, new ArrayList<Block>(path)));
				}
			} else {
				paths.add(path);
			}
		}

	}

	/**
	 * 
	 */
	public void setupForOptimization() {
		accept(new RenumberVisitor());		
		accept(new FlowGraphVisitor());		
		accept(new LocalLifetimeVisitor());		
	}

	/**
	 * Split the given block at the given instruction, which must be somewhere
	 * other than the start or end of the block, and make a new block with the
	 * new context (including 'at') labeled 'afterLabel'.
	 * @param block
	 * @param at
	 * @return
	 */
	public Block splitBlockAt(Block block, AsmInstruction at, ISymbol afterLabel) {
		int idx = block.getInstrs().indexOf(at);
		assert idx > 0 && idx < block.getInstrs().size();
		
		List<AsmInstruction> tail = block.getInstrs().subList(idx, block.getInstrs().size());
		
		Block after = new Block(afterLabel);
		after.getInstrs().addAll(tail);
		
		for (Block s : block.succ())
			after.addSucc(s);
		block.succ().clear();
		
		tail.clear();
		
		addBlock(after);
		
		return after;
	}

}
