/*
  Phase.java

  (c) 2008-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.ti99.asm;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ejs.base.utils.Check;
import ejs.base.utils.HexUtils;


import v9t9.common.asm.Block;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.InstTableCommon;
import v9t9.common.asm.Label;
import v9t9.common.asm.MemoryRange;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.Routine;
import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.machine.ti99.cpu.InstructionWorkBlock9900;
import v9t9.machine.ti99.cpu.MachineOperand9900;

public abstract class Phase implements IDecompilePhase {
	protected Map<Integer, Block> blocks;
	protected IMemoryDomain mainMemory;
	public IDecompileInfo decompileInfo;
	protected Map<Block, Label> labels;
	protected Map<Label, Routine> routines;
	private ICpuState state;
	
	public Phase(ICpuState state, IDecompileInfo info) {
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


    /* (non-Javadoc)
     * @see v9t9.common.asm.IDecompilePhase#getDecompileInfo()
     */
    @Override
    public IDecompileInfo getDecompileInfo() {
    	return decompileInfo;
    	
    }
    
	/* (non-Javadoc)
	 * @see v9t9.common.asm.IDecompilePhase#reset()
	 */
	@Override
	public void reset() {
		decompileInfo.reset();
		blocks.clear();
		labels.clear();
		routines.clear();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#getLabels()
	 */
	@Override
	public Map<Block, Label> getLabels() {
		return labels;
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#disassemble()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#dumpInstructions(java.io.PrintStream)
	 */
	@Override
	public void dumpInstructions(PrintStream os) {
		for (Iterator<MemoryRange> iter = decompileInfo.getMemoryRanges().rangeIterator(); iter
				.hasNext();) {
			MemoryRange range = iter.next();
			for (IHighLevelInstruction inst = (IHighLevelInstruction) range.getCode(); inst != null; 
					inst = inst.getLogicalNext()) {
				dumpInstruction(os, inst);
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#dumpInstructions(java.io.PrintStream, java.util.Collection)
	 */
	@Override
	public void dumpInstructions(PrintStream os, Collection<MemoryRange> ranges) {
		for (MemoryRange range : ranges) {
			for (IHighLevelInstruction inst = range.getCode(); inst != null; inst = inst.getLogicalNext()) {
				dumpInstruction(os, inst);
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#dumpInstruction(java.io.PrintStream, v9t9.common.asm.IHighLevelInstruction)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#getLabel(int)
	 */
	@Override
	public Label getLabel(int addr) {
		Block block = getLabelKey(addr);
		if (block == null)
			return null;
		Label label = labels.get(block);
		return label;
	}

	private void addProgramList(int list) {
		int addr, link;
		char[] nameChars = new char[256];
		int len;
		System.out.printf("Scanning program list at >%04X\n", list);
		while (list != 0) {
			link = mainMemory.readWord(list);
			addr = mainMemory.readWord(list + 2);
			if (validCodeAddress(addr)) {
				len = mainMemory.readByte(list + 4);
				String name = null;
				if (len > 0) {
					for (int i = 0; i < len; i++) {
						nameChars[i] = (char) mainMemory.readByte(list + 5 + i);
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

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#getRoutine(int)
	 */
	@Override
	public Routine getRoutine(int addr) {
		Label label = getLabel(addr);
		if (label == null) {
			return null;
		}
		return routines.get(label);
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#addRoutine(int, java.lang.String, v9t9.common.asm.Routine)
	 */
	@Override
	public Routine addRoutine(int addr, String name, Routine routine) {
		Check.checkState(validCodeAddress(addr));
		
		Label label = decompileInfo.findOrCreateLabel(addr);
		if (label.getAddr() != addr) {
			// instr could be in the middle of valid instructions
			return null;
		}
		
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
				addr = mainMemory.readWord(ptr);
				if (addr == 0) {
					break;
				}
				int length = 6;
				for (int i = 0; i < 6; i++) {
					int pos = 5 - i;
					nameChars[pos] = (char) mainMemory.readByte(--ptr);
					if (nameChars[pos] == ' ') {
						length = pos;
					}
				}

				// now, these are almost always vectors, so take the PC
				String name = new String(nameChars, 0, length);
				short wp = mainMemory.readWord(addr);
				addr = mainMemory.readWord(addr + 2);
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
			if (mainMemory.readByte(addr) == (byte) 0xaa) {
				System.out.println("Scanning standard header at >"
						+ HexUtils.toHex4(addr));
				int paddr = mainMemory.readWord(addr + 4);
				addProgramList(paddr);
				paddr = mainMemory.readWord(addr + 6);
				addProgramList(paddr);
				paddr = mainMemory.readWord(addr + 8);
				addProgramList(paddr);
				paddr = mainMemory.readWord(addr + 10);
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
		short wp = mainMemory.readWord(ctx);
		int addr = mainMemory.readWord(ctx + 2);
		if (wp == (short) addr || wp == ctx) {
			return null;
		}
		if (/*mainMemory.hasRamAccess(wp) && mainMemory.hasRamAccess(wp + 31)
				&&*/ (addr & 1) == 0
				&& validCodeAddress(addr)) {
			System.out.println("Adding " + name + " vector at >"
					+ HexUtils.toHex4(addr));
			Routine routine = addRoutine(addr, name, new ContextSwitchRoutine(
					wp));
			return routine;
		}
		return null;
	}

	public short operandEffectiveAddress(IHighLevelInstruction inst, IMachineOperand mop) {
		InstructionWorkBlock9900 block = new InstructionWorkBlock9900(state);
		block.inst = inst.getInst();
		return mop.getEA(block);
	}

	public boolean operandIsLabel(IHighLevelInstruction inst, IMachineOperand mop) {
		return mop.isLabel()
				&& decompileInfo.getMemoryRanges().getRangeContaining(operandEffectiveAddress(
						inst, mop)) != null;
	}

	//  operand is relocatable if it's in our memory
	//  and is a direct address, a jump target, or
	//  a nontrivial register indirect (a likely lookup table)
	public boolean operandIsRelocatable(IHighLevelInstruction inst, MachineOperand9900 mop) {
		if (inst.getInst().getInst() == Inst9900.Ilwpi) {
			return true;
		}
		if (!(mop instanceof IMachineOperand)) {
			return false;
		}
		return (mop.type == MachineOperand9900.OP_ADDR
				&& (mop.val == 0 || mop.immed >= 0x20) || mop.type == MachineOperand9900.OP_JUMP)
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
		RawInstruction inst = decompileInfo.getInstruction(new Integer(addr));
		if (inst == null) {
			return false;
		}
		if (inst.getInst() == InstTableCommon.Idata) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#dumpLabels(java.io.PrintStream)
	 */
	@Override
	public void dumpLabels(PrintStream os) {
		for (Object element : labels.values()) {
			Label label = (Label) element;
			os.println(label);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#dumpBlocks(java.io.PrintStream)
	 */
	@Override
	public void dumpBlocks(PrintStream os) {
		for (Object element : getBlocks()) {
			Block block = (Block) element;
			dumpBlock(os, block);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#dumpRoutines(java.io.PrintStream)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#dumpBlock(java.io.PrintStream, v9t9.common.asm.Block)
	 */
	@Override
	public void dumpBlock(PrintStream os, Block block) {
		for (Iterator<IHighLevelInstruction> iter = block.iterator(); iter.hasNext();) {
			IHighLevelInstruction inst = iter.next();
			dumpInstruction(os, inst);
		}
		System.out.println();
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#addBlock(v9t9.common.asm.Block)
	 */
	@Override
	public void addBlock(Block block) {
		blocks.put(block.getFirst().getInst().pc, block);
	}
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#getBlocks()
	 */
	@Override
	public Set<Block> getBlocks() {
		return new TreeSet<Block>(blocks.values());
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.asm.IDecompilePhase#getRoutines()
	 */
	@Override
	public Collection<Routine> getRoutines() {
		return routines.values();
	}

}
