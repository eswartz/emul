/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.BitSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import v9t9.common.asm.Block;
import v9t9.common.asm.IDecompilePhase;
import v9t9.common.asm.IHighLevelInstruction;
import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.asm.Routine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.gui.common.IMemoryDecoder;

/**
 * @author ejs
 *
 */
public class DisassemblerDecoder implements IMemoryDecoder {

	private final int chunkSize;
	final IMemoryDomain domain;
	private boolean dirty = true;

	private int[] indexToAddrMap;
	private TreeMap<Integer, IHighLevelInstruction> addrToInstrMap = new TreeMap<Integer, IHighLevelInstruction>();
	private final IDecompilePhase decompilePhase;
	
	/**
	 * @param entry 
	 * @param instructionFactory
	 */
	public DisassemblerDecoder(IMemoryDomain domain, IRawInstructionFactory instructionFactory, IDecompilePhase decompilePhase) {
		this.domain = domain;
		this.decompilePhase = decompilePhase;
		this.chunkSize = instructionFactory.getChunkSize();
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#getLabelProvider()
	 */
	@Override
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			@Override
			public String getText(Object element) {
				return ((IDecodedContent) element).getContent().toString();
			}
		};
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#getChunkSize()
	 */
	@Override
	public int getChunkSize() {
		return chunkSize;
	}

	private void refresh() {
		if (!dirty)
			return;
		

		decompilePhase.disassemble();
		decompilePhase.run();

		
		TreeSet<Integer> addrs = new TreeSet<Integer>();
		for (Routine routine : decompilePhase.getRoutines()) {
			for (Block block : routine.getSpannedBlocks()) {
				IHighLevelInstruction instr = block.getFirst();
				while (instr != null) {
					addrToInstrMap.put(instr.getInst().getPc(), instr);
					addrs.add(instr.getInst().getPc());
					if (instr == block.getLast())
						break;
					instr = instr.getLogicalNext();
				}
			}
		}
		
		int idx = 0;
		indexToAddrMap = new int[addrs.size()];
		for (Integer addr : addrs) {
			indexToAddrMap[idx++] = addr;
		}
		
		dirty = false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#reset()
	 */
	@Override
	public void reset() {
		if (decompilePhase == null)
			return;
		decompilePhase.getDecompileInfo().getMemoryRanges().clear();
		decompilePhase.getDecompileInfo().getInstructions().clear();
		decompilePhase.reset();
		addrToInstrMap.clear();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#addRange(int, int)
	 */
	@Override
	public void addRange(int addr, int size) {
		if (decompilePhase == null)
			return;
		decompilePhase.getDecompileInfo().getMemoryRanges().
			addRange(addr, size, true);
		
		dirty = true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#updateRange(java.util.BitSet)
	 */
	@Override
	public void updateRange(BitSet addrSet) {
		
		if (decompilePhase == null)
			return;
		
		//decompilePhase.getDecompileInfo().getMemoryRanges().clear();
		BitSet workingSet = (BitSet) addrSet.clone();
		boolean needsRebuild = false;
		Map<Integer, RawInstruction> instructions = decompilePhase.getDecompileInfo().getInstructions();
		for (int addr = workingSet.nextSetBit(0); addr >= 0 ; addr = workingSet.nextSetBit(addr + 1)) {
			RawInstruction inst = instructions.remove(addr);
			needsRebuild = true;
			if (inst != null) {
				for (int i = 1; i < inst.getSize() + getChunkSize(); i++) {
					workingSet.set(addr + i);
				}
			}
			addrToInstrMap.remove(addr);
		}
		
		if (needsRebuild) {
			addrToInstrMap.clear();
			decompilePhase.reset();
		}
		dirty = true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#getItemCount()
	 */
	@Override
	public int getItemCount(int addr, int size) {
		refresh();
		
		if (indexToAddrMap == null)
			return 0;
		int cnt = 0;
		for (int a : indexToAddrMap) {
			if (a >= addr && a < addr + size)
				cnt++;
		}
		return cnt;
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#getItemCount()
	 */
	@Override
	public int getFirstItemIndex(int addr) {
		refresh();
		
		if (indexToAddrMap == null)
			return 0;
		int cnt = 0;
		for (int a : indexToAddrMap) {
			if (a >= addr)
				return cnt;
			cnt++;
		}
		return cnt;
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#decode(int, int)
	 */
	@Override
	public IDecodedContent decodeItem(int index) {
		refresh();
		
		int addr = indexToAddrMap[index];
		final IHighLevelInstruction hl = addrToInstrMap.get(addr);
		if (hl == null)
			throw new IllegalStateException();
		final RawInstruction instr = hl.getInst();
		return new IDecodedContent() {

			@Override
			public int getAddr() {
				return instr.getPc();
			}
			
			@Override
			public int getSize() {
				return instr.getSize();
			}
			
			@Override
			public Object getContent() {
				return instr;
			}
			
		};
	}

	
}
