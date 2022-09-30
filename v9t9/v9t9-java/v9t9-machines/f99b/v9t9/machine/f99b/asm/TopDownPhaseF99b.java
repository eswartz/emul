/*
  TopDownPhase.java

  (c) 2022 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.asm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ejs.base.utils.Check;
import v9t9.common.asm.BaseMachineOperand;
import v9t9.common.asm.Block;
import v9t9.common.asm.DataWordListOperand;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.Label;
import v9t9.common.asm.LabelListOperand;
import v9t9.common.asm.LabelOperand;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.Routine;
import v9t9.common.asm.RoutineOperand;
import v9t9.common.asm.UnknownRoutine;
import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemoryEntry;

/**
 * Top-down phase.  This looks at a known set of entry points
 * and derives functions from that.  Then, blocks and labels are
 * found according to the branches made from known locations.
 * @author ejs
 */
public class TopDownPhaseF99b extends PhaseF99b {

	private List<Block> unresolvedBlocks;
	private List<Routine> unresolvedRoutines;
	private TreeSet<IHighLevelInstruction> routineCalls;
	private TreeSet<Block> flowedBlocks;

	public TopDownPhaseF99b(ICpuState state, IDecompileInfo info) {
		super(state, info);
		unresolvedRoutines = new LinkedList<Routine>();
		unresolvedBlocks = new LinkedList<Block>();
		routineCalls = new TreeSet<IHighLevelInstruction>();

	}

	/* (non-Javadoc)
	 * @see v9t9.common.asm.IDecompilePhase#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		unresolvedBlocks.clear();
		unresolvedRoutines.clear();
		routineCalls.clear();
	}
	
	
	public void run() {
		addDictionaryRoutines();
			
		// add blocks for every branch instruction
		for (IHighLevelInstruction inst : decompileInfo.getLLInstructions().values()) {
			if (inst.getBlock() != null && !blocks.containsKey(inst.getInst().pc)) {
				inst.setBlock(null);
			}
			if (inst.getInst().getInst() == InstTableCommon.Idata) {
				continue;
			}
			boolean doAddBlock = false;
			if ((inst.getFlags() & IHighLevelInstruction.fStartsBlock) != 0) {
				doAddBlock = true;
			} else {
				IHighLevelInstruction logPrev = inst.getLogicalPrev();
				if (logPrev == null || (logPrev.getFlags() & IHighLevelInstruction.fEndsBlock) != 0) {
					doAddBlock = true;
				}
			}
			if (doAddBlock) {
				if (inst.getBlock() == null) {
					addBlock(new Block(inst));
				} else if (inst.getBlock().getFirst() != inst) {
					System.out.println("Splitting block: " + inst.getBlock() + " at " + inst);
					//dumpBlock(System.out, inst.getBlock());
					//inst.getBlock().setLast(null);
					//addBlock(new Block(inst));
					Block split = inst.getBlock().split(inst);
					if (split != null)
						addBlock(split);
				}
			}
		}
		
		unresolvedRoutines.clear();
		unresolvedRoutines.addAll(getRoutines());
		unresolvedBlocks.clear();
		unresolvedBlocks.addAll(blocks.values());
		routineCalls.clear();
		
		boolean changed;
		
		int loops = 0;
		do {
			changed = false;
			
			while (!unresolvedRoutines.isEmpty()) {
				changed = true;
				
				Routine routine = unresolvedRoutines.remove(0);
				unresolvedBlocks.addAll(routine.getEntries());
				
			}
			
			flowedBlocks = new TreeSet<Block>();
			while (!unresolvedBlocks.isEmpty()) {
				changed = true;
				Block block = unresolvedBlocks.remove(0);
				flowBlock(block);
			}
			
			getFlowgraph(getBlocks());
			
			// if any blocks have no preds, make them routines
			for (Block block : getBlocks()) {
				if (block.pred.size() == 0 && validCodeAddress(block.getFirst().getInst().pc)) {
					Label label = labels.get(block);
					if (label == null) {
						Routine routine = addRoutine(block.getFirst().getInst().pc, null, new UnknownRoutine());
						if (routine != null)
							unresolvedRoutines.add(routine);
					}
				}
			}
			
			if (combineBlocks()) {
				changed = true;
				continue;
			}
	
			routineCalls.clear();
			for (Map.Entry<Integer, IHighLevelInstruction> entry : decompileInfo.getLLInstructions().entrySet()) {
				if (entry.getValue().isCall()) {
					routineCalls.add(entry.getValue());
				}
			}
			
			analyzeRoutines();
			
		} while (changed && ++loops < 10);
		
		//dumpInstructions();
		//dumpBlocks();
	}

	/**
	 * 
	 */
	private void addDictionaryRoutines() {
		// Get standard entries
		int paddr = mainMemory.readWord(0x400);
		if (validCodeAddress(paddr)) {
			addRoutine(paddr, "BOOT", new F99bRoutine());
		}
		paddr = mainMemory.readWord(0x402);
		if (validCodeAddress(paddr)) {
			addRoutine(paddr, "COLD", new F99bRoutine());
		}
		
		// builtin 
		for (IMemoryEntry entry : mainMemory.getMemoryEntries()) {
			if (entry.hasReadAccess() && validCodeAddress(entry.getAddr()) ) {
				int addr = entry.getAddr();
				int addrEnd = addr + entry.getSize();
				while (addr < addrEnd) {
					String name = entry.lookupSymbol((short) addr);
					if (name != null) {
						addRoutine(addr, name, new F99bRoutine());
					}
					addr++;
				}
			}
		}
		
	}

	private void flowBlock(Block block) {
		
		IHighLevelInstruction inst = block.getFirst();


		if (flowedBlocks.contains(block))
			return;
		
		flowedBlocks.add(block);
		
		while (inst != null) {
			if (inst == block.getFirst()) {
				 inst.setFlags(inst.getFlags() | IHighLevelInstruction.fStartsBlock);
			}
			if (inst.getInst().getInst() == InstTableCommon.Idata) {
				System.out.println("stopping at data: " + inst);
				if (block.getLast() == null)
					block.setLast(block.getFirst());
				break;
			}

			block.setLast(inst);

			if ((inst.getFlags() & IHighLevelInstruction.fEndsBlock + IHighLevelInstruction.fNotFallThrough) 
					== IHighLevelInstruction.fEndsBlock) {
				// handle block break
				Block nextBlock = getLabelKey(inst.getInst().pc + inst.getInst().getSize());
				if (nextBlock != null) {
					Label nextBlockLabel = decompileInfo.findOrCreateLabel(inst.getInst().pc + inst.getInst().getSize());
					if (!unresolvedBlocks.contains(nextBlockLabel.getBlock())) {
						unresolvedBlocks.add(nextBlockLabel.getBlock());
					}
				}
			}
			if (inst.isBranch()) {


				// where does this go?
				Object data = getLabels(inst);

				if (data instanceof RoutineOperand) {

					Routine rout = ((RoutineOperand) data).routine;
					if (!unresolvedRoutines.contains(rout)) {
						unresolvedRoutines.add(rout);
					}
				} else if (data instanceof LabelOperand) {

					Label label = ((LabelOperand) data).label;
					if (!label.getBlock().isComplete()) {
						unresolvedBlocks.add(label.getBlock());
					}

				} else if (data instanceof LabelListOperand) {
					List<LabelOperand> labelops = ((LabelListOperand) data).operands;
					for (Object element : labelops) {
						LabelOperand op = (LabelOperand) element;
						if (op instanceof RoutineOperand) {
							Routine rout = ((RoutineOperand) op).routine;
							if (!unresolvedRoutines.contains(rout)) {
								unresolvedRoutines.add(rout);
							}
						} else if (op instanceof LabelOperand) {
							// get a block for that destination
							Label label = op.label;
							if (!label.getBlock().isComplete()) {
								unresolvedBlocks.add(label.getBlock());
							}
						} else {
							Check.checkState(false);
						}
					}
				} else {
					// TODO: identify this metadata later
					/*
					if (inst.isReturn() || routine.isReturn(inst)) {
						inst.flags |= LLInstruction.fIsReturn;
					} else {
						inst.flags |= LLInstruction.fUnknown;
					}
					*/
				}

				if (!inst.isCall()) {
					break;
				}
			}

			inst = inst.getLogicalNext();

			// stop
			//if (inst != null && getLabel(inst.getInst().pc) != null) {
			if (inst == null || inst.getBlock() != null) {
				break;
			}

		}

	}

	/**
	 * Get labels referenced by inst.
	 * @param routine current routine
	 * @param inst
	 * @return either single Label, a single Routine, or a List&lt;LabelOperand&gt;
	 */
	private Object getLabels(IHighLevelInstruction inst) {
		if (inst.getInst().getOp1() instanceof LabelOperand) {
			return inst.getInst().getOp1();
		}

		if (!(inst.getInst().getOp1() instanceof IMachineOperand)) {
			return null;
		}

		MachineOperandF99b mop1 = (MachineOperandF99b) inst.getInst().getOp1();
		
		if (operandIsLabel(inst, mop1)) {
			IOperand op = handleLabel(inst, mop1);
			if (op != null) {
				inst.getInst().setOp1(op);
			}
			return op;
		}

		return null;
	}

	/**
	 * Handle a branch to a label
	 * @param inst
	 * @return
	 */
	private IOperand handleLabel(IHighLevelInstruction inst, IMachineOperand mop) {
		IOperand op = handleLabel(inst, operandJumpTarget(inst, mop));
		return op;
	}

	/**
	 * Handle a branch to a label
	 * @param inst instruction referencing label 
	 * @param addr label address
	 * @return LabelOperand or RoutineOperand or original operand
	 */
	private IOperand handleLabel(IHighLevelInstruction inst, int addr) {
		Routine routine = null;
		Label label = findOrCreateLabel(inst, addr, true);
		if (label == null)
			return null;

		if (inst.getInst().getInst() == InstF99b.Icall) {
			routine = getRoutine(addr);
			if (routine == null) {
				if (validCodeAddress(addr)) {
					inst.setFlags(inst.getFlags() | IHighLevelInstruction.fIsCall);
					routine = addRoutine(addr, null, new F99bRoutine());
					if (routine != null)
						unresolvedRoutines.add(routine);
				} else {
					System.out.println("!!! ignoring invalid code reference: "
							+ inst);
				}
			}
			if (routine != null) {
				return new RoutineOperand(routine);
			} else if (label != null) {
				return new LabelOperand(label);
			}
		} else {
			/*
			if (label == null) {
				if (validCodeAddress(addr)) {
					label = addLabel(addr, decompileInfo.getRanges()
							.getRangeContaining(addr) != null, inst.getInst().pc, null);
				} else {
					System.out.println("!!! ignoring invalid code reference: "
							+ inst);
					return null;
				}
			}*/
			return new LabelOperand(label);
		}

		return null;
	}

	private Label findOrCreateLabel(IHighLevelInstruction caller, int addr, boolean force) {
		Block block = null;
		Label label = null;

		IHighLevelInstruction target = decompileInfo.getLLInstructions().get(addr & 0xfffe);
		if (target == null) {
			System.out.println("!!! ignoring invalid code reference: "
					+ caller);
			return null;
		}
		
		if (target.getBlock() == null) {
			// be wary of jumps inside already-recognized code
			if (!force) {
				RawInstruction prevTarget = decompileInfo.getInstruction((addr - 2) & 0xfffe);
				if (prevTarget != null && prevTarget.getSize() >= 4) {
					System.out.println("!!! ignoring jump inside code: " + prevTarget + " (was " + target + ") from "  
							+ caller);
					return null;
				}
				prevTarget = decompileInfo.getInstruction((addr - 4) & 0xfffe);
				if (prevTarget != null && prevTarget.getSize() >= 6) {
					System.out.println("!!! ignoring jump inside code: " + prevTarget + " (was " + target + ") from "  
							+ caller);
					return null;
				}
					
			}
			
			if (target.getInst().getInst() == InstTableCommon.Idata) {
				System.out.println("!!! ignoring jump to data: " + target + " from "  
						+ caller);
				return null;
			}
			
			block = new Block(target);
			addBlock(block);
			unresolvedBlocks.add(block);
		} else {
			block = target.getBlock();
			if (block.getFirst() != target) {
				block = block.split(target);
				if (block == null) {
					// could not split
					return null;
				}
				addBlock(block);
				unresolvedBlocks.add(block);
			}
		}
		label = labels.get(block);
		if (label == null) {
			label = new Label(block, null);
			labels.put(block, label);
		}
		return label;
	}
	
	//  For each block, figure out the 
	//  successors/predecessors lists 
	private void getFlowgraph(Collection<Block> list) {
		for (Block block : list) {
			if (block != null) {
				block.succ.clear();
				block.pred.clear();
			}
		}

		for (Object element : list) {
			Block block = (Block) element;
			if (block == null || !block.isComplete()) {
				continue;
			}
			IHighLevelInstruction inst = block.getLast();
			inst = block.getLast();
			if ((inst.getFlags() & IHighLevelInstruction.fEndsBlock) != 0) {
				if ((inst.getFlags() & IHighLevelInstruction.fIsBranch) != 0
					&& (inst.getFlags() & IHighLevelInstruction.fIsCall) == 0) {
					// jump?
					if (inst.getInst().getOp1() instanceof LabelOperand) {
						addLabelOperandSucc(inst, block, (LabelOperand) inst.getInst().getOp1());
					}
					if (inst.getInst().getOp1() instanceof LabelListOperand) {
						LabelListOperand ll = (LabelListOperand) inst.getInst().getOp1();
						for (LabelOperand lo : ll.operands) {
							addLabelOperandSucc(inst, block, lo);
						}
					}
					if (inst.getInst().getOp2() instanceof LabelOperand) {
						addLabelOperandSucc(inst, block, (LabelOperand) inst.getInst().getOp2());
					}
				}

				// fallthrough?
				if (0 == (inst.getFlags() & IHighLevelInstruction.fNotFallThrough)) {
					IHighLevelInstruction logNext = inst.getLogicalNext();
					if (logNext != null && logNext.getBlock() != null) {
						block.addSucc(logNext.getBlock());
					} else {
						System.out.printf(
								"??? Ignoring fallthrough after >%04X\n",
								inst.getInst().pc);
					}
				}
			} else {
				// normal fall through
				IHighLevelInstruction logNext = inst.getLogicalNext();
				if (logNext != null && logNext.getBlock() != null) {
					block.addSucc(logNext.getBlock());
				} else {
					System.out.printf("??? Ignoring fallthrough after >%04X\n",
							inst.getInst().pc);
				}
			}
		}
	}

	private boolean combineBlocks() {
		boolean anyChanged = false;
		boolean changed;
		
		do {
			changed = false;
			for (Block block : new ArrayList<Block>(getBlocks())) {
				if (block.succ.size() == 1) {
					Block succ = block.succ.get(0);
					if (succ.isComplete() && succ.pred.size() == 1) {
						Label label = labels.get(succ);
						if (label == null) {
							// safe to delete
							//System.out.println("### Removing " + succ);
							IHighLevelInstruction last = succ.getLast();
							removeBlock(succ);
							block.setLast(last);
							changed = true;
						}
					}
				}
			}
		} while (changed);
		return anyChanged;
	}

	private void addLabelOperandSucc(IHighLevelInstruction inst, Block block, LabelOperand op1) {
		Label label = ((LabelOperand) op1).label;
		if (label.getBlock() != null && label.getBlock().getFirst() != null) {
			block.addSucc(label.getBlock());
		} else {
			System.out.printf("??? Ignoring branch to label %s from >%04X\n",
							label.getName(), inst.getInst().pc);
		}
		
	}

	private void analyzeRoutines() {
		for (Routine routine : getRoutines()) {
			analyzeRoutine(routine);
			if (unresolvedRoutines.contains(routine))
				return;
		}
		
		// see if any overlap
		Map<Routine, Collection<Block>> routineBlockMap = new HashMap<Routine, Collection<Block>>();
		Set<Block> handled = new TreeSet<Block>();
		
		for (Routine routine : getRoutines()) {
			Collection<Block> covered = routine.getSpannedBlocks();
			if (!Collections.disjoint(handled, covered)) {
				// we are going in order so make the second a subroutine
				routine.flags |= Routine.fSubroutine;
				
				for (Map.Entry<Routine, Collection<Block>> entry : routineBlockMap.entrySet()) {
					if (entry.getValue().contains(routine.getMainLabel().getBlock())) {
						entry.getKey().addEntry(routine.getMainLabel());
					}
				}
			}
			routineBlockMap.put(routine, covered);
			handled.addAll(covered);
		}
		
		for (Block block : handled) {
			if (!block.isComplete())
				unresolvedBlocks.add(block);
		}

	}
	
	private void analyzeRoutine(Routine routine) {
		if (unresolvedRoutines.contains(routine))
			return;
		
		routine.flags = 0;
		
		routine.examineEntryCode();
		
		Set<Block> exits = new TreeSet<Block>();
		for (Block entry : routine.getEntries()) {
			exits.addAll(entry.getExitBlocks());
		}
		
		for (Block exit : exits) {
			IHighLevelInstruction inst = exit.getLast();
			if (routine.isReturn(inst)) {
				inst.setFlags(inst.getFlags() | IHighLevelInstruction.fIsReturn);
			} else {
				routine.flags |= Routine.fUnknownExit;
			}
		}
		
		if (routine.getDataWords() > 0) {
			// examine all call sites and fix up
			for (IHighLevelInstruction callSite : new ArrayList<IHighLevelInstruction>(routineCalls)) {
				if (callSite.getInst().getOp1() instanceof RoutineOperand
						&& ((RoutineOperand) callSite.getInst().getOp1()).routine == routine) {
					if (!(callSite.getInst().getOp2() instanceof DataWordListOperand)) {
						int[] args = new int[routine.getDataWords()];
						int pc = (callSite.getInst().pc + callSite.getInst().getSize()) & 0xfffe;
						int last = pc + routine.getDataWords() * 2;
						int idx = 0;
						IHighLevelInstruction inst = null;
						while (pc < last) {
							inst = decompileInfo.getLLInstructions().get(pc);
							
							if (inst.getBlock() != null && inst.getBlock().getFirst() == inst)
								break;
							
							noopInstruction(inst);
							args[idx++] = ((BaseMachineOperand)inst.getInst().getOp1()).immed & 0xffff;
							pc += 2;
							if (callSite.getBlock() != null && callSite.getBlock().getLast() == inst) {
								//inst = decompileInfo.getLLInstructions().get(pc);
								//if (inst.getBlock())
								callSite.getBlock().setLast(callSite);
							}
						}
						
						callSite.getInst().setSize(pc - callSite.getInst().getPc());
						//inst = decompileInfo.getLLInstructions().get(pc);
						//callSite.setNext(inst);
						
						callSite.getInst().setOp2(new DataWordListOperand(args));
						//callSite.setNext(decompileInfo.getLLInstructions().get(last));
						unresolvedRoutines.add(routine);
					}
				}
			}
		}
	}

	private void noopInstruction(IHighLevelInstruction inst) {
		if (inst != null) {
			if (inst.getInst().getInst() != InstTableCommon.Idata) {
				System.out.println("NOOP'ing " + inst);
				if (inst.isCall())
					routineCalls.remove(inst);
				if (inst.isBranch() || inst.isReturn()) {
					// this messes up the flowgraph
					removeBadBlock(inst.getBlock());
					
					Collection<Block> blocks = inst.getReferencedBlocks();
					for (Block rblock : blocks) {
						removeBadBlock(rblock);
					}
				}
				
				inst.convertToData();
				decompileInfo.replaceInstruction(inst);
			}
		}
	}

	private void removeBadBlock(Block block) {
		if (block == null)
			return;
		boolean blockIsEntry = false;
		for (Routine routine : getRoutines()) {
			if (unresolvedRoutines.contains(routine))
				continue;
			if (routine.getEntries().contains(block)) {
				// delete routine
				blockIsEntry = true;
				//getRoutines().remove(routine);
				routines.remove(routine.getMainLabel());
				unresolvedRoutines.add(routine);
			} else {
				// block is inside the routine, so just regenerate it
				unresolvedRoutines.add(routine);
				
				/*
				Collection<Block> rblocks = routine.getSpannedBlocks();
				for (Block rblock : rblocks) {
					if (rblock != block && rblock.isComplete())
						removeBlock(rblock);
				}
				*/
			}
		}

		if (blockIsEntry) {
			unresolvedBlocks.add(block);
		} else {
			removeBlock(block);
		}
	}

	private void removeBlock(Block block) {
		Label key = labels.remove(block);
		if (key != null) {
			Routine routine = routines.remove(key);
			if (routine != null)
				unresolvedRoutines.remove(routine);
		}
		blocks.remove(block.getFirst().getInst().pc);
		unresolvedBlocks.remove(block);
		block.clear();
		
	}

	public Set<Integer> getBlockSpannedPcs() {
		Set<Integer> pcSet = new TreeSet<Integer>();
		for (Block block : getBlocks()) {
			pcSet.addAll(block.getSpannedPcs());
		}
		return pcSet;
	}

}
