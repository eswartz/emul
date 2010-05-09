package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.ejs.coffee.core.utils.Check;

import v9t9.tools.asm.assembler.HLInstruction;

public class Block {

	static int nextId;
	
	private int id;

	private LinkedList<HLInstruction> instrs;
    public List<Block> succ;
    public List<Block> pred;
    
    public static final int fVisited = 1;
    static final int fInsideInstruction = 2;
    
    private int flags;

	private Label label;

    public Block(Label label) {
    	this.label = label;
		this.id = nextId++;
		instrs = new LinkedList<HLInstruction>();
        succ = new ArrayList<Block>(2);
        pred = new ArrayList<Block>(2);
    }

	@Override
    public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("block " + id + ": " + label.toString()+"\n");
        for (HLInstruction instr : instrs) {
        	sb.append(instr);
        	sb.append('\n');
        }
        return sb.toString();
        
    }

	public Label getLabel() {
		return label;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
			return true;
		}
        if (obj instanceof Block) {
            Block b = (Block) obj;
            return b.label.equals(label);
        }
        return false;
    }

    
    public Iterator<HLInstruction> iterator() {
        return instrs.iterator();
    }

    public int size() {
    	return instrs.size();
    }
    

    public HLInstruction get(int index) {
    	return instrs.get(index);
    }
    
	public HLInstruction getFirst() {
		if (instrs.isEmpty())
			return null;
		return instrs.get(0);
	}

	public HLInstruction getLast() {
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
    	Check.checkArg(block.getFirst());
    	
        if (!succ.contains(block)) {
			succ.add(block);
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
		instrs.clear();
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getFlags() {
		return flags;
	}

	public void addInst(HLInstruction inst) {
		instrs.add(inst);
	}

	/**
	 * @return
	 */
	public List<HLInstruction> getInstrs() {
		return instrs;
	}

}
