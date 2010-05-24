/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ejs.eulang.llvm.tms9900.Block.Edge;
import org.ejs.eulang.symbols.ISymbol;

/**
 * Construct the tree edges and the dominator information for a block tree.
 * @author ejs
 *
 */
public class FlowGraphVisitor extends CodeVisitor {

	public boolean DUMP = false;

	private boolean setupFlow = true;
	
	private Map<Integer, Integer> pre = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> rpost = new HashMap<Integer, Integer>();
	private int preorder;
	private int rpostorder;
	
	public FlowGraphVisitor() {
	}
	
	/** Tell whether the successor/predecessor links should be established (default: true) */
	public void setupFlow(boolean setup) {
		this.setupFlow = setup;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#getWalk()
	 */
	@Override
	public Walk getWalk() {
		// not used ;)
		return Walk.LINEAR;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterRoutine(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public boolean enterRoutine(Routine routine) {
		if (setupFlow)
			setupSuccessors(routine);
		
		constructEdges(routine);

		constructDominators(routine);
		
		return false;
	}


	/**
	 * @param routine
	 */
	private void setupSuccessors(Routine routine) {
		Map<ISymbol, Block> labelBlockMap = new HashMap<ISymbol, Block>();
		
		for (Block block : routine.getBlocks()) {
			block.succ().clear();
			block.pred().clear();
			
			ISymbol label = routine.getLocals().getScope().get(block.getLabel().getName());
			assert label != null;
			labelBlockMap.put(label, block);
		}
		
		for (Block block : routine.getBlocks()) {
			AsmInstruction last = block.getLast();
			assert last != null;
			
			for (ISymbol sym : last.getSources()) {
				Block succ = labelBlockMap.get(sym);
				if (succ != null) {
					block.addSucc(succ);
				}
			}
		}
	}

	private void constructEdges(Routine routine) {
		preorder = 1;
		List<Block> blocks = routine.getBlocks();
		rpostorder = blocks.size();
		
		int num = 0;
		for (Block block : blocks) {
			block.setId(num++);
			pre.put(block.getId(), 0);
			rpost.put(block.getId(), 0);
		}
		
		DFS(routine.getEntry());
	}

	/**
	 * Classify the edges of the tree (after Morgan)
	 * @param blocks
	 */
	private void DFS(Block block) {
		assert block != null;
		
		pre.put(block.getId(), preorder);
		preorder++;
		
		block.getEdges().clear();
		
		for (Block succ : block.getSucc()) {
			if (pre.get(succ.getId()) == 0) {
				block.getEdges().put(succ, Edge.TREE);
				DFS(succ);
			}
			else if (rpost.get(succ.getId()) == 0) {
				block.getEdges().put(succ, Edge.BACK);
			}
			else if (pre.get(block.getId()) < pre.get(succ.getId())) {
				block.getEdges().put(succ, Edge.FORWARD);
			}
			else {
				block.getEdges().put(succ, Edge.CROSS);
			}
		}
		
		rpost.put(block.getId(), rpostorder);
		rpostorder--;
	}

	static class BlockSkippingVisitor  {
		private BitSet toVisit;
		private final Block skip;
		public BlockSkippingVisitor(Routine routine, Block skip) {
			this.skip = skip;
			toVisit = new BitSet();

			for (Block block : routine.getBlocks()) {
				if (!block.pred().isEmpty() || block == routine.getEntry()) {
					toVisit.set(block.getId());
				}
			}

			visit(routine.getEntry());
		}
		
		private void visit(Block block) {
			// pretend 'skip' is not in the tree
			if (block == skip)
				return;
			
			// ignore previously visited
			if (!toVisit.get(block.getId()))
				return;
			
			toVisit.clear(block.getId());
			for (Block succ : block.succ()) {
				visit(succ);
			}
		}
		
		public BitSet getSkippedBlocks() {
			return toVisit;
		}
	}
	
	/**
	 * Construct the dominator relationship (after Purdom, from Morgan).
	 * For each block N in the tree, do a depth-first search from Entry pretending that
	 * the block is not in the tree.  Whichever blocks are not reachable are 
	 * dominated by N.
	 * <p>
	 * Then, construct the immediate dominators and domninated children list.
	 * @param routine
	 */
	private void constructDominators(Routine routine) {
		Map<Integer, Block> blocks = new TreeMap<Integer, Block>();
		for (Block block : routine.getBlocks()) {
			blocks.put(block.getId(), block);
		}
		
		Map<Block, BitSet> dominated = new LinkedHashMap<Block, BitSet>();
		for (Block block : routine.getBlocks()) {
			BlockSkippingVisitor skipper = new BlockSkippingVisitor(routine, block);
			dominated.put(block, skipper.getSkippedBlocks());
		}
		
		Map<Block, List<Block>> dominators = new LinkedHashMap<Block, List<Block>>();
		for (Map.Entry<Block, BitSet> entry : dominated.entrySet()) {
			for (int idx = entry.getValue().nextSetBit(0); idx >= 0; idx = entry.getValue().nextSetBit(idx + 1)) { 
				Block domd = blocks.get(idx);
				List<Block> list = dominators.get(domd);
				if (list == null) {
					list = new ArrayList<Block>();
					dominators.put(domd, list);
				}
				list.add(entry.getKey());
			}
		}
		
		if (DUMP) {
			System.out.println("Dominator tree for " + routine + ":");
			for (Map.Entry<Block, List<Block>> entry : dominators.entrySet()) {
				System.out.println(entry.getKey().getLabel());
				System.out.print("\t");
				for (Block b : entry.getValue())
					System.out.print(b.getLabel()+ " ");
				System.out.println();
			}
		}
		
		// With this tree, now construct immediate dominators.
		// A block's idom is the predecessor closest to the node (e.g., next from last)
		for (Block block : routine.getBlocks()) {
			block.setIdom(null);
			block.getDominatedChildren().clear();
		}
		
		for (Map.Entry<Block, List<Block>> entry : dominators.entrySet()) {
			List<Block> dom = entry.getValue();
			Block block = entry.getKey();
			if (dom.size() == 1) {
				assert block == routine.getEntry() || block.pred().isEmpty();
				continue;
			}
			Block idom = dom.get(dom.size() - 2);
			block.setIdom(idom);
			idom.getDominatedChildren().add(block);
		}

		if (DUMP) {
			System.out.println("Immediate dominator tree for " + routine + ":");
			for (Block block : routine.getBlocks()) {
				System.out.println(block.getLabel());
				System.out.println("\tidom = " + (block.getIdom() != null ? block.getIdom().getLabel() : "<<none>>"));
				System.out.print("\tdom children =");
				for (Block b : block.getDominatedChildren()) {
					System.out.print(" "  + b.getLabel());
				}
				System.out.println();
			}
		}

	}

}
