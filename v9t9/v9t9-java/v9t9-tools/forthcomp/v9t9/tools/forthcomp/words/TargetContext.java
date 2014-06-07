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
import v9t9.tools.forthcomp.ITargetContext;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;
import v9t9.tools.forthcomp.RelocEntry;
import v9t9.tools.forthcomp.RelocEntry.RelocType;

/**
 * @author ejs
 *
 */
public abstract class TargetContext extends Context implements ITargetContext {

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
	protected PrintStream logfile = System.out;
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

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#setHostContext(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	public void setHostContext(HostContext hostCtx) {
		this.hostCtx = hostCtx;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.Context#define(java.lang.String, v9t9.tools.forthcomp.IWord)
	 */
	@Override
	public IWord define(String string, IWord word) {
		if (word instanceof TargetWord) {
			logfile.println("T>"+Integer.toHexString(((TargetWord) word).getEntry().getAddr()) +" " + ((TargetWord) word).getClass().getSimpleName() + " " + string);
		}
		return super.define(string, word);
	}
	
	protected DictEntry defineStub(String name) {
		StubWord stubWord = new StubWord(name);
		//getDictionary().put(name, stubWord.getEntry());
		stubWords.add(stubWord);
		return stubWord.getEntry();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineBuiltins()
	 */
	@Override
	abstract public void defineBuiltins() throws AbortException;
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#readCell(int)
	 */
	@Override
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
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#readAddr(int)
	 */
	@Override
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
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#addRelocation(int, v9t9.tools.forthcomp.RelocEntry.RelocType, int)
	 */
	@Override
	public int addRelocation(int addr, RelocType type, int target) {
		RelocEntry reloc = new RelocEntry(addr, type, target);
		assert !relocEntries.containsKey(addr);
		relocEntries.put(addr, reloc);
		relocs.add(reloc);
		return -relocs.size();
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getDP()
	 */
	@Override
	public int getDP() {
		return dp;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#setDP(int)
	 */
	@Override
	public void setDP(int dp) {
		this.dp = dp;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#alloc(int)
	 */
	@Override
	public int alloc(int size) {
		int old = dp;
		// clear out alloc'd space in case another word wrote temp stuff here
		while (size-- > 0) {
			writeChar(dp++, 0);
		}
		return old;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#allocCell()
	 */
	@Override
	public int allocCell() {
		stubData.use(cellSize);
		return alloc(cellSize);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#resolveAddr(int)
	 */
	@Override
	public int resolveAddr(int relocIndex) {
		// read actual contents
		RelocEntry reloc = relocs.get(-relocIndex - 1);
		if (reloc == null)
			throw new IllegalArgumentException();
		relocIndex = reloc.addr;
		return relocIndex;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#findReloc(int)
	 */
	@Override
	public int findReloc(int addr) {
		RelocEntry reloc = relocEntries.get(addr);
		if (reloc == null)
			return 0;
		return reloc.target;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#removeReloc(int)
	 */
	@Override
	public void removeReloc(int addr) {
		relocEntries.remove(addr);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getRelocEntry(int)
	 */
	@Override
	public RelocEntry getRelocEntry(int id) {
		return relocs.get(-id - 1);
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineEntry(java.lang.String)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getLastEntry()
	 */
	@Override
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


	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineForward(java.lang.String, java.lang.String)
	 */
	@Override
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
				rel.target = entry.getContentAddr();
				if (rel.type != RelocType.RELOC_FORWARD)
					writeCell(rel.addr, entry.getContentAddr());
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#alignDP()
	 */
	@Override
	public void alignDP() {
		dp = getAlignedDP();
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getAlignedDP()
	 */
	@Override
	public int getAlignedDP() {
		return (dp + cellSize - 1) & ~(cellSize - 1);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#align(int)
	 */
	@Override
	public int align(int bytes) {
		return (bytes + cellSize - 1) & ~(cellSize - 1);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#convertCell(int)
	 */
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#writeCell(byte[], int, int)
	 */
	@Override
	public int writeCell(byte[] memory, int offs, int cell) {
		memory[offs++] = (byte) (cell >> 8);
		memory[offs++] = (byte) (cell & 0xff);
		return offs;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#writeCell(int, int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#writeChar(int, int)
	 */
	@Override
	public void writeChar(int addr, int ch) {
		if (charBits != 8) throw new UnsupportedOperationException();
		memory[addr] = (byte) ch;
	}

	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getCellSize()
	 */
	@Override
	public int getCellSize() {
		return cellSize;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#readChar(int)
	 */
	@Override
	public int readChar(int addr) {
		if (charBits != 8) throw new UnsupportedOperationException();
		return memory[addr];
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#create(java.lang.String, int)
	 */
	@Override
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



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineColonWord(java.lang.String)
	 */
	@Override
	public TargetColonWord defineColonWord(String name) {
		DictEntry entry = defineEntry(name);
//		logfile.println("T> : " + name);
		initCode();
		final TargetColonWord colon =  (TargetColonWord) define(name, new TargetColonWord(entry));
		return colon;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineConstant(java.lang.String, int, int)
	 */
	@Override
	public TargetConstant defineConstant(String name, int value, int cells) throws AbortException {
//		logfile.println("T> CONSTANT " + name);
		
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

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineValue(java.lang.String, int, int)
	 */
	@Override
	public TargetValue defineValue(String name, int value, int cells) throws AbortException {
		DictEntry entry = defineEntry(name);
//		logfile.println("T> VALUE " + name);
		
		int origDp = entry.getContentAddr();
		
		int loc = compilePushValue(cells, value);
		
		entry.setCodeSize(loc - origDp);
		
		final TargetValue tvalue = (TargetValue) define(name, new TargetValue(entry, cells));
		return tvalue;
	}

	abstract protected void compileLoad(int cells);



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compilePushValue(int, int)
	 */
	@Override
	abstract public int compilePushValue(int cells, int value) throws AbortException;
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileDoConstant(int, int)
	 */
	@Override
	abstract public void compileDoConstant(int value, int cells) throws AbortException;
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileDoUser(int)
	 */
	@Override
	abstract public void compileDoUser(int index) throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineUser(java.lang.String, int)
	 */
	@Override
	public TargetUserVariable defineUser(String name, int bytes) throws AbortException {
		
//		logfile.println("T> USER " + name);
		
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

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#initCode()
	 */
	@Override
	abstract public void initCode();
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#alignBranch()
	 */
	@Override
	abstract public void alignBranch();
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compile(v9t9.tools.forthcomp.ITargetWord)
	 */
	@Override
	abstract public void compile(ITargetWord word);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileLiteral(int, boolean, boolean)
	 */
	@Override
	abstract public void compileLiteral(int value, boolean isUnsigned, boolean optimize);
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileDoubleLiteral(int, int, boolean, boolean)
	 */
	@Override
	abstract public void compileDoubleLiteral(int valueLo, int valiueHi, boolean isUnsigned, boolean optimize);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#exportMemory(v9t9.common.memory.IMemoryDomain)
	 */
	@Override
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



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#importMemory(v9t9.common.memory.IMemoryDomain)
	 */
	@Override
	public void importMemory(IMemoryDomain console) {
		for (int i = 0; i < memory.length; i += cellSize) {
			RelocEntry reloc = relocEntries.get(i);
			if (reloc == null) {
				short val = console.readWord(i);
				writeCell(i, val);
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#pushFixup(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public void pushFixup(HostContext hostContext);
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#pushHere(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public int pushHere(HostContext hostContext);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#swapFixup(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public void swapFixup(HostContext hostContext);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#resolveFixup(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public void resolveFixup(HostContext hostContext) throws AbortException;
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileBack(v9t9.tools.forthcomp.HostContext, boolean)
	 */
	@Override
	abstract public void compileBack(HostContext hostContext, boolean conditional) throws AbortException;
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#clearDict()
	 */
	@Override
	public void clearDict() {
		super.clearDict();
		dp = 0;
		relocs.clear();
		relocEntries.clear();
		lastEntry = null;
		lastExportedEntry = null;
		Arrays.fill(memory, (byte) 0);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileCell(int)
	 */
	@Override
	abstract public void compileCell(int val);
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileChar(int)
	 */
	@Override
	abstract public void compileChar(int val);
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileTick(v9t9.tools.forthcomp.ITargetWord)
	 */
	@Override
	abstract public void compileTick(ITargetWord word);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileWordParamAddr(v9t9.tools.forthcomp.words.TargetValue)
	 */
	@Override
	abstract public void compileWordParamAddr(TargetValue word);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#pushLeave(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public void pushLeave(HostContext hostContext);
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#loopCompile(v9t9.tools.forthcomp.HostContext, v9t9.tools.forthcomp.ITargetWord)
	 */
	@Override
	abstract public void loopCompile(HostContext hostCtx, ITargetWord loopCaller) throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineCompilerWords(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public void defineCompilerWords(HostContext hostContext);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#setExport(boolean)
	 */
	@Override
	public void setExport(boolean export) {
		this.export = export;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#isExport()
	 */
	@Override
	public boolean isExport() {
		return export;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#setExportNext(boolean)
	 */
	@Override
	public void setExportNext(boolean export) {
		this.exportFlag = export;
		this.exportFlagNext = true;
	}

	public interface IMemoryReader {
		int readWord(int addr);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#dumpDict(java.io.PrintStream, int, int)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getBaseDP()
	 */
	@Override
	public int getBaseDP() {
		return baseDP;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#setBaseDP(int)
	 */
	@Override
	public void setBaseDP(int baseDP) {
		this.baseDP = baseDP;
		if (dp == 0)
			dp = baseDP;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#setLog(java.io.PrintStream)
	 */
	@Override
	public void setLog(PrintStream logfile) {
		this.logfile = logfile != null ? logfile : System.out;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getForwardRefs()
	 */
	@Override
	public Collection<ForwardRef> getForwardRefs() {
		return forwards.values();
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getTargetDictionary()
	 */
	@Override
	public Map<String, DictEntry> getTargetDictionary() {
		return dictEntryMap;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#isLocalSupportAvailable(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public boolean isLocalSupportAvailable(HostContext hostContext) throws AbortException;
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#ensureLocalSupport(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public void ensureLocalSupport(HostContext hostContext) throws AbortException;
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileSetupLocals(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public void compileSetupLocals(HostContext hostContext) throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileAllocLocals(int)
	 */
	@Override
	abstract public void compileAllocLocals(int count) throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileLocalAddr(int)
	 */
	@Override
	abstract public void compileLocalAddr(int index);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileFromLocal(int)
	 */
	@Override
	abstract public void compileFromLocal(int index) throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileToLocal(int)
	 */
	@Override
	abstract public void compileToLocal(int index) throws AbortException;
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileCleanupLocals(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public void compileCleanupLocals(HostContext hostContext) throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#find(java.lang.String)
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
	

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileExit(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	public void compileExit(HostContext hostContext) throws AbortException {
		if (((ITargetWord) getLatest()).getEntry().hasLocals())
			compileCleanupLocals(hostContext);
		
		require(";S").getCompilationSemantics().execute(hostContext, this);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileToValue(v9t9.tools.forthcomp.HostContext, v9t9.tools.forthcomp.words.TargetValue)
	 */
	@Override
	public void compileToValue(HostContext hostContext, TargetValue word) throws AbortException {
		compileWordParamAddr(word);
		//compile((ITargetWord) require("!"));
		require("!").getCompilationSemantics().execute(hostContext, this);

	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#createMemory()
	 */
	@Override
	abstract public MemoryDomain createMemory();


	protected abstract void doExportState(HostContext hostCtx, IBaseMachine machine,
			int baseSp, int baseRp, int baseUp) throws AbortException;
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#exportState(v9t9.tools.forthcomp.HostContext, v9t9.common.machine.IBaseMachine, int, int, int)
	 */
	@Override
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
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#importState(v9t9.tools.forthcomp.HostContext, v9t9.common.machine.IBaseMachine, int, int)
	 */
	@Override
	public void importState(HostContext hostCtx, IBaseMachine machine,
			int baseSp, int baseRp) {
		doImportState(hostCtx, machine, baseSp, baseRp);
		
		IWord dp = find("DP");
		if (dp instanceof ITargetWord) {
			setDP(readCell(((ITargetWord) dp).getEntry().getParamAddr()));
		}

	}



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#writeLengthPrefixedString(java.lang.String)
	 */
	@Override
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



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#markHostExecutionUnsupported()
	 */
	@Override
	public void markHostExecutionUnsupported() {
		((TargetWord) getLatest()).setHostDp(-1);
	}



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileCall(v9t9.tools.forthcomp.ITargetWord)
	 */
	@Override
	public abstract void compileCall(ITargetWord word);



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compilePostpone(v9t9.tools.forthcomp.ITargetWord)
	 */
	@Override
	abstract public void compilePostpone(ITargetWord word) throws AbortException;



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileDoes(v9t9.tools.forthcomp.HostContext, v9t9.tools.forthcomp.DictEntry, int)
	 */
	@Override
	abstract public void compileDoes(HostContext hostContext, DictEntry dictEntry, int targetDP) throws AbortException;


	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileString(v9t9.tools.forthcomp.HostContext, java.lang.String)
	 */
	@Override
	public void compileString(HostContext hostContext, String string) throws AbortException {
		IWord parenString = require("(s\")");
		compileCall((ITargetWord) parenString);
		Pair<Integer, Integer> info = writeLengthPrefixedString(string);
		setDP(getDP() + info.second);
	}



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileDoDoes(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	abstract public int compileDoDoes(HostContext hostContext) throws AbortException;



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileOpcode(int)
	 */
	@Override
	abstract public void compileOpcode(int opcode);
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileUser(v9t9.tools.forthcomp.words.TargetUserVariable)
	 */
	@Override
	abstract public void compileUser(TargetUserVariable var);

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#getUP()
	 */
	@Override
	public int getUP() {
		TargetVariable up = findOrCreateVariable("UP0");
		return readCell(up.getEntry().getParamAddr());
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#dumpStubs(java.io.PrintStream)
	 */
	@Override
	public void dumpStubs(PrintStream logfile) {
		logfile.println("Stub uses:");
		for (StubWord word : stubWords) {
			logfile.println("\t"+word.getName() + " = " + word.getEntry().getUses());
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#parseLiteral(java.lang.String)
	 */
	@Override
	public IWord parseLiteral(String token) {
		int radix = ((HostVariable) hostCtx.find("base")).getValue();
		boolean isNeg = token.startsWith("-");
		if (isNeg) {
			token = token.substring(1);
		}
		if (token.startsWith("$")) {
			radix = 16;
			token = token.substring(1);
		}
		else if (token.startsWith("&")) {
			radix = 10;
			token = token.substring(1);
		}
		boolean isDouble = false;
		if (token.contains(".")) {
			isDouble = true;
		}
		token = token.replaceAll("\\.", "");
		boolean isUnsigned = false;
		if (token.toUpperCase().endsWith("U")) {
			isUnsigned = true;
			token = token.substring(0, token.length() - 1);
		}
		try {
			long val = Long.parseLong(token, radix);
			if (isNeg)
				val = -val;
			if (isDouble) {
				if (getCellSize() == 2)
					return new HostDoubleLiteral((int)(val & 0xffff), (int)(val >> 16), isUnsigned);
				else
					throw new UnsupportedOperationException();
			}
			else
				return new HostLiteral((int) val, isUnsigned);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
