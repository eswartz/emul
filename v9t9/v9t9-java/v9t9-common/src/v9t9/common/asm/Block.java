/*
  Block.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.asm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ejs.base.utils.Check;


public class Block implements Comparable<Block>, Iterable<IHighLevelInstruction> {

	static int nextId;
	
	private int id;
	
    /** first instruction in block; only one block holds this first */
    private IHighLevelInstruction first;
    /** last instruction in block; only one block holds this last,
     * but may be <code>null</code> for unresolved block */
    private IHighLevelInstruction last;
    
    public List<Block> succ;
    public List<Block> pred;
    
    public static final int fVisited = 1;
    static final int fInsideInstruction = 2;
    
    private int flags;

    /**
     * Create a block starting at the given inst
     * @param inst
     */
    public Block(IHighLevelInstruction inst) {
    	Check.checkArg((inst.getBlock() == null));
    	this.id = nextId++;
        succ = new ArrayList<Block>(2);
        pred = new ArrayList<Block>(2);
//        if (inst.getInst().getInst() == InstTableCommon.Idata)
//        	throw new IllegalArgumentException();
        this.first = inst;
        this.last = inst;
        this.first.setBlock(this);
    }

    public Block(IHighLevelInstruction first, IHighLevelInstruction last) {
    	this(first);
    	setLast(last);
	}

    public String getName() {
        return "block " + id;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for (IHighLevelInstruction s = first;  s != null; s = s.getLogicalNext()) {
    		sb.append(s);
    		sb.append('\n');
    		if (last == null) {
    			sb.append("<< no last >>");
    			break;
    		}
    		if (s == last)
    			break;
    		if (s.getInst().getPc() > last.getInst().getPc()) {
    			sb.append("<< inconsistent block, expected to end at: " + last + ">>");
    			break;
    		}
    	}
    	return sb.toString();
    }
	
    public String format() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getName());
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
        return (first != null ? first.getInst().pc : 0) - o.first.getInst().pc;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
			return true;
		}
        if (obj instanceof Block) {
            Block b = (Block) obj;
            return b.first != null && b.first.getInst().pc == first.getInst().pc;
        }
        return false;
    }

    class BlockInstIterator implements Iterator<IHighLevelInstruction> {
        private IHighLevelInstruction inst;

        BlockInstIterator() {
            this.inst = getFirst();
        }
        public boolean hasNext() {
            return inst != null && last != null && last.getLogicalNext() != inst;
        }

        public IHighLevelInstruction next() {
            IHighLevelInstruction next = inst;
            if (inst == last || last == null)
            	inst = null;
            else
            	inst = inst.getLogicalNext();
            return next;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    public Iterator<IHighLevelInstruction> iterator() {
        return new BlockInstIterator();
    }

    public int size() {
    	IHighLevelInstruction inst = first;
    	int size = 0;
    	while (inst != null) {
    		size++;
    		inst = inst.getLogicalNext();
    	}
        return size; 
    }
    

    public IHighLevelInstruction get(int index) {
    	IHighLevelInstruction inst = first;
    	while (index-- > 0) {
    		inst = inst.getLogicalNext();
    	}
        return inst; 
    }
    
	public IHighLevelInstruction getFirst() {
		return first;
	}

	public void setLast(IHighLevelInstruction last) {
		
		if (last != null) {
			//Check.checkArg((last.getBlock() == null || last.getBlock() == this));
			
			boolean hitOldLast = false;
			boolean hitNewLast = false;
			
			// make all the insts from first to last use this block;
			// remember if we passed over the old last (i.e. extending a block)
			// because if we didn't, those insts need to be lost
			IHighLevelInstruction inst = this.first;
			while (inst != null) {
				inst.setBlock(null);
				inst.setBlock(this);
				if (inst == this.last) {
					hitOldLast = true;
				}
				//if (inst.getInst().getPc() >= last.getInst().getPc()) {
				if (inst == last) {
					hitNewLast = true;
					break;
				}
				inst = inst.getLogicalNext();
			}
			Check.checkState(hitNewLast);
	
			// reset blocks for insts no longer in block
			if (!hitOldLast && this.last != null) {
				inst = inst.getLogicalNext();
				while (inst != null) {
					inst.setBlock(null);
					//if (inst.getInst().getPc() >= this.last.getInst().getPc()) {
					if (inst == this.last) {
						hitOldLast = true;
						break;
					}
					inst = inst.getLogicalNext();
				}
				
				if (!hitOldLast) {
					Check.checkState(false);
				}
			}
		} else {
			// clear block for from first.next to current last
			IHighLevelInstruction inst = first.getLogicalNext();
			while (inst != null) {
				inst.setBlock(null);
				if (inst == this.last) {
					break;
				}
				inst = inst.getLogicalNext();
			}
		}
		
		this.last = last;
	}

	public IHighLevelInstruction getLast() {
		return last;
	}

	public Block split(IHighLevelInstruction first) {
		if (first == this.first)
			return this;
		IHighLevelInstruction oldLast = this.last;
		Check.checkArg(oldLast);	// can't split unbounded block

		IHighLevelInstruction logPrev = first.getLogicalPrev();
		if (logPrev == null)
			return null;
		boolean found = false;
		for (IHighLevelInstruction inst : this) {
			if (inst == logPrev) {
				found = true;
				break;
			}
		}
		if (!found)
			return null;
		setLast(first.getLogicalPrev());
		first.setBlock(null);
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
			first = first.getLogicalNext();
		}
		first = last = null;
		
	}

	public Set<Integer> getSpannedPcs() {
		Set<Integer> pcSet = new TreeSet<Integer>();
		for (Iterator<IHighLevelInstruction> iter = iterator(); iter.hasNext(); ) {
			IHighLevelInstruction inst = iter.next();
			for (int size = inst.getInst().getSize() - 2; size >= 0; size -= 2)
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
	public void addInst(IHighLevelInstruction inst) {
		if (first == null) {
			first = last = inst;
	        if (inst.getInst().getInst() == InstTableCommon.Idata)
	        	throw new IllegalArgumentException();

		} else {
			last.setPhysicalNext(inst);
			last = inst;
		}
		inst.setBlock(this);
	}

}
