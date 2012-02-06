/**
 * 
 */
package v9t9.common.asm;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ejs.base.utils.Check;

import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author ejs
 *
 */
public abstract class BasePhase {

	protected Map<Integer, Block> blocks;
	protected IMemoryDomain mainMemory;
	public IDecompileInfo decompileInfo;
	protected Map<Block, Label> labels;
	protected Map<Label, Routine> routines;
	protected ICpuState state;

	public BasePhase(ICpuState state, IDecompileInfo info) {
		this.decompileInfo = info;
		this.state = state;
		this.mainMemory = state.getConsole();
		//this.setBlocks(new TreeSet<Block>());
		blocks = info.getBlockMap();
		labels = info.getLabelMap();
		routines = info.getRoutineMap();
		//blocks = new TreeMap<Integer, Block>();
		//labels = new TreeMap<Block, Label>();
		//this.labels = codeProvider.getLabelMap();
		//labels = info.getLabelMap();
		//this.routines = codeProvider.getRoutineMap();
		//this.routines = info.getRoutines();
		//routines = new TreeMap<Label, Routine>();
	}
	

	public Map<Block, Label> getLabels() {
		return labels;
	}

	/**
	 * Disassemble all the code.  Required after adding ranges.
	 *
	 */
	public Collection<MemoryRange> disassemble() {
		List<MemoryRange> ranges = new ArrayList<MemoryRange>();
		MemoryRange prev = null;
		MemoryRange range = null;
		for (Iterator<MemoryRange> iter = decompileInfo.getMemoryRanges().rangeIterator(); iter
				.hasNext();) {
			range = iter.next();
			if (prev != null && prev.isCode()) {
				IHighLevelInstruction first = decompileInfo.disassemble(prev.from, range.from - prev.from);
				prev.setCode(first);
				ranges.add(prev);
			}
			prev = range;
		}
		return ranges;
	}
/*
 public MemoryDomain getCPUMemory() {
        return CPU;
    }

    public void addInstruction(LLInstruction inst) {
        instructionMap.put(new Integer(inst.pc), inst);
    }

    public LLInstruction getInstruction(int addr) {
        return instructionMap.get(new Integer(addr));
    }

    public Iterator<LLInstruction> instructionIterator() {
        return instructionMap.values().iterator();
    }

    public MemoryRanges getRanges() {
        return ranges;
    }
 */
	

	public void dumpInstructions(PrintStream os) {
		for (Iterator<MemoryRange> iter = decompileInfo.getMemoryRanges().rangeIterator(); iter
				.hasNext();) {
			MemoryRange range = iter.next();
			for (IHighLevelInstruction inst = (IHighLevelInstruction) range.getCode(); inst != null; inst = inst.getNext()) {
				dumpInstruction(os, inst);
			}
		}
	}

	public void dumpInstructions(PrintStream os, Collection<MemoryRange> ranges) {
		for (MemoryRange range : ranges) {
			for (IHighLevelInstruction inst = range.getCode(); inst != null; inst = inst.getNext()) {
				dumpInstruction(os, inst);
			}
		}
	}

	public void dumpInstruction(PrintStream os, IHighLevelInstruction inst) {
		if (inst.getBlock() != null && inst.getBlock().getFirst() == inst) {
			os.println(inst.getBlock().format());
		}
		Label label = getLabel(inst.getInst().pc);
		if (label != null) {
			os.println(label);
		}
		os.print('\t');
		//        os.println("WP="+ Utils.toHex4(inst.wp) +" " + inst.format(true, true));
		os.println(inst.format(true, true));
	}

	protected Block getLabelKey(int addr) {
		IHighLevelInstruction inst = decompileInfo.getLLInstructions().get(addr);
		if (inst == null)
			return null;
		if (inst.getBlock() == null)
			return null;
		if (inst.getBlock().getFirst().getInst().pc == addr)
			return inst.getBlock();
		return null;
	}

	public Label getLabel(int addr) {
		Block block = getLabelKey(addr);
		if (block == null)
			return null;
		Label label = labels.get(block);
		return label;
	}

	/**
	 * Add a label for the given address.
	 * The label is created with the given parameters,
	 * and a routine is realized by setting its label to this.
	 * If a label already exists, an exception is emitted.
	 * @param bufaddr
	 * @param path
	 * @param routine
	 * @return
	 */
	/*
	public Label addLabel(int addr, boolean rel, int pc, String name) {
		Integer key = getLabelKey(addr);
		Label label = labels.get(key);
		Check.checkArg(label == null);

		
		label = new Label((short) addr, name);
		label.llll(null);
		label.rel = rel;
		label.rels = 0;
		labels.put(key, label);

		return label;
	}
*/


	public Routine getRoutine(int addr) {
		Label label = getLabel(addr);
		if (label == null) {
			return null;
		}
		return routines.get(label);
	}

	/**
	 * Add a routine at the given address.  Any label already existing
	 * here is renamed.
	 * @param addr
	 * @param name
	 * @param routine
	 * @return same incoming routine, updated with a label and added to the routines
	 */
	public Routine addRoutine(int addr, String name, Routine routine) {
		Check.checkState(validCodeAddress(addr));
		
		Label label = decompileInfo.findOrCreateLabel(addr);
		if (name != null && label.getName() == null) {
			label.setName(name);
		}
		routine.addEntry(label);
		routines.put(label, routine);
		return routine;
	}


	public abstract int operandEffectiveAddress(IHighLevelInstruction inst, IMachineOperand mop);

	public boolean operandIsLabel(IHighLevelInstruction inst, IMachineOperand mop) {
		return mop.isLabel()
				&& decompileInfo.getMemoryRanges().getRangeContaining(operandEffectiveAddress(
						inst, mop)) != null;
	}


	public boolean validCodeAddress(int addr) {
		MemoryRange range = decompileInfo.getMemoryRanges().getRangeContaining(addr);
		if (range == null) {
			return false;
		}
		if (!range.isCode()) {
			return false;
		}
		RawInstruction inst = decompileInfo.getInstruction(new Integer(addr));
		if (inst == null) {
			return false;
		}
		if (inst.getInst() == InstTableCommon.Idata) {
			return false;
		}
		return true;
	}


	public void dumpLabels(PrintStream os) {
		for (Object element : labels.values()) {
			Label label = (Label) element;
			os.println(label);
		}
	}

	public void dumpBlocks(PrintStream os) {
		for (Object element : getBlocks()) {
			Block block = (Block) element;
			dumpBlock(os, block);
		}
	}

	public void dumpRoutines(PrintStream os) {
		for (Object element : getRoutines()) {
			Routine routine = (Routine) element;
			os.print("routine: " + routine);
			if ((routine.flags & Routine.fSubroutine) != 0)
				os.print(" [subroutine]");
			if ((routine.flags & Routine.fUnknownExit) != 0)
				os.print(" [unknownExit]");
			os.println();

			Collection<Block> blocks = routine.getSpannedBlocks();
			os.print("blocks = [");
			for (Block block : blocks) {
				os.print(block.getId() + " ");
			}
			os.println("]");
			for (Block block : blocks) {
				dumpBlock(os, block);
			}
			os.println("-------------------");

		}
	}

	public void dumpBlock(PrintStream os, Block block) {
		for (Iterator<IHighLevelInstruction> iter = block.iterator(); iter.hasNext();) {
			IHighLevelInstruction inst = iter.next();
			dumpInstruction(os, inst);
		}
		System.out.println();
	}

	public void addBlock(Block block) {
		blocks.put(block.getFirst().getInst().pc, block);
	}
	public Set<Block> getBlocks() {
		return new TreeSet<Block>(blocks.values());
	}

	public Collection<Routine> getRoutines() {
		return routines.values();
	}

}