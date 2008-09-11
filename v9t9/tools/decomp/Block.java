/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.tools.decomp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import v9t9.utils.Check;

public class Block implements Comparable<Block> {

	static int nextId;
	
	private int id;
	
    /** first instruction in block; only one block holds this fist */
    private LLInstruction first;
    /** last instruction in block; only one block holds this last,
     * but may be <code>null</code> for unresolved block */
    private LLInstruction last;
    
    public List<Block> succ;
    public List<Block> pred;
    
    static final int fVisited = 1;
    static final int fInsideInstruction = 2;
    
    int flags;

    /**
     * Create a block starting at the given inst
     * @param inst
     */
    public Block(LLInstruction inst) {
    	Check.checkArg(inst.getBlock() == null);
    	this.id = nextId++;
        succ = new ArrayList<Block>(2);
        pred = new ArrayList<Block>(2);
        this.first = inst;
        this.last = null;
        this.first.setBlock(this);
    }

    public Block(LLInstruction first, LLInstruction last) {
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
        return first.pc - o.first.pc;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
			return true;
		}
        if (obj instanceof Block) {
            Block b = (Block) obj;
            return b.first.pc == first.pc;
        }
        return false;
    }

    class BlockInstIterator implements Iterator<LLInstruction> {
        private LLInstruction inst;

        BlockInstIterator() {
            this.inst = getFirst();
        }
        public boolean hasNext() {
            return inst != null && last != null && last.getNext() != inst;
        }

        public LLInstruction next() {
            LLInstruction next = inst;
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
    
    public Iterator<LLInstruction> iterator() {
        return new BlockInstIterator();
    }

    public int size() {
    	LLInstruction inst = first;
    	int size = 0;
    	while (inst != null) {
    		size++;
    		inst = inst.getNext();
    	}
        return size; 
    }
    

    public LLInstruction get(int index) {
    	LLInstruction inst = first;
    	while (index-- > 0) {
    		inst = inst.getNext();
    	}
        return inst; 
    }
    
	public LLInstruction getFirst() {
		return first;
	}

	public void setLast(LLInstruction last) {
		
		if (last != null) {
			Check.checkArg(last.getBlock() == null || last.getBlock() == this);
			
			boolean hitOldLast = false;
			boolean hitNewLast = false;
			
			// make all the insts from first to last use this block;
			// remember if we passed over the old last (i.e. extending a block)
			// because if we didn't, those insts need to be lost
			LLInstruction inst = this.first;
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
			LLInstruction inst = first.getNext();
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

	public LLInstruction getLast() {
		return last;
	}

	public Block split(LLInstruction first) {
		if (first == this.first)
			return this;
		LLInstruction oldLast = this.last;
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
		for (Iterator<LLInstruction> iter = iterator(); iter.hasNext(); ) {
			LLInstruction inst = iter.next();
			for (int size = inst.size - 2; size >= 0; size -= 2)
				pcSet.add((inst.pc + size) & 0xffff);
		}
		return pcSet;
	}

}
