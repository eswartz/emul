/*
  HighLevelCodeInfo.java

  (c) 2022 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.asm;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import ejs.base.utils.Check;


import v9t9.common.asm.Block;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IInstructionFactory;
import v9t9.common.asm.Label;
import v9t9.common.asm.MemoryRanges;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.Routine;
import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.ti99.asm.HighLevelInstruction;

/**
 * An instance of this class stores all the known information 
 * about a certain collection of code -- discovered entry points, blocks, routines, etc.
 * @author ejs
 *
 */
public class HighLevelCodeInfoF99b implements IDecompileInfo {
	//Cpu cpu;
	IMemoryDomain domain;
    /** instruction list */
    Map<Integer, RawInstruction> instructions;
    /** LL instruction list */
    Map<Integer, IHighLevelInstruction> llInstructions;
    //List<Instruction> instructions;
	private MemoryRanges memoryRanges;
	private TreeMap<Block, Label> labelMap;
	protected Map<Integer, Block> blockMap;
	protected Map<Label, Routine> routineMap;
	private TopDownPhaseF99b phase;
	private boolean scanned;
	private ICpuState state;
	private final IInstructionFactory instructionFactory;

	public HighLevelCodeInfoF99b(ICpuState state, IInstructionFactory instructionFactory) {
		this.state = state;
		this.instructionFactory = instructionFactory;
		this.domain = state.getConsole();
		//this.ent = entry;
		//this.addr = addr;
		//this.size = size;
		this.instructions = new TreeMap<Integer, RawInstruction>();
		this.blockMap = new TreeMap<Integer, Block>();
        this.memoryRanges = new MemoryRanges();
		this.llInstructions = new TreeMap<Integer, IHighLevelInstruction>();
		this.labelMap = new TreeMap<Block, Label>();
		this.routineMap = new TreeMap<Label, Routine>();
		
		//memoryRanges.addRange(addr, size, true);

		this.scanned = false;
	}
	
	/** Get the instruction for the given PC */
	public RawInstruction getInstruction(int pc) {
		pc &= 0xfffe;
		RawInstruction ins = instructions.get(pc);
		if (ins == null) {
			ins = instructionFactory.decodeInstruction(pc, domain);
			instructions.put(pc, ins);
		}
		return ins;
	}

	/** Test method */
	public void addInstruction(IHighLevelInstruction inst) {
		instructions.put(inst.getInst().pc & 0xfffe, inst.getInst());
	}

	/**
	 * Examine the call site.  
	 * For now, see how many words are consumed by it
	 * @param block current block containing the call
	 * @param caller instruction with call
	 * @param target PC of call target
	 */
/*	
	public FunctionInfo getFunctionInfoFromCall(Instruction caller, short target) {
		int type = caller.inst == Instruction.Ibl 
			? FunctionInfo.FUNCTION_BL : FunctionInfo.FUNCTION_BLWP;
        FunctionInfo fi = getFunction(target, type);
        if (fi == null) {
            int params = 0;
            int reg = type == FunctionInfo.FUNCTION_BL ? 11 : 14;
        
            // look for uses of parameter words; ignore any branching
            for (Iterator<Instruction> iter = iterateInstructions(target); iter.hasNext();) {
                Instruction inst = iter.next();
                MachineOperand mop1 = (MachineOperand) inst.op1;
                // return?
                if (inst.inst == Instruction.Ib && mop1.type == MachineOperand.OP_IND && mop1.val == 11) {
					break;
				}
                if (inst.inst == Instruction.Irtwp) {
					break;
				}
                if (mop1.isMemory() && mop1.type == MachineOperand.OP_INC 
                        && mop1.val == reg) {
                    params++;
                }
            }
            
            if (params > 0) {
				System.out.println("call site " + Utils.toHex4(target) + " seems to use " + params + " data words");
			}
            registerFunction(type, target, params);
        }
        return fi;
        
    }*/

	/** Get an instruction iterator from the PC */
	/*
	public Iterator<Instruction> iterateInstructions(final short pc) {
		return new Iterator<Instruction>() {
			int nextAddr = pc;
			int end = addr + size;

			public boolean hasNext() {
				return nextAddr < end;
			}

			public Instruction next() {
				int addr = nextAddr;
				Instruction ins = getInstruction(addr);
				nextAddr += ins.size;
				return ins;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}*/

	public MemoryRanges getMemoryRanges() {
		return memoryRanges;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.asm.IDecompileInfo#getInstructions()
	 */
	@Override
	public Map<Integer, RawInstruction> getInstructions() {
		return instructions;
	}
	public Map<Integer, Block> getBlockMap() {
		return blockMap;
	}
	
	public Map<Block, Label> getLabelMap() {
		return labelMap;
	}

	public Collection<Routine> getRoutines() {
		return routineMap.values();
	}

	public Map<Label, Routine> getRoutineMap() {
		return routineMap;
	}
	
	public Collection<Block> getBlocks() {
		return blockMap.values();
	}
	
	public Collection<Label> getLabels() {
		return labelMap.values();
	}
	
	public Map<Integer, IHighLevelInstruction> getLLInstructions() {
		return llInstructions;
	}

	public IHighLevelInstruction disassemble(int startAddr, int size) {
		memoryRanges.addRange(startAddr, size, true);
		
		IHighLevelInstruction first = null;
		IHighLevelInstruction prev = null;
		for (int addr = startAddr; addr < startAddr + size; addr++) {
			IHighLevelInstruction inst = getLLInstructions().get(addr);
			if (inst == null) {
				RawInstruction rawInst = instructionFactory.decodeInstruction(addr, domain);
				inst = new HighLevelInstruction(0,
						rawInst,
						instructionFactory.getInstructionFlags(rawInst));
				getLLInstructions().put(Integer.valueOf(inst.getInst().pc), inst);
			}
			if (prev != null) {
				prev.setPhysicalNext(inst);
			} else {
				first = inst;
			}
			prev = inst;
		}

		/*
		// wire up instructions to their next real instructions
		for (IHighLevelInstruction inst : getLLInstructions().values()) { 
			if (inst.getInst().getSize() > 2) {
				inst.setLogicalNext(getLLInstructions().get(Integer.valueOf(inst.getInst().pc + inst.getInst().getSize())));
			}
		}
		*/
		
		return first;
	}
	
	/** Detect the structure of the code using the current memory ranges.
	 * Does not delete any information. */  
	public void analyze() {
		if (!scanned) {
			scanned = true;
			
			phase = new TopDownPhaseF99b(state, this);
			phase.run();
		}
	}
	
	public void markDirty() {
		this.scanned = false;
	}

	public IDecompileInfo getDecompileInfo() {
		return this;
	}

	public Label getLabel(int addr) {
		// first, see if a block exists here
		IHighLevelInstruction inst = getLLInstructions().get(addr);
		if (inst == null) {
			RawInstruction rawInst = getInstruction(addr);
			inst = new HighLevelInstruction(0, rawInst, instructionFactory.getInstructionFlags(rawInst));
			llInstructions.put(addr, inst);
		}
		Check.checkState((inst != null));
		Block block = inst.getBlock();
		if (block != null) {
			Label label = labelMap.get(block);
			return label;
		}
		return null;
	}
	
	public Label findOrCreateLabel(int addr) {
		// first, see if a block exists here
		IHighLevelInstruction inst = getLLInstructions().get(addr);
		if (inst == null) {
			RawInstruction rawInst = getInstruction(addr);
			inst = new HighLevelInstruction(0, getInstruction(addr), instructionFactory.getInstructionFlags(rawInst));
			llInstructions.put(addr, inst);
		}
		Check.checkState((inst != null));
		inst.setFlags(inst.getFlags() | IHighLevelInstruction.fStartsBlock);
		
		Block block = inst.getBlock();
		if (block == null) {
			// make block
			block = new Block(inst);
			blockMap.put(addr, block);
		}
		
		Label label = labelMap.get(block);
		if (label == null) {
			label = new Label(block, null);
			labelMap.put(block, label);
		}
		return label;
	}
	
	public void replaceInstruction(IHighLevelInstruction inst) {
		instructions.put(inst.getInst().pc, inst.getInst());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IDecompileInfo#reset()
	 */
	@Override
	public void reset() {
		instructions.clear();
		llInstructions.clear();
		labelMap.clear();
		routineMap.clear();
		blockMap.clear();
	}
}
