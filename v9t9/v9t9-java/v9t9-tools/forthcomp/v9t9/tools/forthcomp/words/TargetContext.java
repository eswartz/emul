/*
  TargetContext.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;

import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.Context;
import v9t9.tools.forthcomp.DictEntry;
import v9t9.tools.forthcomp.ForwardRef;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;
import v9t9.tools.forthcomp.RelocEntry;
import v9t9.tools.forthcomp.RelocEntry.RelocType;

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
	
	private Map<String, DictEntry> dictEntryMap = new LinkedHashMap<String, DictEntry>();
	
	private DictEntry lastEntry;
	private DictEntry lastExportedEntry;
	/** in bytes */
	protected int cellSize;
	private boolean export;
	private int baseDP;
	private PrintStream logfile = System.out;
	private Map<String, ForwardRef> forwards;
	private List<StubWord> stubWords = new ArrayList<StubWord>();
	public DictEntry stubData;
	private boolean exportFlagNext;
	private boolean exportFlag;
	//private boolean inlineFlagNext;
	protected HostContext hostCtx;

	public TargetContext(boolean littleEndian, int charBits, int cellBits, int memorySize) {
		this.littleEndian = littleEndian;
		this.charBits = charBits;
		this.cellBits = cellBits;
		this.cellSize = cellBits / 8;
		this.memory = new byte[memorySize];
		this.forwards = new LinkedHashMap<String,ForwardRef>();
		this.export = true;
		
		stubData = defineStub("<<data space>>");
	}

	public void setHostContext(HostContext hostCtx) {
		this.hostCtx = hostCtx;
	}

	
	protected DictEntry defineStub(String name) {
		StubWord stubWord = new StubWord(name);
		//getDictionary().put(name, stubWord.getEntry());
		stubWords.add(stubWord);
		return stubWord.getEntry();
	}
	
	abstract public void defineBuiltins() throws AbortException;
	
	/** read the value in memory */
	public int readCell(int addr) {
		if (addr < 0) {
			addr = resolveAddr(addr);
		}
		
		if (!littleEndian && cellBits == 16) {
			addr &= 0xffff;
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
		// clear out alloc'd space in case another word wrote temp stuff here
		while (size-- > 0) {
			writeChar(dp++, 0);
		}
		return old;
	}

	/**
	 * @return
	 */
	public int allocCell() {
		stubData.use(cellSize);
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
		boolean doExport = currentExport();
		//boolean doInline = inlineFlagNext;
		exportFlagNext = false;
		//inlineFlagNext = false;
		
		DictEntry entry = createDictEntry(size, entryAddr, name, doExport);
		entry.setExport(doExport);

		if (doExport && hostCtx != null) {
			ITargetWord word = (ITargetWord) find(">latest");
			if (word != null) {
				try {
					word.getExecutionSemantics().execute(hostCtx, this);
					//System.out.println("Latest: " + HexUtils.toHex4(entryAddr));
					writeCell(hostCtx.popData(), entry.getAddr());
				} catch (AbortException e) {
					e.printStackTrace();
				}
			}
		}
		
		DictEntry existing = dictEntryMap.get(name.toUpperCase());
		if (existing != null)
			logfile.println("*** Redefining " + name);
		dictEntryMap.put(name.toUpperCase(), entry);
		
		if (lastEntry != null) 
			lastEntry.setEndAddr(entry.getAddr());
		lastEntry = entry;
		
		if (hostCtx != null)
			hostCtx.setLatest(hostCtx.getDictionary().get(name.toUpperCase()));
		setLatest(getDictionary().get(name.toUpperCase()));
		
		if (doExport) {
			if (lastExportedEntry != null) {
				entry.setLink(lastExportedEntry.getAddr());
			}
			
			entry.writeEntry(this);
			lastExportedEntry = entry;
		}

		symbols.put(entry.getContentAddr(), name);

		ForwardRef ref = forwards.get(name.toUpperCase());
		if (ref != null) {
			resolveForward(ref, entry);
			forwards.remove(name.toUpperCase());
		}

		return entry;
	}

	/**
	 * @return the lastEntry
	 */
	public DictEntry getLastEntry() {
		return lastEntry;
	}
	
	/**
	 * @param size
	 * @param entryAddr
	 * @param name
	 * @param doExport TODO
	 * @return
	 */
	protected DictEntry createDictEntry(int size, int entryAddr, String name, boolean doExport) {

		if (doExport) {
			// link, name
			size = cellSize + align(1 + name.length());
			
			alignDP();
			stubData.use(size);
			entryAddr = alloc(size);
		}
		
		return new DictEntry(size, entryAddr, name);
	}

	private boolean currentExport() {
		boolean doExport = export;
		if (exportFlagNext)
			doExport = exportFlag;
		return doExport;
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
		for (RelocEntry rel : relocs) {
			if (rel.target == ref.getId()) {
				//System.out.println(rel);
				rel.target = entry.getContentAddr();
				if (rel.type != RelocType.RELOC_FORWARD)
					writeCell(rel.addr, entry.getContentAddr());
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
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#convertCell(int)
	 */
	public int writeCell(byte[] memory, int offs, int cell) {
		memory[offs++] = (byte) (cell >> 8);
		memory[offs++] = (byte) (cell & 0xff);
		return offs;
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

	public TargetVariable create(String name, int bytes) {
		DictEntry entry = defineEntry(name);
		int dp = entry.getContentAddr();
		initCode();
		compileDoVar();
		stubData.use(bytes);
		int loc = alloc(bytes);
		entry.setCodeSize(loc - dp);
		final TargetVariable var = (TargetVariable) define(name, new TargetVariable(entry));
		
		return var;
	}

	abstract protected void compileDoVar();



	public TargetColonWord defineColonWord(String name) {
		DictEntry entry = defineEntry(name);
		logfile.println(name);
		initCode();
		final TargetColonWord colon =  (TargetColonWord) define(name, new TargetColonWord(entry));
		
		return colon;
	}

	public TargetConstant defineConstant(String name, int value, int cells) throws AbortException {
		logfile.println(name);
		
		boolean mustDefine = currentExport();
		if (!mustDefine) {
			if (forwards.get(name.toUpperCase()) != null) {
				mustDefine = true;
				System.err.println("*** WARNING: forward reference to : " +name + " forces dictionary definition");
			}
		}
		if (mustDefine) {
			defineEntry(name);
			initCode();
			compileDoConstant(value, cells);
		} else {
			exportFlagNext = false;
			// assume address
			symbols.put(value, name);
		}
		final TargetConstant constant = (TargetConstant) define(name, new TargetConstant(name, value, 1));
		return constant;
	}

	public TargetValue defineValue(String name, int value, int cells) throws AbortException {
		DictEntry entry = defineEntry(name);
		logfile.println(name);
		
		int origDp = entry.getContentAddr();
		
		int loc = compilePushValue(cells, value);
		
		entry.setCodeSize(loc - origDp);
		
		final TargetValue tvalue = (TargetValue) define(name, new TargetValue(entry, cells));
		return tvalue;
	}

	abstract protected void compileLoad(int cells);



	/**
	 * At runtime, push the # of cells to the stack from the current DP.
	 * Return the location of the value.  The exact space must be allocated
	 * so the value can change.
	 * @param cells
	 * @param value
	 * @return DP of value
	 */
	abstract public int compilePushValue(int cells, int value) throws AbortException;
	
	/** At runtime, push the value in the given # of cells to the stack. 
	 * Can be optimized.
	 * @param value
	 * @param cells
	 * @throws AbortException
	 */
	abstract public void compileDoConstant(int value, int cells) throws AbortException;
	/** At runtime, push the user variable for the given index. 
	 * Can be optimized.
	 */
	abstract public void compileDoUser(int index) throws AbortException;

	public TargetUserVariable defineUser(String name, int bytes) throws AbortException {
		
		logfile.println(name);
		
		// the "variable" will be frozen in ROM, a count of bytes
		TargetVariable up = findOrCreateVariable("UP0");
		int index = readCell(up.getEntry().getParamAddr());
		writeCell(up.getEntry().getParamAddr(), index + bytes);


		boolean mustDefine = currentExport();
		if (!mustDefine) {
			if (forwards.get(name.toUpperCase()) != null) {
				mustDefine = true;
				System.err.println("*** WARNING: forward reference to : " +name + " forces dictionary definition");
			}
		}
		if (mustDefine) {
			defineEntry(name);
			initCode();
			
			compileDoUser(index);
		} else {
			exportFlagNext = false;
		}
		
		return (TargetUserVariable) define(name, new TargetUserVariable(name, index));
	}

	protected TargetVariable findOrCreateVariable(String name) {
		TargetVariable var = (TargetVariable) find(name);
		if (var == null) {
			var = create(name, getCellSize());
		}
		return var;
	}

	abstract public void initCode();
	abstract public void alignBranch();
	/**
	 * Compile a word onto the current dictionary entry
	 */
	abstract public void compile(ITargetWord word);

	abstract public void compileLiteral(int value, boolean isUnsigned, boolean optimize);
	abstract public void compileDoubleLiteral(int valueLo, int valiueHi, boolean isUnsigned, boolean optimize);

	/**
	 * Flatten memory and resolve addresses
	 * @param console
	 * @throws AbortException 
	 */
	public void exportMemory(IMemoryDomain console) throws AbortException {
		for (int i = baseDP; i < dp; i += cellSize) {
			console.writeWord(i, (short) readCell(i));
		}
		for (RelocEntry reloc : relocEntries.values()) {
			int val = doResolveRelocation(reloc);
			console.writeWord(reloc.addr, (short) val);
		}
		
		for (int i = baseDP; i < dp; i += MemoryDomain.AREASIZE)
			console.getEntryAt(i).clearSymbols();
		
		for (Map.Entry<Integer, String> symEntry : symbols.entrySet()) {
			console.getEntryAt(symEntry.getKey()).defineSymbol(symEntry.getKey(), symEntry.getValue());
		}
	}
	protected abstract int doResolveRelocation(RelocEntry reloc) throws AbortException;



	/**
	 * Flatten memory and resolve addresses
	 * @param console
	 */
	public void importMemory(IMemoryDomain console) {
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
	 * @throws AbortException TODO
	 */
	abstract public void resolveFixup(HostContext hostContext) throws AbortException;
	
	abstract public void compileBack(HostContext hostContext, boolean conditional) throws AbortException;
	
	public void clearDict() {
		super.clearDict();
		dp = 0;
		relocs.clear();
		relocEntries.clear();
		lastEntry = null;
		lastExportedEntry = null;
		Arrays.fill(memory, (byte) 0);
	}

	/** compile cell value */
	abstract public void compileCell(int val);
	abstract public void compileChar(int val);
	
	/** compile address */
	abstract public void compileTick(ITargetWord word);

	abstract public void compileWordParamAddr(TargetValue word);

	abstract public void pushLeave(HostContext hostContext);
	abstract public void loopCompile(HostContext hostCtx, ITargetWord loopCaller) throws AbortException;

	abstract public void defineCompilerWords(HostContext hostContext);

	public void setExport(boolean export) {
		this.export = export;
	}
	public boolean isExport() {
		return export;
	}
	public void setExportNext(boolean export) {
		this.exportFlag = export;
		this.exportFlagNext = true;
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

	/**
	 * @return
	 */
	public Map<String, DictEntry> getTargetDictionary() {
		return dictEntryMap;
	}

	abstract public boolean isLocalSupportAvailable(HostContext hostContext) throws AbortException;
	
	abstract public void ensureLocalSupport(HostContext hostContext) throws AbortException;
	
	abstract public void compileSetupLocals(HostContext hostContext) throws AbortException;

	abstract public void compileAllocLocals(int count) throws AbortException;

	abstract public void compileLocalAddr(int index);

	abstract public void compileFromLocal(int index) throws AbortException;

	abstract public void compileToLocal(int index) throws AbortException;
	
	abstract public void compileCleanupLocals(HostContext hostContext) throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.Context#find(java.lang.String)
	 */
	@Override
	public IWord find(String token) {
		if (getLatest() != null) {
			DictEntry entry = ((ITargetWord) getLatest()).getEntry();
			if (entry.hasLocals()) {
				IWord word = entry.findLocalWord(token);
				if (word != null) {
					return word;
				}
			}
		}
		return super.find(token);
	}
	

	/**
	 * @param hostContext TODO
	 * @throws AbortException 
	 * 
	 */
	public void compileExit(HostContext hostContext) throws AbortException {
		if (((ITargetWord) getLatest()).getEntry().hasLocals())
			compileCleanupLocals(hostContext);
		
		ITargetWord semiS = (ITargetWord) require(";S");
		semiS.getCompilationSemantics().execute(hostContext, this);
	}

	/**
	 * @param hostContext TODO
	 * @param word
	 * @throws AbortException 
	 */
	public void compileToValue(HostContext hostContext, TargetValue word) throws AbortException {
		compileWordParamAddr(word);
		//compile((ITargetWord) require("!"));
		require("!").getCompilationSemantics().execute(hostContext, this);

	}

	abstract public MemoryDomain createMemory();


	protected abstract void doExportState(HostContext hostCtx, IBaseMachine machine,
			int baseSp, int baseRp, int baseUp) throws AbortException;
	public void exportState(HostContext hostCtx, IBaseMachine machine,
			int baseSp, int baseRp, int baseUp) throws AbortException {
		
		IWord dp = find("DP");
		if (dp instanceof ITargetWord) {
			writeCell(((ITargetWord) dp).getEntry().getParamAddr(), getDP());
		}
		
		doExportState(hostCtx, machine, baseSp, baseRp, baseUp);

	}



	protected abstract void doImportState(HostContext hostCtx, IBaseMachine machine,
			int baseSp, int baseRp);
	public void importState(HostContext hostCtx, IBaseMachine machine,
			int baseSp, int baseRp) {
		doImportState(hostCtx, machine, baseSp, baseRp);
		
		IWord dp = find("DP");
		if (dp instanceof ITargetWord) {
			setDP(readCell(((ITargetWord) dp).getEntry().getParamAddr()));
		}

	}



	/**
	 * @param string
	 * @return pair of the address of start, plus the total length
	 */
	public Pair<Integer, Integer> writeLengthPrefixedString(String string) throws AbortException {
		int length = string.length();
		if (length > 255)
			throw new AbortException("String constant is too long");
		
		int dp = getDP();
		
		writeChar(dp, length);
		stubData.use();
		
		for (int i = 0; i < length; i++) {
			writeChar(dp + 1 + i, string.charAt(i));
			stubData.use();
		}
		
		return new Pair<Integer, Integer>(dp, length + 1);
	}



	/**
	 * 
	 */
	public void markHostExecutionUnsupported() {
		((TargetWord) getLatest()).setHostDp(-1);
	}



	public abstract void compileCall(ITargetWord word);



	abstract public void compilePostpone(ITargetWord word) throws AbortException;



	abstract public void compileDoes(HostContext hostContext, DictEntry dictEntry, int targetDP) throws AbortException;


	public void compileString(HostContext hostContext, String string) throws AbortException {
		IWord parenString = require("(s\")");
		compileCall((ITargetWord) parenString);
		Pair<Integer, Integer> info = writeLengthPrefixedString(string);
		setDP(getDP() + info.second);
	}



	/**
	 * Prepare for DOES>
	 * @param hostContext
	 * @return target addr for DOES
	 */
	abstract public int compileDoDoes(HostContext hostContext) throws AbortException;



	/**
	 * @param opcode
	 */
	abstract public void compileOpcode(int opcode);
	abstract public void compileUser(TargetUserVariable var);

	/**
	 * @return
	 */
	public int getUP() {
		TargetVariable up = findOrCreateVariable("UP0");
		return readCell(up.getEntry().getParamAddr());
	}

	public void dumpStubs(PrintStream logfile) {
		logfile.println("Stub uses:");
		for (StubWord word : stubWords) {
			logfile.println("\t"+word.getName() + " = " + word.getEntry().getUses());
		}
	}
}
