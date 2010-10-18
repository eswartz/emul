/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ejs.v9t9.forthcomp.RelocEntry.RelocType;

import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public abstract class TargetContext extends Context {

	private final boolean littleEndian;
	private final int charBits;
	private final int cellBits;
	private byte[] memory;

	private Map<Integer, RelocEntry> relocEntries = new TreeMap<Integer, RelocEntry>();
	private List<RelocEntry> relocs = new ArrayList<RelocEntry>();
	protected int dp;
	
	private DictEntry lastEntry;
	protected int cellSize;
	
	public TargetContext(boolean littleEndian, int charBits, int cellBits, int memorySize) {
		this.littleEndian = littleEndian;
		this.charBits = charBits;
		this.cellBits = cellBits;
		this.cellSize = cellBits / 8;
		this.memory = new byte[memorySize];
	}

	abstract public void defineBuiltins();
	
	/** read the value in memory */
	public int readCell(int addr) {
		if (addr < 0) {
			addr = resolveAddr(addr);
		}
		
		if (!littleEndian && cellBits == 16) {
			return ((memory[addr] & 0xff) << 8) | (memory[addr + 1] & 0xff); 
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	/** read the addr in memory, which may be a relocation
	 * @return negative value for relocation, else literal value */
	public int readAddr(int addr) {
		if (addr < 0) {
			addr = resolveAddr(addr);
		}
		else {
			// see if this is a relocated entry
			RelocEntry reloc = relocEntries.get(addr);
			if (reloc != null)
				return -relocs.indexOf(reloc) - 1;
		}
		
		if (!littleEndian && cellBits == 16) {
			return ((memory[addr] & 0xff) << 8) | (memory[addr + 1] & 0xff); 
		} else {
			throw new UnsupportedOperationException();
		}
	}
	public int addRelocation(int addr, RelocType type, int target) {
		RelocEntry reloc = new RelocEntry(addr, type, target);
		assert !relocEntries.containsKey(addr);
		relocEntries.put(addr, reloc);
		relocs.add(reloc);
		return -relocs.size();
	}

	public int getDP() {
		return dp;
	}
	
	/**
	 * @param dp the dp to set
	 */
	public void setDP(int dp) {
		this.dp = dp;
	}
	
	public int alloc(int size) {
		int old = dp;
		dp += size;
		return old;
	}

	/**
	 * @return
	 */
	public int allocCell() {
		return alloc(cellSize);
	}

	/**
	 * @param addr
	 * @return
	 */
	public int resolveAddr(int addr) {
		// read actual contents
		RelocEntry reloc = relocs.get(-addr - 1);
		if (reloc == null)
			throw new IllegalArgumentException();
		addr = reloc.addr;
		return addr;
	}

	/**
	 * @param name
	 * @return
	 */
	public DictEntry defineEntry(String name) {
		// link, name
		int size = cellSize + align(1 + name.length());
		
		alignDP();
		int entryAddr = alloc(size);

		DictEntry entry = new DictEntry(size, entryAddr, name);
		if (lastEntry != null)
			entry.setLink(lastEntry.getAddr());
		lastEntry = entry;

		entry.writeEntry(this);
		
		return entry;
	}


	/**
	 * 
	 */
	public void alignDP() {
		dp = getAlignedDP();
	}

	public int getAlignedDP() {
		return (dp + cellSize - 1) & ~(cellSize - 1);
	}

	/**
	 * @param i
	 * @return
	 */
	public int align(int bytes) {
		return (bytes + cellSize - 1) & ~(cellSize - 1);
	}

	/**
	 * @param ad
	 * @param link
	 */
	public void writeCell(int addr, int cell) {
		if (addr < 0)
			addr = relocs.get(-addr - 1).target;
		
		RelocEntry entry = relocEntries.get(cell);
		if (entry != null)
			cell = relocs.indexOf(entry);		// flag
		if (!littleEndian && cellBits == 16) {
			memory[addr] = (byte) (cell >> 8);
			memory[addr + 1] = (byte) (cell & 0xff);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * @param ad
	 * @param i
	 */
	public void writeChar(int addr, int ch) {
		if (charBits != 8) throw new UnsupportedOperationException();
		memory[addr] = (byte) ch;
	}

	/**
	 * @return
	 */
	public int getCellSize() {
		return cellSize;
	}

	/**
	 * @param i
	 * @return
	 */
	public int readChar(int addr) {
		if (charBits != 8) throw new UnsupportedOperationException();
		return memory[addr];
	}

	/**
	 * @param name
	 * @return 
	 */
	public TargetVariable defineVariable(String name) {
		DictEntry entry = defineEntry(name);
		int here = allocCell();
		return (TargetVariable) define(name, new TargetVariable(entry, 
				addRelocation(here, RelocType.RELOC_ABS_ADDR_16, here)));
		
	}

	/**
	 * @param name
	 */
	public TargetColonWord defineColonWord(String name) {
		DictEntry entry = defineEntry(name);
		return (TargetColonWord) define(name, new TargetColonWord(entry));		
	}

	/**
	 * Compile a word onto the current dictionary entry
	 * @param semiS
	 */
	abstract public void compile(ITargetWord word);

	abstract public void compileLiteral(int value);
	abstract public void compileDoubleLiteral(int value);

	/**
	 * Flatten memory and resolve addresses
	 * @param console
	 */
	public void exportMemory(MemoryDomain console) {
		for (int i = 0; i < dp; i += cellSize) {
			int val = readAddr(i);
			if (val < 0) {
				RelocEntry reloc = relocs.get(-val - 1);
				val = reloc.target;	// TODO
			}
			console.writeWord(i, (short) val);
		}
	}
	/**
	 * Flatten memory and resolve addresses
	 * @param console
	 */
	public void importMemory(MemoryDomain console) {
		for (int i = 0; i < memory.length; i += cellSize) {
			RelocEntry reloc = relocEntries.get(i);
			if (reloc == null) {
				short val = console.readWord(i);
				writeCell(i, val);
			}
		}
	}

	/**
	 * here 0 ,
	 */
	abstract public void pushFixup(HostContext hostContext);

	/**
	 * swap
	 */
	abstract public void swapFixup(HostContext hostContext);

	/**
	 * here over - swap !
	 */
	abstract public void resolveFixup(HostContext hostContext);
}
