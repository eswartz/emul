/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 22, 2006
 *
 */
package v9t9.tools.decomp;

import java.util.Iterator;

import org.ejs.emul.core.utils.HexUtils;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.llinst.Block;
import v9t9.tools.llinst.ContextSwitchRoutine;
import v9t9.tools.llinst.HighLevelInstruction;
import v9t9.tools.llinst.Label;
import v9t9.tools.llinst.LabelOperand;
import v9t9.tools.llinst.LinkedRoutine;
import v9t9.tools.llinst.MemoryRange;
import v9t9.tools.llinst.Phase;
import v9t9.tools.llinst.Routine;

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

    FullSweepPhase(MemoryDomain cpu, IDecompileInfo info) {
        super(cpu, info);
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

                if (inst.inst == InstructionTable.Ibl || inst.inst == InstructionTable.Ib || inst.inst == InstructionTable.Iblwp
                    || inst.inst == InstructionTable.Ijmp || inst.jump == Instruction.INST_JUMP_COND) 
                {
                    label = null;
                    if (operandIsLabel(inst, (MachineOperand) inst.op1)) {
                        if (inst.inst == InstructionTable.Iblwp) {
                            // need to read vector
                            if (((MachineOperand)inst.op1).type == MachineOperand.OP_ADDR) {
                                int vecaddr = operandEffectiveAddress(inst, (MachineOperand) inst.op1);
                                
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
                                              inst.pc,
                                              null, 
                                              new ContextSwitchRoutine(wp));
                                              */
                                if (label != null) {
									inst.op1 = new LabelOperand(label);
								}
                                
                            }
                        } else {
                            // normal label
                            int addr = operandEffectiveAddress(inst, (MachineOperand) inst.op1);

                            if (inst.inst == InstructionTable.Ibl) {
                                Routine routine = addRoutine(addr, null, new LinkedRoutine());
                                label = routine.getMainLabel();
                            } else {
                            	label = decompileInfo.findOrCreateLabel(addr);
                            }
                            
                            inst.op1 = new LabelOperand(label);
                        }
                    }

/*
                    if (op_isa_label(&inst.op2)) {
                        l = add_label(op_ea(&inst.op2), 
                                      op_is_rel(inst, &inst.op2), 
                                      inst.pc,
                                      0L);

                        if (inst.inst == Ibl || inst.inst == Iblwp)
                            l.func = true;

                        obj = (Object*)xcalloc(sizeof(Object));
                        obj.type = OBJ_LABEL;
                        obj.u.label = l;
                        obj.name = l.name;
                        inst.op2.obj = obj;
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
            for (LLInstruction inst = range.code; inst != null; inst = inst.next) {
                Label label = getLabel(inst.pc);
                
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
    
                inst.block = curblock;
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
                getBlocks().add(curblock);
            } else {
                inst.setBlock(curblock);
            }
            curblock.setLast(inst);
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
                    if (inst.op1 instanceof LabelOperand)
                    {
                        Label label = ((LabelOperand) inst.op1).label;
                        if (label.getBlock() != null) {
							block.addSucc(label.getBlock());
						} else {
							System.out.printf( "??? Ignoring branch to label %s from >%04X\n", label.getName(), inst.pc);
						}
                    }
                    if (inst.op2 instanceof LabelOperand)
                    {
                        Label label = ((LabelOperand) inst.op2).label;
                        if (label.getBlock() != null) {
							block.addSucc(label.getBlock());
						} else {
							System.out.printf( "??? Ignoring branch to label %s from >%04X\n", label.getName(), inst.pc);
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
						System.out.printf("??? Ignoring fallthrough after >%04X\n", inst.pc);
					}
                }
            }
            else {
                // normal fall through
                if (inst.getNext() != null && inst.getNext().getBlock() != null) {
					block.addSucc(inst.getNext().getBlock());
				} else {
					System.out.printf("??? Ignoring fallthrough after >%04X\n", inst.pc);
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
            if (inst.inst == InstructionTable.Ilwpi) {
				wp = ((MachineOperand)inst.op1).immed;
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
