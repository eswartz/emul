/**
 * 
 */
package v9t9.engine;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import v9t9.engine.cpu.Instruction9900;
import v9t9.engine.cpu.InstTable9900;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.asm.common.MemoryRanges;
import v9t9.tools.asm.decomp.Block;
import v9t9.tools.asm.decomp.HighLevelInstruction;
import v9t9.tools.asm.decomp.IDecompileInfo;
import v9t9.tools.asm.decomp.Label;
import v9t9.tools.asm.decomp.Routine;
import v9t9.tools.asm.decomp.TopDownPhase;

/**
 * An instance of this class stores all the known information 
 * about a certain collection of code -- discovered entry points, blocks, routines, etc.
 * @author ejs
 *
 */
public class HighLevelCodeInfo implements IDecompileInfo {
	//Cpu cpu;
	MemoryDomain domain;
    /** dimensions */
    //int addr;
    //int size;
    /** instruction list */
    Map<Integer, Instruction9900> instructions;
    /** LL instruction list */
    Map<Integer, HighLevelInstruction> llInstructions;
    //List<Instruction> instructions;
	private MemoryRanges memoryRanges;
	private TreeMap<Block, Label> labelMap;
	protected Map<Integer, Block> blockMap;
	protected Map<Label, Routine> routineMap;
	private TopDownPhase phase;
	private boolean scanned;

	public HighLevelCodeInfo(MemoryDomain domain) {
		this.domain = domain;
		//this.ent = entry;
		//this.addr = addr;
		//this.size = size;
		this.instructions = new TreeMap<Integer, Instruction9900>();
		this.blockMap = new TreeMap<Integer, Block>();
        this.memoryRanges = new MemoryRanges();
		this.llInstructions = new TreeMap<Integer, HighLevelInstruction>();
		this.labelMap = new TreeMap<Block, Label>();
		this.routineMap = new TreeMap<Label, Routine>();
		
		//memoryRanges.addRange(addr, size, true);

		this.scanned = false;
	}
	
	/** Get the instruction for the given PC */
	public Instruction9900 getInstruction(int pc) {
		pc &= 0xfffe;
		Instruction9900 ins = instructions.get(pc);
		if (ins == null) {
			short op = domain.readWord(pc);
			ins = new Instruction9900(InstTable9900.decodeInstruction(op, pc, domain));
			instructions.put(pc, ins);
		}
		return ins;
	}

	/** Test method */
	public void addInstruction(HighLevelInstruction inst) {
		instructions.put(inst.pc & 0xfffe, inst);
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
	
	public Map<Integer, HighLevelInstruction> getLLInstructions() {
		return llInstructions;
	}

	public HighLevelInstruction disassemble(int startAddr, int size) {
		memoryRanges.addRange(startAddr, size, true);
		
		HighLevelInstruction first = null;
		HighLevelInstruction prev = null;
		for (int addr = startAddr; addr < startAddr + size; addr += 2) {
			short op = domain.readWord(addr);
			HighLevelInstruction inst = new HighLevelInstruction(0, new Instruction9900(InstTable9900.decodeInstruction(op, addr, domain)));
			getLLInstructions().put(new Integer(inst.pc), inst);
			if (prev != null) {
				prev.setNext(inst);
			} else {
				first = inst;
			}
			prev = inst;
		}

		// wire up instructions to their next real instructions
		for (HighLevelInstruction inst = first; inst != null; inst = inst.getNext()) {
			if (inst.size > 2) {
				inst.setNext(getLLInstructions().get(new Integer(inst.pc + inst.size)));
			}
		}
		
		return first;
	}
	
	/** Detect the structure of the code using the current memory ranges.
	 * Does not delete any information. */  
	public void analyze() {
		if (!scanned) {
			scanned = true;
			
			phase = new TopDownPhase(domain, this);
				
			phase.addStandardROMRoutines();
				
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
		addr &= 0xfffe;
		HighLevelInstruction inst = getLLInstructions().get(addr);
		if (inst == null) {
			inst = new HighLevelInstruction(0, getInstruction(addr));
			llInstructions.put(addr, inst);
		}
		org.ejs.coffee.core.utils.Check.checkState((inst != null));
		Block block = inst.getBlock();
		if (block != null) {
			Label label = labelMap.get(block);
			return label;
		}
		return null;
	}
	
	public Label findOrCreateLabel(int addr) {
		// first, see if a block exists here
		addr &= 0xfffe;
		HighLevelInstruction inst = getLLInstructions().get(addr);
		if (inst == null) {
			inst = new HighLevelInstruction(0, getInstruction(addr));
			llInstructions.put(addr, inst);
		}
		org.ejs.coffee.core.utils.Check.checkState((inst != null));
		inst.flags |= HighLevelInstruction.fStartsBlock;
		
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
	
	public void replaceInstruction(HighLevelInstruction inst) {
		instructions.put(inst.pc, inst);
	}
}
