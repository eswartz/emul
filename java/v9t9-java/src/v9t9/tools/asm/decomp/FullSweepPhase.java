/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.asm.decomp;

import java.util.Iterator;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.engine.cpu.BaseMachineOperand;
import v9t9.engine.cpu.Inst9900;
import v9t9.engine.cpu.InstInfo;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.asm.common.LabelOperand;
import v9t9.tools.asm.common.MemoryRange;

/**
 * Full sweep phase.  Assume everything in memory is potential
 * code, and figure out from there what really is.
 * 
 * Cconvert machine instructions into higher-level instructions.<p>
 * (1) Replace addresses/offsets with labels
 * <p>
 * (2) Replace memory references with register references 
 * <p>
 * @author ejs
 */
public class FullSweepPhase extends Phase {

    FullSweepPhase(MemoryDomain mainMemory, IDecompileInfo info) {
        super(mainMemory, info);
    }
    

    /**
     * Find labels, i.e. branch/blwp targets
     *
     */
    public void findLabels() {
        for (Iterator<MemoryRange> iter = decompileInfo.getMemoryRanges().rangeIterator(); iter.hasNext();) {
            MemoryRange range = iter.next();
            if (!range.isCode()) {
				continue;
			}

            for (HighLevelInstruction inst = (HighLevelInstruction) range.getCode(); inst != null; inst = inst.getNext()) {
                Label label;

                if (inst.getInst().getInst() == Inst9900.Ibl || inst.getInst().getInst() == Inst9900.Ib || inst.getInst().getInst() == Inst9900.Iblwp
                    || inst.getInst().getInst() == Inst9900.Ijmp || inst.getInst().info.jump == InstInfo.INST_JUMP_COND) 
                {
                    label = null;
                    if (operandIsLabel(inst, (MachineOperand) inst.getInst().getOp1())) {
                        if (inst.getInst().getInst() == Inst9900.Iblwp) {
                            // need to read vector
                            if (((BaseMachineOperand)inst.getInst().getOp1()).type == MachineOperand9900.OP_ADDR) {
                                int vecaddr = operandEffectiveAddress(inst, (MachineOperand) inst.getInst().getOp1());
                                
                                Routine routine = addPossibleContextSwitch(vecaddr, null);
                                if (routine != null) {
									label = routine.getMainLabel();
								}
                                /*
                                int wp = CPU.readWord(vecaddr);
                                int addr = CPU.readWord(vecaddr+2);
                            
                                System.out.printf("Adding BLWP vector at >%04X\n", addr);
                                label = findOrAddLabel(addr, 
                                              false,
                                              inst.getInst().pc,
                                              null, 
                                              new ContextSwitchRoutine(wp));
                                              */
                                if (label != null) {
									inst.getInst().setOp1(new LabelOperand(label));
								}
                                
                            }
                        } else {
                            // normal label
                            int addr = operandEffectiveAddress(inst, (MachineOperand) inst.getInst().getOp1());

                            if (inst.getInst().getInst() == Inst9900.Ibl) {
                                Routine routine = addRoutine(addr, null, new LinkedRoutine());
                                label = routine.getMainLabel();
                            } else {
                            	label = decompileInfo.findOrCreateLabel(addr);
                            }
                            
                            inst.getInst().setOp1(new LabelOperand(label));
                        }
                    }

/*
                    if (op_isa_label(&inst.getInst().op2)) {
                        l = add_label(op_ea(&inst.getInst().op2), 
                                      op_is_rel(inst, &inst.getInst().op2), 
                                      inst.getInst().pc,
                                      0L);

                        if (inst.getInst().inst == Ibl || inst.getInst().inst == Iblwp)
                            l.func = true;

                        obj = (Object*)xcalloc(sizeof(Object));
                        obj.type = OBJ_LABEL;
                        obj.u.label = l;
                        obj.name = l.name;
                        inst.getInst().op2.obj = obj;
                    }
    */
                }
            }
        }
    }

    /*
     *  Traverse the instructions and make a blocklist
     * (using instructions from mem_range.code[tail] and
     * adding to mem_range.block[tail])
     */
    private void findBlocks() { 
        /*
        for (Iterator iter = dc.rangeIterator(); iter.hasNext();) {
            Range range = (Range) iter.next();
            if (!range.isCode()) 
                continue;

            Block curblock = null;

            LLInstruction prev = null;
            for (LLInstruction inst = range.code; inst != null; inst = inst.getInst().next) {
                Label label = getLabel(inst.getInst().pc);
                
                // break if this is a label, there is no block yet,
                // or the previous instruction jumps (but not as a subroutine)
                if (label != null) {
                    if (curblock == null
                    || (prev != null && prev.jump != LLInstruction.INST_JUMP_FALSE 
                        && prev.inst != LLInstruction.Ibl 
                        && prev.inst != LLInstruction.Iblwp)) {
                        // new block
                        Block n = new Block();
                        n.id = blockid++;
                        blocks.add(n);
                        curblock = n;
                        curblock.first = inst;
                    }
    
                    // this address is presumably branched to
                    label.block = curblock;
                    curblock.label = label;
                }
    
                inst.getInst().block = curblock;
                curblock.last = inst;
                prev = inst;
            }
            
            
            
            // to terminate the range
            blocks.add(null);
        }
        */

    	/*
        // make a block at every label
        for (Object element : labels.values()) {
            Label label = (Label) element;
            LLInstruction inst = decompileInfo.getLLInstructions().get(new Integer(label.getAddr()));
            if (inst != null) {
                Block n = new Block(inst);
                getBlocks().add(n);
                label.llll(n);
            }
        }*/
        
        // now assign blocks to all instructions, adding new blocks
        // where no labels reach 
        Block curblock = null;
        for (Iterator<HighLevelInstruction> iter = decompileInfo.getLLInstructions().values().iterator(); iter.hasNext();) {
            HighLevelInstruction inst = iter.next();
            if (inst.getBlock() != null) {
                curblock = inst.getBlock();
            } else if (curblock == null) {
                curblock = new Block(inst);
               	addBlock(curblock);
            } else {
            	curblock.addInst(inst);
            }
            if ((inst.flags & HighLevelInstruction.fIsBranch) != 0) {
				curblock = null;
			}
        }
    }

     //  For each block, figure out the 
    //  successors/predecessors lists 
    
    void getFlowgraph()
    {
        for (Object element : getBlocks()) {
            Block block = (Block) element;
            if (block != null) {
                block.succ.clear();
                block.pred.clear();
            }
        }
    
        for (Object element : getBlocks()) {
            Block block = (Block) element;
            if (block == null) {
				continue;
			}
            HighLevelInstruction inst = block.getLast();
            inst = block.getLast();
            if ((inst.flags & HighLevelInstruction.fIsBranch+HighLevelInstruction.fIsCondBranch) != 0) {
                if ((inst.flags & HighLevelInstruction.fIsCall) == 0) {
                    // jump?
                    if (inst.getInst().getOp1() instanceof LabelOperand)
                    {
                        Label label = ((LabelOperand) inst.getInst().getOp1()).label;
                        if (label.getBlock() != null) {
							block.addSucc(label.getBlock());
						} else {
							System.out.printf( "??? Ignoring branch to label %s from >%04X\n", label.getName(), inst.getInst().pc);
						}
                    }
                    if (inst.getInst().getOp2() instanceof LabelOperand)
                    {
                        Label label = ((LabelOperand) inst.getInst().getOp2()).label;
                        if (label.getBlock() != null) {
							block.addSucc(label.getBlock());
						} else {
							System.out.printf( "??? Ignoring branch to label %s from >%04X\n", label.getName(), inst.getInst().pc);
						}
                    }
                }

                // fallthrough?
                if ((inst.flags & HighLevelInstruction.fIsCondBranch) != 0 
                    || (inst.flags & HighLevelInstruction.fIsCall+HighLevelInstruction.fIsCall) == HighLevelInstruction.fIsCall+HighLevelInstruction.fIsCall)
                {
                    if (inst.getNext() != null && inst.getNext().getBlock() != null) {
						block.addSucc(inst.getNext().getBlock());
					} else {
						System.out.printf("??? Ignoring fallthrough after >%04X\n", inst.getInst().pc);
					}
                }
            }
            else {
                // normal fall through
                if (inst.getNext() != null && inst.getNext().getBlock() != null) {
					block.addSucc(inst.getNext().getBlock());
				} else {
					System.out.printf("??? Ignoring fallthrough after >%04X\n", inst.getInst().pc);
				}
            }
        }
    }
    
    /**
     * Flow the WP value through the tree
     */
    void flowWP() {
        for (Object element : getBlocks()) {
            Block block = (Block) element;
            block.setFlags(block.getFlags() & (~Block.fVisited));
        }

        boolean changed = false;
        for (Object element : getRoutines()) {
            Routine routine = (Routine) element;
            if (routine instanceof ContextSwitchRoutine) {
                ContextSwitchRoutine ctx = (ContextSwitchRoutine) routine;
                for (Block block : routine.getEntries()) {
                	changed |= flowWP(block, ctx.getWp());
                }
            }
        }
    }
    
    private boolean flowWP(Block block, short wp) {
        if (block == null) {
			return false;
		}
        
        if ((block.getFlags() & Block.fVisited) != 0) {
            if (wp != block.getFirst().getWp()) {
                System.out.println("!!! mismatched WP at " + block.getFirst() + 
                        " (stored: " + HexUtils.toHex4(block.getFirst().getWp()) +", new: " 
                        + HexUtils.toHex4(wp));
            }
            return false;
        }

        block.setFlags(block.getFlags() | Block.fVisited);

        for (HighLevelInstruction inst = block.getFirst(); inst != null; inst = inst.getNext()) {
            // TODO: watch for self-modifying memory!
            if (inst.getInst().getInst() == Inst9900.Ilwpi) {
				wp = ((BaseMachineOperand)inst.getInst().getOp1()).immed;
			}
            
            inst.setWp(wp);
            
            for (Object element : block.succ) {
                Block succ = (Block) element;
                flowWP(succ, wp);
            }
            if (inst == block.getLast()) {
				break;
			}
        }
        return true;
    }

    public void run() {
        
        addStandardROMRoutines();
        findLabels();
        
        findBlocks();
        
        getFlowgraph();

        flowWP();
        
        //dumpInstructions();
        //dumpBlocks();
    }
}
