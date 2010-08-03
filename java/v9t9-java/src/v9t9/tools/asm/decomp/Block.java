/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.asm.decomp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.ejs.coffee.core.utils.Check;

public class Block implements Comparable<Block> {

	static int nextId;
	
	private int id;
	
    /** first instruction in block; only one block holds this fist */
    private HighLevelInstruction first;
    /** last instruction in block; only one block holds this last,
     * but may be <code>null</code> for unresolved block */
    private HighLevelInstruction last;
    
    public List<Block> succ;
    public List<Block> pred;
    
    public static final int fVisited = 1;
    static final int fInsideInstruction = 2;
    
    private int flags;

    /**
     * Create a block starting at the given inst
     * @param inst
     */
    public Block(HighLevelInstruction inst) {
    	Check.checkArg((inst.getBlock() == null));
    	this.id = nextId++;
        succ = new ArrayList<Block>(2);
        pred = new ArrayList<Block>(2);
        this.first = inst;
        this.last = inst;
        this.first.setBlock(this);
    }

    public Block(HighLevelInstruction first, HighLevelInstruction last) {
    	this(first);
    	setLast(last);
	}

	@Override
    public String toString() {
        return "block " + id;
    }

    public String format() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.toString());
        buffer.append(" pred = [");
        for (Object element : pred) {
            Block block = (Block) element;
            buffer.append(block.id);
            buffer.append(' ');
        }
        buffer.append("] succ = [");
        for (Object element : succ) {
            Block block = (Block) element;
            buffer.append(block.id);
            buffer.append(' ');
        }
        buffer.append("]");
        return buffer.toString();
    }

    public int compareTo(Block o) {
        return first.getInst().pc - o.first.getInst().pc;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
			return true;
		}
        if (obj instanceof Block) {
            Block b = (Block) obj;
            return b.first.getInst().pc == first.getInst().pc;
        }
        return false;
    }

    class BlockInstIterator implements Iterator<HighLevelInstruction> {
        private HighLevelInstruction inst;

        BlockInstIterator() {
            this.inst = getFirst();
        }
        public boolean hasNext() {
            return inst != null && last != null && last.getNext() != inst;
        }

        public HighLevelInstruction next() {
            HighLevelInstruction next = inst;
            if (inst == last || last == null)
            	inst = null;
            else
            	inst = inst.getNext();
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public Iterator<HighLevelInstruction> iterator() {
        return new BlockInstIterator();
    }

    public int size() {
    	HighLevelInstruction inst = first;
    	int size = 0;
    	while (inst != null) {
    		size++;
    		inst = inst.getNext();
    	}
        return size; 
    }
    

    public HighLevelInstruction get(int index) {
    	HighLevelInstruction inst = first;
    	while (index-- > 0) {
    		inst = inst.getNext();
    	}
        return inst; 
    }
    
	public HighLevelInstruction getFirst() {
		return first;
	}

	public void setLast(HighLevelInstruction last) {
		
		if (last != null) {
			Check.checkArg((last.getBlock() == null || last.getBlock() == this));
			
			boolean hitOldLast = false;
			boolean hitNewLast = false;
			
			// make all the insts from first to last use this block;
			// remember if we passed over the old last (i.e. extending a block)
			// because if we didn't, those insts need to be lost
			HighLevelInstruction inst = this.first;
			while (inst != null) {
				inst.setBlock(this);
				if (inst == this.last) {
					hitOldLast = true;
				}
				if (inst == last) {
					hitNewLast = true;
					break;
				}
				inst = inst.getNext();
			}
			Check.checkState(hitNewLast);
	
			// reset blocks for insts no longer in block
			if (!hitOldLast && this.last != null) {
				inst = inst.getNext();
				while (inst != null) {
					inst.setBlock(null);
					if (inst == this.last) {
						hitOldLast = true;
						break;
					}
					inst = inst.getNext();
				}
				
				if (!hitOldLast) {
					Check.checkState(false);
				}
			}
		} else {
			// clear block for from first.next to current last
			HighLevelInstruction inst = first.getNext();
			while (inst != null) {
				inst.setBlock(null);
				if (inst == this.last) {
					break;
				}
				inst = inst.getNext();
			}
		}
		
		this.last = last;
	}

	public HighLevelInstruction getLast() {
		return last;
	}

	public Block split(HighLevelInstruction first) {
		if (first == this.first)
			return this;
		HighLevelInstruction oldLast = this.last;
		Check.checkArg(oldLast);	// can't split unbounded block
		first.setBlock(null);
		setLast(first.getPrev());
		Block split = new Block(first);
		split.setLast(oldLast);
		return split;
	}


    public Block[] getPred() {
        return pred.toArray(new Block[pred.size()]);
    }

    public Block[] getSucc() {
        return succ.toArray(new Block[succ.size()]);
    }

    public void addSucc(Block block) {
    	Check.checkArg(block.getFirst());
    	
        if (!succ.contains(block)) {
			succ.add(block);
		}
            
        if (!block.pred.contains(this)) {
			block.pred.add(this);
		}
    }

	public boolean isComplete() {
		return first != null && last != null;
	}

	public Set<Block> getSpannedBlockSet() {
		Set<Block> spanned = new TreeSet<Block>();
		recurseGetSpan(spanned);
		return spanned;
	}

	private void recurseGetSpan(Set<Block> spanned) {
		if (!isComplete() || spanned.contains(this))
			return;
		spanned.add(this);
		for (Block block : succ) {
			if (block != this)
				block.recurseGetSpan(spanned);
		}
	}

	public Set<Block> getExitBlocks() {
		// don't recurse to find exits since we may have loops
		Set<Block> spanned = getSpannedBlockSet();
		for (Iterator<Block> iter = spanned.iterator(); iter.hasNext(); ) {
			if (iter.next().succ.size() != 0)
				iter.remove();
		}
		return spanned;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void clear() {
		for (Block blk : pred)
			blk.succ.remove(this);
		for (Block blk : succ)
			blk.pred.remove(this);
		pred.clear();
		succ.clear();
		
		while (first != null) {
			if (first.getBlock() == this)
				first.setBlock(null);
			else
				break;
			if (first == last)
				break;
			first = first.getNext();
		}
		first = last = null;
		
	}

	public Set<Integer> getSpannedPcs() {
		Set<Integer> pcSet = new TreeSet<Integer>();
		for (Iterator<HighLevelInstruction> iter = iterator(); iter.hasNext(); ) {
			HighLevelInstruction inst = iter.next();
			for (int size = inst.getInst().size - 2; size >= 0; size -= 2)
				pcSet.add((inst.getInst().pc + size) & 0xffff);
		}
		return pcSet;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getFlags() {
		return flags;
	}

	/**
	 * @param inst
	 */
	public void addInst(HighLevelInstruction inst) {
		if (first == null) {
			first = last = inst;
		} else {
			last.setNext(inst);
			last = inst;
		}
		inst.setBlock(this);
	}

}
