/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.ejs.coffee.core.utils.HexUtils;
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
	private Map<Integer, String> symbols = new TreeMap<Integer, String>();
	protected int dp;
	
	private Map<String, DictEntry> dictionary = new LinkedHashMap<String, DictEntry>();
	
	private DictEntry lastEntry;
	protected int cellSize;
	private boolean export;
	private int baseDP;
	private PrintStream logfile = System.out;
	private Map<String, ForwardRef> forwards;
	
	public TargetContext(boolean littleEndian, int charBits, int cellBits, int memorySize) {
		this.littleEndian = littleEndian;
		this.charBits = charBits;
		this.cellBits = cellBits;
		this.cellSize = cellBits / 8;
		this.memory = new byte[memorySize];
		this.forwards = new LinkedHashMap<String,ForwardRef>();
		this.export = true;
	}

	abstract public void defineBuiltins();
	
	/** read the value in memory */
	public int readCell(int addr) {
		if (addr < 0) {
			addr = resolveAddr(addr);
		}
		
		if (!littleEndian && cellBits == 16) {
			return (short) ((memory[addr] & 0xff) << 8) | (memory[addr + 1] & 0xff); 
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
	public int addRelocation(int addr, RelocType type, int target, String name) {
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
	 * @param relocIndex
	 * @return
	 */
	public int resolveAddr(int relocIndex) {
		// read actual contents
		RelocEntry reloc = relocs.get(-relocIndex - 1);
		if (reloc == null)
			throw new IllegalArgumentException();
		relocIndex = reloc.addr;
		return relocIndex;
	}
	public int findReloc(int addr) {
		RelocEntry reloc = relocEntries.get(addr);
		if (reloc == null)
			return 0;
		return reloc.target;
	}

	public RelocEntry getRelocEntry(int id) {
		return relocs.get(-id - 1);
	}
	/**
	 * @param name
	 * @return
	 */
	public DictEntry defineEntry(String name) {
		alignDP();
		int entryAddr = getDP();
		int size = 0;
		if (export) {
			// link, name
			size = cellSize + align(1 + name.length());
			
			alignDP();
			entryAddr = alloc(size);
	
		}
		symbols.put(entryAddr, name);
		
		DictEntry entry = new DictEntry(size, entryAddr, name);
		entry.setExport(export);
		
		DictEntry existing = dictionary.get(name.toUpperCase());
		if (existing != null)
			logfile.println("*** Redefining " + name);
		dictionary.put(name.toUpperCase(), entry);
		
		if (export) {
			if (lastEntry != null)
				entry.setLink(lastEntry.getAddr());
			lastEntry = entry;
			
			entry.writeEntry(this);
		}
		
		ForwardRef ref = forwards.get(name.toUpperCase());
		if (ref != null) {
			resolveForward(ref, entry);
			forwards.remove(name.toUpperCase());
		}
		
		return entry;
	}


	/**
	 * @param token
	 * @return
	 */
	public IWord defineForward(String token, String location) {
		token = token.toUpperCase();
		ForwardRef ref = forwards.get(token);
		if (ref == null) {
			ref = new ForwardRef(token, location, -relocs.size() - 1);
			relocs.add(new RelocEntry(0, RelocType.RELOC_FORWARD, ref.getId()));
		}
		forwards.put(token, ref);
		return ref;
	}

	/**
	 * @param ref
	 */
	private void resolveForward(ForwardRef ref, DictEntry entry) {
		for (RelocEntry rel : relocs.toArray(new RelocEntry[relocs.size()])) {
			if (rel.target == ref.getId()) {
				rel.target = entry.getContentAddr();
				writeCell(rel.addr, entry.getContentAddr());
				//relocs.remove(rel);
			}
		}
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

	public int align(int bytes) {
		return (bytes + cellSize - 1) & ~(cellSize - 1);
	}

	public void writeCell(int addr, int cell) {
		if (addr < 0)
			addr = relocs.get(-addr - 1).target;
		
		RelocEntry entry = relocEntries.get(addr);
		if (entry != null)
			cell = -relocs.indexOf(entry) - 1;		// flag
		if (!littleEndian && cellBits == 16) {
			memory[addr] = (byte) (cell >> 8);
			memory[addr + 1] = (byte) (cell & 0xff);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public void writeChar(int addr, int ch) {
		if (charBits != 8) throw new UnsupportedOperationException();
		memory[addr] = (byte) ch;
	}

	public int getCellSize() {
		return cellSize;
	}

	public int readChar(int addr) {
		if (charBits != 8) throw new UnsupportedOperationException();
		return memory[addr];
	}

	public TargetVariable create(String name, int cells) {
		DictEntry entry = defineEntry(name);
		int dp = entry.getContentAddr();
		try {
			ITargetWord doVar = (ITargetWord) require("DOVAR");
			initCode();
			compile(doVar);
		} catch (AbortException e) {
			// for unit tests
		}
		int loc = alloc(cells * cellSize);
		entry.setCodeSize(loc - dp);
		TargetVariable var = (TargetVariable) define(name, new TargetVariable(entry, loc));
		return var;
	}

	public TargetColonWord defineColonWord(String name) {
		DictEntry entry = defineEntry(name);
		logfile.println(name);
		initCode();
		return (TargetColonWord) define(name, new TargetColonWord(entry));		
	}

	public TargetConstant defineConstant(String name, int value, int cells) throws AbortException {
		DictEntry entry = defineEntry(name);
		logfile.println(name);
		initCode();
		compile((ITargetWord) require("DOCON"));
		compileAddr(value);
		return (TargetConstant) define(name, new TargetConstant(entry, value, 1));		
	}

	abstract public void initCode();
	abstract public void alignCode();
	/**
	 * Compile a word onto the current dictionary entry
	 * @param semiS
	 */
	abstract public void compile(ITargetWord word);

	abstract public void compileLiteral(int value, boolean isUnsigned);
	abstract public void compileDoubleLiteral(int value, boolean isUnsigned);

	/**
	 * Flatten memory and resolve addresses
	 * @param console
	 */
	public void exportMemory(MemoryDomain console) {
		for (int i = 0; i < dp; i += cellSize) {
			RelocEntry reloc = relocEntries.get(i);
			int val;
			if (reloc != null) {
				val = reloc.target;	// TODO
				if (reloc.type == RelocType.RELOC_CALL_15S1)
					val = ((val >> 1) & 0x7fff) | 0x8000;
			} else {
				val = readCell(i);
			}
			console.writeWord(i, (short) val);
		}
		
		for (int i = 0; i < dp; i += MemoryDomain.AREASIZE)
			console.getEntryAt(i).clearSymbols();
		
		for (Map.Entry<Integer, String> symEntry : symbols.entrySet()) {
			console.getEntryAt(symEntry.getKey()).defineSymbol(symEntry.getKey(), symEntry.getValue());
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
	 * here 
	 */
	abstract public int pushHere(HostContext hostContext);

	/**
	 * swap
	 */
	abstract public void swapFixup(HostContext hostContext);

	/**
	 * here over - swap !
	 */
	abstract public void resolveFixup(HostContext hostContext);
	
	abstract public void compileBack(HostContext hostContext);
	
	public void clearDict() {
		super.clearDict();
		dp = 0;
		relocs.clear();
		relocEntries.clear();
		lastEntry = null;
		Arrays.fill(memory, (byte) 0);
	}

	/** compile address or offset */
	abstract public void compileAddr(int loc);
	abstract public void compileChar(int val);
	
	abstract public void pushLeave(HostContext hostContext);
	abstract public void loopCompile(HostContext hostCtx, ITargetWord loopCaller) throws AbortException;

	abstract public void defineCompilerWords(HostContext hostContext);

	/**
	 * 
	 */
	public void setExport(boolean export) {
		this.export = export;
	}
	/**
	 * @return the export
	 */
	public boolean isExport() {
		return export;
	}

	public interface IMemoryReader {
		int readWord(int addr);
	}

	public void dumpDict(PrintStream out, int from, int to) {
		dumpMemory(out, from, to, new IMemoryReader() {

			public int readWord(int addr) {
				return readCell(addr);
			}
			
		});
	}
	public static void dumpMemory(PrintStream out, int from, int to, IMemoryReader reader) {
		int perLine = 8;
		int lines = ((to - from) / 2 + perLine - 1) / perLine;
		int addr = from;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lines; i++) {
			boolean allZero = true;
			for (int j = 0; j < perLine && addr < to; j++) {
				if (reader.readWord(addr + j) != 0) {
					allZero = false;
					break;
				}
			}
			if (allZero) {
				addr += perLine * 2;
				continue;
			}
			out.print(HexUtils.toHex4(addr) + ": ");
			sb.setLength(0);
			int j;
			for (j = 0; j < perLine && addr < to; j++) {
				int word = reader.readWord(addr);
				out.print(HexUtils.toHex4(word) + " ");
				addr += 2;
				
				int ch = ((word >> 8) & 0xff);
				if (ch >= 0x20 && ch < 0x7f)
					sb.append((char) ch);
				else
					sb.append('.');
				ch = (word & 0xff);
				if (ch >= 0x20 && ch < 0x7f)
					sb.append((char) ch);
				else
					sb.append('.');
			}
			for (; j < perLine ; j++) {
				out.print("     ");
			}
			
			out.print(' ');
			out.print(sb);
			out.println();
		}
	}

	/**
	 * @return
	 */
	public int getBaseDP() {
		return baseDP;
	}
	
	/**
	 * @param baseDP the baseDP to set
	 */
	public void setBaseDP(int baseDP) {
		this.baseDP = baseDP;
		if (dp == 0)
			dp = baseDP;
	}

	/**
	 * @param logfile
	 */
	public void setLog(PrintStream logfile) {
		this.logfile = logfile != null ? logfile : System.out;
	}

	/**
	 * @return
	 */
	public Collection<ForwardRef> getForwardRefs() {
		return forwards.values();
	}

}
