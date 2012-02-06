/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

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
import v9t9.common.memory.IMemoryEntry;
import v9t9.gui.common.IMemoryDecoder;

/**
 * @author ejs
 *
 */
public class DisassemblerDecoder implements IMemoryDecoder {

	private final int chunkSize;
	private final IMemoryEntry entry;

	private int[] indexToAddrMap;
	private TreeMap<Integer, IHighLevelInstruction> addrToInstrMap = new TreeMap<Integer, IHighLevelInstruction>();
	private final IDecompilePhase decompilePhase;
	
	/**
	 * @param entry 
	 * @param instructionFactory
	 */
	public DisassemblerDecoder(IMemoryEntry entry, IRawInstructionFactory instructionFactory, IDecompilePhase decompilePhase) {
		this.entry = entry;
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

	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#initialize(v9t9.gui.client.swt.shells.debugger.MemoryRange)
	 */
	@Override
	public void initialize(MemoryRange range) {
		decompilePhase.getDecompileInfo().getMemoryRanges().clear();
		addrToInstrMap.clear();

		if (range == null)
			return;
		
		decompilePhase.getDecompileInfo().getMemoryRanges().
			addRange(range.getAddress(), range.getSize(), true);
		
		decompilePhase.disassemble();
		decompilePhase.run();

		
		TreeSet<Integer> addrs = new TreeSet<Integer>();
		for (Routine routine : decompilePhase.getRoutines()) {
			for (Block block : routine.getSpannedBlocks()) {
				IHighLevelInstruction instr = block.getFirst();
				while (true) {
					addrToInstrMap.put(instr.getInst().getPc(), instr);
					addrs.add(instr.getInst().getPc());
					if (instr.getInst().getPc() >= block.getLast().getInst().getPc())
						break;
					instr = instr.getNext();
				}
			}
		}
		
		int idx = 0;
		indexToAddrMap = new int[addrs.size()];
		for (Integer addr : addrs) {
			indexToAddrMap[idx++] = addr;
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#getItemCount()
	 */
	@Override
	public int getItemCount() {
		
		return indexToAddrMap.length;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.common.IMemoryDecoder#decode(int, int)
	 */
	@Override
	public IDecodedContent decodeItem(int index) {
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
