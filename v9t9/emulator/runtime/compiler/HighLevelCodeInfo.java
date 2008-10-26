/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.decomp.IDecompileInfo;
import v9t9.tools.decomp.TopDownPhase;
import v9t9.tools.llinst.Block;
import v9t9.tools.llinst.LLInstruction;
import v9t9.tools.llinst.Label;
import v9t9.tools.llinst.MemoryRanges;
import v9t9.tools.llinst.Routine;
import v9t9.utils.Check;

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
    Map<Integer, Instruction> instructions;
    /** LL instruction list */
    Map<Integer, LLInstruction> llInstructions;
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
		this.instructions = new TreeMap<Integer, Instruction>();
		this.blockMap = new TreeMap<Integer, Block>();
        this.memoryRanges = new MemoryRanges();
		this.llInstructions = new TreeMap<Integer, LLInstruction>();
		this.labelMap = new TreeMap<Block, Label>();
		this.routineMap = new TreeMap<Label, Routine>();
		
		//memoryRanges.addRange(addr, size, true);

		this.scanned = false;
	}
	
	/** Get the instruction for the given PC */
	public Instruction getInstruction(int pc) {
		pc &= 0xfffe;
		Instruction ins = instructions.get(pc);
		if (ins == null) {
			short op = domain.readWord(pc);
			ins = new Instruction(InstructionTable.decodeInstruction(op, pc, domain));
			instructions.put(pc, ins);
		}
		return ins;
	}

	/** Test method */
	public void addInstruction(LLInstruction inst) {
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
	
	public Map<Integer, LLInstruction> getLLInstructions() {
		return llInstructions;
	}

	public LLInstruction disassemble(int startAddr, int size) {
		memoryRanges.addRange(startAddr, size, true);
		
		LLInstruction first = null;
		LLInstruction prev = null;
		for (int addr = startAddr; addr < startAddr + size; addr += 2) {
			short op = domain.readWord(addr);
			LLInstruction inst = new LLInstruction(0, new Instruction(InstructionTable.decodeInstruction(op, addr, domain)));
			getLLInstructions().put(new Integer(inst.pc), inst);
			if (prev != null) {
				prev.setNext(inst);
			} else {
				first = inst;
			}
			prev = inst;
		}

		// wire up instructions to their next real instructions
		for (LLInstruction inst = first; inst != null; inst = inst.getNext()) {
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
		LLInstruction inst = getLLInstructions().get(addr);
		if (inst == null) {
			inst = new LLInstruction(0, getInstruction(addr));
			llInstructions.put(addr, inst);
		}
		Check.checkState(inst != null);
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
		LLInstruction inst = getLLInstructions().get(addr);
		if (inst == null) {
			inst = new LLInstruction(0, getInstruction(addr));
			llInstructions.put(addr, inst);
		}
		Check.checkState(inst != null);
		inst.flags |= LLInstruction.fStartsBlock;
		
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
	
	public void replaceInstruction(LLInstruction inst) {
		instructions.put(inst.pc, inst);
	}
}
