/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 23, 2006
 *
 */
package v9t9.tools.llinst;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ejs.emul.core.utils.HexUtils;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionTable;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.decomp.IDecompileInfo;

public abstract class Phase {
	protected Map<Integer, Block> blocks;
	protected MemoryDomain CPU;
	public IDecompileInfo decompileInfo;
	protected Map<Block, Label> labels;
	protected Map<Label, Routine> routines;
	
	public Phase(MemoryDomain cpu, IDecompileInfo info) {
		this.decompileInfo = info;
		this.CPU = cpu;
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
	public void disassemble() {
		MemoryRange prev = null;
		MemoryRange range = null;
		for (Iterator<MemoryRange> iter = decompileInfo.getMemoryRanges().rangeIterator(); iter
				.hasNext();) {
			range = iter.next();
			if (prev != null && prev.isCode()) {
				HighLevelInstruction first = decompileInfo.disassemble(prev.from, range.from - prev.from);
				prev.setCode(first);
			}
			prev = range;
		}
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
	

	public void dumpInstructions() {
		for (Iterator<MemoryRange> iter = decompileInfo.getMemoryRanges().rangeIterator(); iter
				.hasNext();) {
			MemoryRange range = iter.next();
			for (HighLevelInstruction inst = (HighLevelInstruction) range.getCode(); inst != null; inst = inst.getNext()) {
				dumpInstruction(inst);
			}
		}
	}

	public void dumpInstruction(HighLevelInstruction inst) {
		if (inst.getBlock() != null && inst.getBlock().getFirst() == inst) {
			System.out.println(inst.getBlock().format());
		}
		Label label = getLabel(inst.pc);
		if (label != null) {
			System.out.println(label);
		}
		System.out.print('\t');
		//        System.out.println("WP="+ Utils.toHex4(inst.wp) +" " + inst.format(true, true));
		System.out.println(inst.format(true, true));
	}

	protected Block getLabelKey(int addr) {
		HighLevelInstruction inst = decompileInfo.getLLInstructions().get(addr);
		if (inst == null)
			return null;
		if (inst.getBlock() == null)
			return null;
		if (inst.getBlock().getFirst().pc == addr)
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
	 * @param addr
	 * @param name
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
	

	private void addProgramList(int list) {
		int addr, link;
		char[] nameChars = new char[256];
		int len;
		System.out.printf("Scanning program list at >%04X\n", list);
		while (list != 0) {
			link = CPU.readWord(list);
			addr = CPU.readWord(list + 2);
			if (validCodeAddress(addr)) {
				len = CPU.readByte(list + 4);
				String name = null;
				if (len > 0) {
					for (int i = 0; i < len; i++) {
						nameChars[i] = (char) CPU.readByte(list + 5 + i);
					}
					name = new String(nameChars, 0, len);
				}
				System.out.printf("Adding routine %s at >%04X\n",
						name != null ? name : "<unnamed>", addr);
				addRoutine(addr, name, new LinkedRoutine());
			}
			list = link;
		}

	}

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
		org.ejs.emul.core.utils.Check.checkState(validCodeAddress(addr));
		
		Label label = decompileInfo.findOrCreateLabel(addr);
		if (name != null && label.getName() == null) {
			label.setName(name);
		}
		routine.addEntry(label);
		routines.put(label, routine);
		return routine;
	}

	/**
	 * Add REF/DEF tables, where each entry points to the END of the table
	 *
	 */
	public void addRefDefTables(List<Integer> refDefTables) {

		// Get explicit symbol tables
		for (Object element : refDefTables) {
			int addr = ((Integer) element).intValue();
			MemoryRange range = decompileInfo.getMemoryRanges().getRangeContaining(addr - 1);
			if (range == null) {
				System.err.println("!!! Can't find range containing >"
						+ HexUtils.toHex4((addr - 1)));
				continue;
			}

			int ptr = addr;
			char[] nameChars = new char[6];
			while (true) {
				ptr -= 2;
				addr = CPU.readWord(ptr);
				if (addr == 0) {
					break;
				}
				int length = 6;
				for (int i = 0; i < 6; i++) {
					int pos = 5 - i;
					nameChars[pos] = (char) CPU.readByte(--ptr);
					if (nameChars[pos] == ' ') {
						length = pos;
					}
				}

				// now, these are almost always vectors, so take the PC
				String name = new String(nameChars, 0, length);
				short wp = CPU.readWord(addr);
				addr = CPU.readWord(addr + 2);
				if (validCodeAddress(addr)) {
					System.out.println("Adding label " + name + " at >"
							+ HexUtils.toHex4(addr));
					addRoutine(addr, name, new ContextSwitchRoutine(wp));
				}
			}
		}
	}

	public void addStandardROMRoutines() {
		// Get standard entries
		for (int addr = 0; addr < 0x10000; addr += 0x2000) {
			if (CPU.readByte(addr) == (byte) 0xaa) {
				System.out.println("Scanning standard header at >"
						+ HexUtils.toHex4(addr));
				int paddr = CPU.readWord(addr + 4);
				addProgramList(paddr);
				paddr = CPU.readWord(addr + 6);
				addProgramList(paddr);
				paddr = CPU.readWord(addr + 8);
				addProgramList(paddr);
				paddr = CPU.readWord(addr + 10);
				addProgramList(paddr);
			}

			if (addr == 0) {

				addPossibleContextSwitch(0, "RESET");

				// int1
				addPossibleContextSwitch(4, "INT1");

				// int2
				addPossibleContextSwitch(8, "INT2");

				for (int xop = 0; xop < 2; xop++) {
					// XOP
					addPossibleContextSwitch(0x40 + xop * 4, "XOP" + xop);
				}
			}
		}
	}

	protected Routine addPossibleContextSwitch(int ctx, String name) {
		short wp = CPU.readWord(ctx);
		int addr = CPU.readWord(ctx + 2);
		if (wp == (short) addr || wp == ctx) {
			return null;
		}
		if (CPU.hasRamAccess(wp) && CPU.hasRamAccess(wp + 31)
				&& (addr & 1) == 0
				&& validCodeAddress(addr)) {
			System.out.println("Adding " + name + " vector at >"
					+ HexUtils.toHex4(addr));
			Routine routine = addRoutine(addr, name, new ContextSwitchRoutine(
					wp));
			return routine;
		}
		return null;
	}

	public short operandEffectiveAddress(HighLevelInstruction inst, MachineOperand mop) {
		// PC and WP are not used
		return mop.getEA(CPU, inst.pc, inst.getWp());
	}

	public boolean operandIsLabel(HighLevelInstruction inst, MachineOperand mop) {
		return mop.isLabel()
				&& decompileInfo.getMemoryRanges().getRangeContaining(operandEffectiveAddress(
						inst, mop)) != null;
	}

	//  operand is relocatable if it's in our memory
	//  and is a direct address, a jump target, or
	//  a nontrivial register indirect (a likely lookup table)
	public boolean operandIsRelocatable(HighLevelInstruction inst, MachineOperand mop) {
		if (inst.inst == InstructionTable.Ilwpi) {
			return true;
		}
		if (!(mop instanceof MachineOperand)) {
			return false;
		}
		return (mop.type == MachineOperand.OP_ADDR
				&& (mop.val == 0 || mop.immed >= 0x20) || mop.type == MachineOperand.OP_JUMP)
				&& decompileInfo.getMemoryRanges().getRangeContaining(operandEffectiveAddress(
						inst, mop)) != null;

	}

	protected boolean validCodeAddress(int addr) {
		MemoryRange range = decompileInfo.getMemoryRanges().getRangeContaining(addr);
		if (range == null) {
			return false;
		}
		if (!range.isCode()) {
			return false;
		}
		Instruction inst = decompileInfo.getInstruction(new Integer(addr));
		if (inst == null) {
			return false;
		}
		if (inst.inst == InstructionTable.Idata) {
			return false;
		}
		return true;
	}

	public void dumpLabels() {
		for (Object element : labels.values()) {
			Label label = (Label) element;
			System.out.println(label);
		}
	}

	public void dumpBlocks() {
		for (Object element : getBlocks()) {
			Block block = (Block) element;
			dumpBlock(block);
		}
	}

	public void dumpRoutines() {
		for (Object element : getRoutines()) {
			Routine routine = (Routine) element;
			System.out.print("routine: " + routine);
			if ((routine.flags & Routine.fSubroutine) != 0)
				System.out.print(" [subroutine]");
			if ((routine.flags & Routine.fUnknownExit) != 0)
				System.out.print(" [unknownExit]");
			System.out.println();

			Collection<Block> blocks = routine.getSpannedBlocks();
			System.out.print("blocks = [");
			for (Block block : blocks) {
				System.out.print(block.getId() + " ");
			}
			System.out.println("]");
			for (Block block : blocks) {
				dumpBlock(block);
			}
			System.out.println("-------------------");

		}
	}

	public void dumpBlock(Block block) {
		for (Iterator<HighLevelInstruction> iter = block.iterator(); iter.hasNext();) {
			HighLevelInstruction inst = iter.next();
			dumpInstruction(inst);
		}
		System.out.println();
	}

	public void addBlock(Block block) {
		blocks.put(block.getFirst().pc, block);
	}
	public Set<Block> getBlocks() {
		return new TreeSet<Block>(blocks.values());
	}

	public Collection<Routine> getRoutines() {
		return routines.values();
	}

}
