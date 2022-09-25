/*
  TopDownPhase.java

  (c) 2008-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.asm;

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
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;

/**
 * Top-down phase.  This looks at a known set of entry points
 * and derives functions from that.  Then, blocks and labels are
 * found according to the branches made from known locations.
 * @author ejs
 */
public class TopDownPhase extends Phase {

	private List<Block> unresolvedBlocks;
	private List<Routine> unresolvedRoutines;
	private TreeSet<IHighLevelInstruction> routineCalls;
	private TreeSet<Block> flowedBlocks;

	public TopDownPhase(ICpuState state, IDecompileInfo info) {
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
		addStandardROMRoutines();
			
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

	private void flowBlock(Block block /*Routine routine, List<Block> unresolvedBlocks,
			List<Routine> unresolvedRoutines*/) {
		
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

		MachineOperand9900 mop1 = (MachineOperand9900) inst.getInst().getOp1();
		
		// try to convert a reg reference to a multi-label or a known address 
		if (mop1.isRegisterReference()) {
			if (mop1.type != MachineOperand9900.OP_REG && mop1.val != 0) {
				LabelListOperand llo = handleJumpTable(inst, mop1);
				if (llo != null) {
					inst.getInst().setOp1(llo);
				}
				return llo;
			} else {
				// branch into workspace (whatever!)
				if (inst.getWp() != 0) {
					int addr = (inst.getWp() + mop1.val * 2) & 0xfffe;
					mop1.type = MachineOperand9900.OP_ADDR;
					mop1.immed = (short) addr;
					mop1.val = 0;
					// fall through
				}
			}
		}

		
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

		if (inst.getInst().getInst() == Inst9900.Iblwp) {
			// context switch
			routine = getRoutine(addr + 2);
			if (routine == null) {
				routine = addPossibleContextSwitch(addr, null);
				if (routine != null) {
					inst.setFlags(inst.getFlags() | IHighLevelInstruction.fIsCall);
					return new RoutineOperand(routine);
				}
			}
		} else if (inst.getInst().getInst() == Inst9900.Ibl) {
			routine = getRoutine(addr);
			if (routine == null) {
				if (validCodeAddress(addr)) {
					inst.setFlags(inst.getFlags() | IHighLevelInstruction.fIsCall);
					routine = addRoutine(addr, null, new LinkedRoutine());
					if (routine != null)
						unresolvedRoutines.add(routine);
				} else {
					System.out.println("!!! ignoring invalid code reference: "
							+ inst);
				}
			} else {
				if (!(routine instanceof LinkedRoutine)) {
					System.out
							.println("found a BL to a routine already marked with BLWP: "
									+ routine);
					routine = null;
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

	/**
	 * Treat an operand as the target of a branch through a jump table
	 * @param mop1 operand indirecting register
	 * @return list of LabelOperands
	 */
	private LabelListOperand handleJumpTable(IHighLevelInstruction inst, MachineOperand9900 mop1) {
		if (mop1.type == MachineOperand9900.OP_ADDR) {
			// register plus address:  address is code and register holds word-aligned offset:
			//
			//	LI R2, 2
			//  BL @>A000(R2)  >A002 = code
			//
			//  LI R2, >A000
			//  BL @>2(R2)     >A002 = code
			//
			//	LI R2, 8
			//  BLWP @>A000(R2)  >A008 = vector
			//
			//  LI R2, >A000
			//  BLWP @>8(R2)     >A008 = vector
			
			// for now, assume table is chock full of valid addresses
			// and ends when an invalid address is found
			List<LabelOperand> dests = new ArrayList<LabelOperand>();
			int addr = mop1.immed;
			//Range srcRange = codeProvider.getRangeContaining(inst.getInst().pc);
			while (true) {
				int entry = mainMemory.readWord(addr);
				if (validCodeAddress(entry)) {
					IOperand op = handleLabel(inst, entry);
					if (op instanceof LabelOperand) {
						dests.add((LabelOperand) op);
					} else {
						break;
					}
				} else {
					break;
				}
				if (inst.getInst().getInst() == Inst9900.Iblwp) {
					addr += 4;
				} else {
					addr += 2;
				}
			}
			if (dests.size() > 0) {
				return new LabelListOperand(mop1, dests);
			} else {
				System.out.println("!!! ouch: jump table seems bogus at "
						+ inst);
				return null;
			}
		} else if (mop1.type == MachineOperand9900.OP_INC
				|| mop1.type == MachineOperand9900.OP_IND) {
			//if (routine.isReturn(inst)) {
			//	return null;
			//}

			// register indirect:  register holds the address to jump to.
			//
			//	MOV @>A000(R3), R2	R2 = >B000
			//  BL *R2			  >B000 = code

			// need to calculate the register value
			//IAstExpression expr = buildExpressionFor(inst, mop1);
			Label[] table = findJumpTable(inst, mop1);
			if (table != null) {
				List<LabelOperand> dests = new ArrayList<LabelOperand>();
				for (Label label : table) {
					// TODO: scan for multiple uses of an addr (Routines mapping here)
					/*
					Operand op = handleLabel(inst, entry);
					if (op instanceof LabelOperand) {
						dests.add((LabelOperand) op);
					}*/
					dests.add(new LabelOperand(label));
				}
				return new LabelListOperand(mop1, dests);
			} else {
				System.out.println("cannot parse jump through register at "	+ inst);
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * From the given instruction, go backwards and find what
	 * jump table is likely associated with the value of the register.
	 * <p>
	 * For instance:
	 * <pre>
	 * 		MOV @>A000, R2
	 * 		BL *R2
	 * </pre>
	 * returns one entry for the word at >A000
	 * <p>
	 * <pre>
	 * 		MOV @>A000(R3), R2
	 * 		BL *R2
	 * </pre>
	 * returns entries at >A000 which look like code
	 * @param inst
	 * @param mop1
	 * @return array of Labels or <code>null</code> if we can't determine anything
	 */
	Label[] findJumpTable(IHighLevelInstruction caller,
			MachineOperand9900 mop1) {
		if (!mop1.isRegisterReference())
			return null;
		
		
		//Block startBlock = inst.getBlock();
		
		int reg = mop1.val;
		
		int maxInsts = 16;
		
		IHighLevelInstruction inst = caller.getLogicalPrev();

		while (inst != null && maxInsts-- > 0) {
			// DON'T stop... any entry point to the code is valid
			// (but we might want to be more exhaustive in the future)
			// // stop if we leave the block
			//if (inst.getBlock() != startBlock)
			//	break;

			// see if instruction modifies register
			if (inst.getInst().getOp2() instanceof IMachineOperand &&
					((IMachineOperand) inst.getInst().getOp2()).isRegisterReference(reg)) {
				IMachineOperand mop2 = ((IMachineOperand) inst.getInst().getOp2());
				if (mop2.isRegister()) {
					// directly set
					if (inst.getInst().getOp1() instanceof IMachineOperand) {
						// just look for address
						MachineOperand9900 fromOp1 = (MachineOperand9900) inst.getInst().getOp1();
						if (fromOp1.type == MachineOperand9900.OP_ADDR) {
							int size;
							if (fromOp1.val == 0) {
								short target = mainMemory.readWord(fromOp1.immed);
								if (target == 0) {
									// probably a saved return address or vector
									System.out.println("Ignoring possible stored address at " + inst);
									return null;
								}
								System.out.println("Looks like " + inst + " stores return/vector");
								size = 2;
							} else {
								System.out.println("Looks like " + inst + " provides jump table");
								size = guessJumpTableSize(inst, fromOp1);
								System.out.println("Appears to be at most " + size + " bytes");
							}
							return scanJumpTable(caller, fromOp1.immed, size);
						} else {
							System.out.println("Unknown from modifying " + mop1 + " in " + inst);
						}
					} else {
						System.out.println("Unknown effect on " + mop1 + " in " + inst);
					}
				} else {
					System.out.println("Register not directly set in " + inst);
				}
				return null;
			}
			inst = inst.getLogicalPrev();
		}
		
		return null;
	}
	
	/** Guess the size of a jump table, looking for specific sequences of instructions:
	 * <pre>
	 * 		SRL reg,cnt
	 * </pre>
	 * <p>
	 * <pre>
	 * 		SRL reg,cnt
	 * 		SLA reg,cnt
	 * </pre>
	 * <p>
	 * <pre>
	 * 		ANDI reg,immed
	 * </pre>
	 * <p>
	 * @param inst
	 * @param fromOp1
	 * @return
	 */
	private int guessJumpTableSize(IHighLevelInstruction inst, MachineOperand9900 fromOp1) {
		Block startBlock = inst.getBlock();
		int range = 0xffff;
		
		if (!fromOp1.isRegisterReference()) {
			System.out.println("Dunno how the register was made in " + fromOp1);
			return range;
		}
		
		int reg = fromOp1.val;
		
		while (inst.getLogicalPrev() != null) {
			inst = inst.getLogicalPrev();

			// stop if we leave the block
			if (inst.getBlock() != startBlock)
				break;

			if (inst.getInst().getOp1() instanceof IMachineOperand
					&& ((IMachineOperand) inst.getInst().getOp1()).isRegister(reg)) {
				if (inst.getInst().getOp2() instanceof IMachineOperand == false) {
					System.out.println("Can't interpret HL operand in " + inst);
					break;
				}
				BaseMachineOperand mop = (BaseMachineOperand) inst.getInst().getOp2();

				// adjust the range by the instruction
				
				// note: for adds/subs, we don't constrain the range, since
				// it may overflow (since it's max to begin with).
				boolean calced = true;
				switch (inst.getInst().getInst()) {
				case Inst9900.Isrl:
					range >>= mop.val;
					break;
				case Inst9900.Isla:
					range = (range << mop.val) & 0xffff;
					break;
				case Inst9900.Isrc:
					range = (short) (( (range & 0xffff) >> mop.val) | (range << (16 - mop.val)));
					break;
				case Inst9900.Iandi:
					range &= mop.immed;
					break;
				case Inst9900.Iai:
					range += mop.val;
					break;
				case Inst9900.Ili:
					range = mop.immed;
					break;
				case Inst9900.Iori:
					range |= mop.immed;
					break;
				case Inst9900.Iinc:
					range++;
					break;
				case Inst9900.Iinct:
					range += 2;
					break;
				case Inst9900.Idec:
					range--;
					break;
				case Inst9900.Idect:
					range -= 2;
					break;
				default:
					System.out.println("Unknown range effect in " + inst);
					calced = false;  
					break;
				}
				if (!calced)
					break;
			}
		}
		return Math.min(range, 0xffff) & 0xffff;
	}

	private Label[] scanJumpTable(IHighLevelInstruction caller, short addr, int size) {
		List<Label> entries = new ArrayList<Label>();
		while (size > 0) {
			short pc = mainMemory.readWord(addr);
			Label label = findOrCreateLabel(caller, pc & 0xfffe, false);
			if (label != null) {
				entries.add(label);
			} else {
				break;
			}
			addr += 2;
			size -= 2;
		}
		return (Label[]) entries.toArray(new Label[entries.size()]);
	}

	/**
	 * Get the given instruction's expression effect on the register
	 * <p>
	 * For instance:
	 * <pre>
	 * 		LI R2, 200
	 * </pre>
	 * returns IAstLiteralExpression(200)
	 * <p>
	 * <pre>
	 * 		AI R2, 200
	 * </pre>
	 * returns IAstLiteralExpression(200)
	 * <p>
	 * <pre>
	 * 		MOV @>A000, R2
	 * </pre>
	 * returns IAstUnaryExpression(K_INDIRECT, IAstAddressExpression(0xa000))
	 * <p>
	 * <pre>
	 * 		MOV @>A000(R3), R2
	 * </pre>
	 * returns IAstUnaryExpression(K_INDIRECT, 
	 * 		IAstBinaryExpression(K_ADD, 
	 * 			IAstRegisterExpression(3), 
	 * 			IAstUnaryExpression(K_INDIRECT, IAstAddressExpression(0xa000))
	 * 		)
	 *  )
	 * @param inst
	 * @param mop1
	 * @return
	 */
	//
	
	/*
	IAstExpression getExpressionForOperand(HighLevelInstruction inst, MachineOperand mop1) {
		switch (mop1.type) {
		case MachineOperand.OP_IMMED:
		case MachineOperand.OP_CNT:
		case MachineOperand.OP_OFFS_R12:
			return new AstIntegralExpression(mop1.val);
		case MachineOperand.OP_ADDR:
			if (mop1.val == 0) {
				return new AstAddressExpression(mop1.immed);
			} else {
				return new AstBinaryExpression(IAstBinaryExpression.K_ADD,
						new AstRegisterExpression(inst.getWp(), mop1.val),
						new AstAddressExpression(mop1.immed));
			}
		case MachineOperand.OP_REG:
		case MachineOperand.OP_REG0_SHIFT_COUNT:
			return new AstRegisterExpression(inst.getWp(), mop1.val);
		case MachineOperand.OP_IND:
			return new AstUnaryExpression(IAstUnaryExpression.K_INDIRECT,
					new AstRegisterExpression(inst.getWp(), mop1.val));
		case MachineOperand.OP_INC:
			// TODO: and add!
			//return new AstBinaryExpression(IAstBinaryExpression.K_COMMA,
			//        new AstBinaryExpression(IAstBinaryExpression.K_ASSIGN,
			//                new AstUnaryExpression(IAstUnaryExpression.K_INDIRECT,
			 //                       new AstRegisterExpression(inst.wp, mop1.val))
			return new AstUnaryExpression(IAstUnaryExpression.K_INDIRECT,
					new AstRegisterExpression(inst.getWp(), mop1.val));

		default:
			Check.checkState(false);
		}
		return null;
	}
	*/
	
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
