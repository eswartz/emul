package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.llvm.tms9900.asm.Label;

public class Block {

	static int nextId;

	public enum Edge {
		TREE,
		FORWARD,
		CROSS,
		BACK
	}
	
	private int id;

	private LinkedList<AsmInstruction> instrs;
    private List<Block> succ;
    private List<Block> pred;
    
    private Map<Block, Edge> edges;
    
    private Block idom;
    private List<Block> children;
    
    public static final int fVisited = 1;
    public static final int fInsideInstruction = 2;
    
    private int flags;

	private Label label;

    public Block(Label label) {
    	this.label = label;
		this.id = nextId++;
		instrs = new LinkedList<AsmInstruction>();
		edges = new LinkedHashMap<Block, Edge>();
        succ = new ArrayList<Block>(2);
        pred = new ArrayList<Block>(2);
        children = new LinkedList<Block>();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		Block other = (Block) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	@Override
    public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("block " + id + ": " + label.toString()+"\n");
        for (AsmInstruction instr : instrs) {
        	sb.append(instr);
        	sb.append('\n');
        }
        return sb.toString();
        
    }

	public Label getLabel() {
		return label;
	}
    public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String format() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.toString());
        buffer.append(" pred = [");
        for (Block block : pred) {
            buffer.append(block.id);
            buffer.append(' ');
        }
        buffer.append("] succ = [");
        for (Block block : succ) {
            buffer.append(block.id);
            buffer.append(' ');
        }
        buffer.append("]");
        return buffer.toString();
    }

    
    public Iterator<AsmInstruction> iterator() {
        return instrs.iterator();
    }

    public int size() {
    	return instrs.size();
    }
    

    public AsmInstruction get(int index) {
    	return instrs.get(index);
    }
    
	public AsmInstruction getFirst() {
		if (instrs.isEmpty())
			return null;
		return instrs.get(0);
	}

	public AsmInstruction getLast() {
		if (instrs.isEmpty())
			return null;
		return instrs.get(instrs.size() - 1);
	}

    public Block[] getPred() {
        return pred.toArray(new Block[pred.size()]);
    }

    public Block[] getSucc() {
        return succ.toArray(new Block[succ.size()]);
    }

    public void addSucc(Block block) {
    	//Check.checkArg(block.getFirst());
    	
        if (!succ.contains(block)) {
			succ.add(block);
			edges.put(block, Edge.TREE);
		}
            
        if (!block.pred.contains(this)) {
			block.pred.add(this);
		}
    }

	public Set<Block> getSpannedBlockSet() {
		Set<Block> spanned = new TreeSet<Block>();
		recurseGetSpan(spanned);
		return spanned;
	}

	private void recurseGetSpan(Set<Block> spanned) {
		if (/*!isComplete() ||*/ spanned.contains(this))
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

	public void clear() {
		for (Block blk : pred)
			blk.succ.remove(this);
		for (Block blk : succ)
			blk.pred.remove(this);
		pred.clear();
		succ.clear();
		instrs.clear();
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getFlags() {
		return flags;
	}

	public void addInst(AsmInstruction inst) {
		instrs.add(inst);
	}

	public List<AsmInstruction> getInstrs() {
		return instrs;
	}

	public void accept(ICodeVisitor visitor) {
		if (visitor.enterBlock(this)) {
			for (AsmInstruction instr : instrs) {
				instr.accept(this, visitor);
			}
			visitor.exitBlock(this);
		}
	}

	/**
	 * Set the immediate dominator
	 * @param idom the idom to set
	 */
	void setIdom(Block idom) {
		this.idom = idom;
	}
	/**
	 * Get the immediate dominator
	 * @return the idom
	 */
	public Block getIdom() {
		return idom;
	}
	/**
	 * Get the children for which this block is the immediate dominator.
	 * @return the children
	 */
	public List<Block> getDominatedChildren() {
		return children;
	}
	/**
	 * Get mapping of successors to edge types.
	 * @return
	 */
	public Map<Block, Edge> getEdges() {
		return edges;
	}

	/**
	 * @return
	 */
	public List<Block> pred() {
		return pred;
	}

	/**
	 * @return
	 */
	public List<Block> succ() {
		return succ;
	}

}
