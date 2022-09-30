/*
  Phase.java

  (c) 2022 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.asm;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ejs.base.utils.Check;
import v9t9.common.asm.Block;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.Label;
import v9t9.common.asm.MemoryRange;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.Routine;
import v9t9.common.cpu.ICpuState;
import v9t9.common.memory.IMemoryDomain;
import v9t9.machine.ti99.cpu.MachineOperand9900;

public abstract class PhaseF99b implements IDecompilePhase {
	protected Map<Integer, Block> blocks;
	protected IMemoryDomain mainMemory;
	public IDecompileInfo decompileInfo;
	protected Map<Block, Label> labels;
	protected Map<Label, Routine> routines;
	private ICpuState state;
	
	public PhaseF99b(ICpuState state, IDecompileInfo info) {
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

	public short operandJumpTarget(IHighLevelInstruction inst, IMachineOperand mop) {
		return (short) (inst.getInst().getPc() + ((MachineOperand9900) mop).val);
	}

	public boolean operandIsLabel(IHighLevelInstruction inst, IMachineOperand mop) {
		return mop.isLabel()
				&& decompileInfo.getMemoryRanges().getRangeContaining(operandJumpTarget(
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
		RawInstruction inst = decompileInfo.getInstruction(Integer.valueOf(addr));
		if (inst == null) {
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
