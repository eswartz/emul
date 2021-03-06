/*
  TargetContext.java

  (c) 2010-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import v9t9.common.asm.IRawInstructionFactory;
import v9t9.common.asm.RawInstruction;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.machine.ti99.memory.EnhancedRamByteArea;
import v9t9.tools.forthcomp.RelocEntry.RelocType;
import v9t9.tools.forthcomp.words.HostDoubleLiteral;
import v9t9.tools.forthcomp.words.HostLiteral;
import v9t9.tools.forthcomp.words.HostVariable;
import v9t9.tools.forthcomp.words.INativeCodeWord;
import v9t9.tools.forthcomp.words.StubWord;
import v9t9.tools.forthcomp.words.TargetCodeWord;
import v9t9.tools.forthcomp.words.TargetColonWord;
import v9t9.tools.forthcomp.words.TargetConstant;
import v9t9.tools.forthcomp.words.TargetDefer;
import v9t9.tools.forthcomp.words.TargetUserVariable;
import v9t9.tools.forthcomp.words.TargetValue;
import v9t9.tools.forthcomp.words.TargetVariable;
import v9t9.tools.forthcomp.words.TargetWord;
import ejs.base.utils.HexUtils;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public abstract class TargetContext extends Context implements ITargetContext {

	private final boolean littleEndian;
	private final int charBits;
	private final int cellBits;
	protected byte[] memory;
	protected IRawInstructionFactory rawInstructionFactory;

	protected Map<Integer, RelocEntry> relocEntries = new TreeMap<Integer, RelocEntry>();
	protected List<RelocEntry> relocs = new ArrayList<RelocEntry>();
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

	private List<Integer> leaves;
	private boolean testMode;

	protected IWord romDeferTableWord;
	private TargetConstant numRomDefersWord;
	private Map<TargetDefer, Integer> targetDefers = new HashMap<TargetDefer, Integer>();
	private MemoryDomain console;
	

	
	public TargetContext(boolean littleEndian, int charBits, int cellBits, int memorySize) {
		this.littleEndian = littleEndian;
		this.charBits = charBits;
		this.cellBits = cellBits;
		this.cellSize = cellBits / 8;
		this.memory = new byte[memorySize];
		this.forwards = new LinkedHashMap<String,ForwardRef>();
		this.export = true;
		leaves = new LinkedList<Integer>();
		
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
			logfile.println("T>"+ HexUtils.toHex4(((TargetWord) word).getEntry().getAddr()) +" " 
					//+ ((TargetWord) word).getClass().getSimpleName() + " " + string
					+ word
					);
			resolveForward(((ITargetWord) word).getEntry());
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
	 * @see v9t9.tools.forthcomp.ITargetContext#defineColonPrims()
	 */
	@Override
	public void defineColonPrims() throws AbortException {
		defineConstant("#CHAR", 1, 1);
		defineConstant("#CELL", getCellSize(), 1);

		setExportNext(false);
		defineConstant("TESTING?", isTestMode() ? -1 : 0, 1);
		
		defineConstant("LOCALS", isLocalSupportAvailable(hostCtx) ? -1 : 0, 1);
		defineConstant("GROM-DICT", this instanceof IGromTargetContext
							&& ((BaseGromTargetContext) this).useGromDictionary() ? -1 : 0, 1);

		romDeferTableWord = find("(rdefertbl)");
		if (romDeferTableWord == null) {
			romDeferTableWord = defineForward("(rdefertbl)", "<builtin>");
		}
		setExportNext(false);

		numRomDefersWord = defineConstant("(#rdefers)", 0, 1);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineBuiltins()
	 */
	public void definePrims() throws AbortException {
		

	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.ITargetContext#defineCompilerWords(v9t9.tools.forthcomp.HostContext)
	 */
	@Override
	public void defineCompilerWords(HostContext hostContext) {
		
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#readCell(int)
	 */
	@Override
	public int readCell(int addr) {
		if (addr < 0) {
			addr = resolveAddr(addr);
		}
		return readCell(memory, addr);
	}
	protected int readCell(byte[] memory, int addr) {
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
		boolean doExport = currentExport();
		alignDP();
		int entryAddr = getDP();
		int size = 0;
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
		if (existing != null) {
			logfile.println("*** Redefining " + name);
			System.err.println("*** Redefining " + name);
		}
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

		return entry;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.ITargetContext#resolveForward(v9t9.tools.forthcomp.DictEntry)
	 */
	@Override
	public void resolveForward(DictEntry entry) {
		String name = entry.getName().toUpperCase();
		ForwardRef ref = forwards.get(name);
		if (ref != null) {
			resolveForward(ref, entry);
			forwards.remove(name);
		}
		
	}

	public ITargetWord require(String token) {
		ITargetWord word = (ITargetWord) find(token);
		if (word == null) {
			word = defineForward(token, hostCtx.getStream().getLocation());
		}
		return word;
		//return (ITargetWord) super.require(token);
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
	public ITargetWord defineForward(String token, String location) {
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
		if (!littleEndian && cellBits == 16) {
			memory[offs++] = (byte) (cell >> 8);
			memory[offs++] = (byte) (cell & 0xff);
		} else {
			throw new UnsupportedOperationException();
		}
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
		writeCell(memory, addr, cell);
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
		initWordEntry();
		compileDoVar();
		stubData.use(bytes);
		int loc = alloc(bytes);
		entry.setCodeSize(loc - dp);
		final TargetVariable var = (TargetVariable) define(name, new TargetVariable(entry));
		return var;
	}

	/** Compile code that will push the parameter area's address */
	abstract protected void compileDoVar();



	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineColonWord(java.lang.String)
	 */
	@Override
	public TargetColonWord defineColonWord(String name) {
		DictEntry entry = createColonEntry(name);
		final TargetColonWord colon =  (TargetColonWord) define(name, new TargetColonWord(entry));
		return colon;
	}

	/**
	 * @param name
	 * @return
	 */
	protected DictEntry createColonEntry(String name) {
		DictEntry entry = defineEntry(name);
		initWordEntry();
		leaves.clear();
		return entry;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineConstant(java.lang.String, int, int)
	 */
	@Override
	public TargetConstant defineConstant(String name, int value, int cells) throws AbortException {
//		logfile.println("T> CONSTANT " + name);
		
		boolean mustDefine = currentExport();
		if (!mustDefine) {
			mustDefine = isForcedExport(name);
		}
		DictEntry entry;
		if (mustDefine) {
			entry = defineEntry(name);
			initWordEntry();
			if (isNativeDefinition())
				compileDoConstant(value, cells);
		} else {
			entry = new DictEntry(0, getDP(), name);
			exportFlagNext = false;
			// assume address
			//if (isLikelyAddress(value))
			symbols.put(value, name);
		}
		if (!isNativeDefinition() && mustDefine)
			compileDoConstant(value, cells);
		
		entry.setCodeSize(getDP() - entry.getAddr() - cells * cellSize);
		final TargetConstant constant = (TargetConstant) define(name, new TargetConstant(entry, value, 1));
		return constant;
	}
	
	/**
	 * @param value
	 * @return
	 */
	abstract protected boolean isLikelyAddress(int value);

	/**
	 * @param name
	 * @param forwardRef 
	 */
	protected void warnForcedWord(String name, ForwardRef forwardRef) {
		System.err.println("*** WARNING: forward reference to : " +name + " forces dictionary definition from "
				+ forwardRef.getLocation());
		
	}

	protected void defineSymbol(int addr, String name) {
		symbols.put(addr, name);
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

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#defineUser(java.lang.String, int)
	 */
	@Override
	public TargetUserVariable defineUser(String name, int bytes) throws AbortException {
		
//		logfile.println("T> USER " + name);
		
		// the "variable" will be frozen in ROM, a count of bytes
		TargetVariable up = findOrCreateVariable("UP0");
		int paramAddr = up.getEntry().getParamAddr();
		int offset = readCell(paramAddr);
		writeCell(paramAddr, offset + bytes);

		boolean mustDefine = currentExport();
		if (!mustDefine) {
			mustDefine = isForcedExport(name);
		}
		DictEntry entry;
		if (mustDefine) {
			entry = defineEntry(name);
			initWordEntry();
			
			if (isNativeDefinition())
				compileDoUser(offset);
		} else {
			entry = new DictEntry(0, getDP(), name);
			exportFlagNext = false;
			symbols.put(entry.getAddr(), name);
		}
		if (!isNativeDefinition())
			compileDoUser(offset);
		
		return (TargetUserVariable) define(name, new TargetUserVariable(entry, offset));
	}


	/**
	 * @param name
	 * @return
	 */
	protected boolean isForcedExport(String name) {
		ForwardRef forwardRef = forwards.get(name.toUpperCase());
		if (forwardRef != null) {
			warnForcedWord(name, forwardRef);
			return true;
		}
		return false;
	}

	@Override
	public TargetDefer defineRomDefer(String name) throws AbortException {
		
		// the defer location is an offset into a table initialized later
		DictEntry entry;
		entry = defineEntry(name);
		initWordEntry();
			
		// the "variable" will be frozen in ROM, a count of bytes
		int paramAddr = numRomDefersWord.getEntry().getParamAddr();
		int offset = readCell(paramAddr);
		int newOffset = offset + getCellSize();
		writeCell(paramAddr, newOffset);
		
		numRomDefersWord.setValue(newOffset);
		
		compileDoRomDefer(offset);
		
		TargetDefer defer = new TargetDefer(entry, offset);
		
		targetDefers.put(defer, null);
		
		return (TargetDefer) define(name, defer);
	}
	
	protected TargetVariable findOrCreateVariable(String name) {
		TargetVariable var = (TargetVariable) find(name);
		if (var == null) {
			setExportNext(false);
			var = create(name, getCellSize());
		}
		return var;
	}

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
	protected int doResolveRelocation(RelocEntry reloc) throws AbortException {
		if (reloc.type == RelocType.RELOC_ABS_ADDR_16)
			return reloc.target;
		throw abort("unhandled relocation: " + reloc);
	}


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
	
	/**
	 * @return the logfile
	 */
	public PrintStream getLog() {
		return logfile;
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
		if (getLatest() != null && ((ITargetWord) getLatest()).getEntry().hasLocals())
			compileCleanupLocals(hostContext);
		
		//buildCall(require(";S"));
		//require(";S").getCompilationSemantics().execute(hostContext, this);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileToValue(v9t9.tools.forthcomp.HostContext, v9t9.tools.forthcomp.words.TargetValue)
	 */
	@Override
	public void compileToValue(HostContext hostContext, TargetValue word) throws AbortException {
		compileWordParamAddr(word);
		require("!").getCompilationSemantics().execute(hostContext, this);

	}
	public void compileToRomDefer(HostContext hostContext, TargetDefer word) throws AbortException {
		compileWordParamAddr(word);
		require("!").getCompilationSemantics().execute(hostContext, this);
	}

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
	 * @see v9t9.tools.forthcomp.words.ITargetContext#compileString(v9t9.tools.forthcomp.HostContext, java.lang.String)
	 */
	@Override
	public Pair<Integer, Integer> buildPushString(HostContext hostContext, String string) throws AbortException {
		ITargetWord parenString = (ITargetWord)require("(s\")");
		buildCall((ITargetWord) parenString);
		Pair<Integer, Integer> info = writeLengthPrefixedString(string);
		setDP(getDP() + info.second);
		return info;
	}

	public void buildXt(int addr) {
		if (isNativeDefinition()) {
			assert false;
		} else {
			int ptr = alloc(cellSize);
			
			int reloc = addRelocation(ptr, 
					RelocType.RELOC_ABS_ADDR_16,
					addr);

			writeCell(ptr, reloc);
		}
	}

	public void buildXt(ITargetWord word) {
		if (isNativeDefinition()) {
			compileTick(word);
		} else {
			logfile.println("T>" + HexUtils.toHex4(getDP()) + " = " + word.getName());
			buildXt(word.getEntry().getContentAddr());
		}
	}
	public void buildCall(ITargetWord word) throws AbortException {
		if (isNativeDefinition()) {
			compile(word);
		} else {
			buildXt(word);
		}
	}
	public void buildLiteral(int val, boolean isUnsigned, boolean optimize) throws AbortException {
		if (isNativeDefinition()) {
			compileLiteral(val, isUnsigned, optimize);
		} else {
			IWord word = find(Integer.toString(val));
			if (word instanceof ITargetWord) {
				buildCall((ITargetWord) word);
			} else {
				buildCall(require("DOLIT"));
				buildCell(val);
			}
		}
	}
	public void buildDoubleLiteral(int valLo, int valHi, boolean isUnsigned, boolean optimize) throws AbortException {
		if (isNativeDefinition()) {
			compileDoubleLiteral(valLo, valHi, isUnsigned, optimize);
		} else {
			buildCall(require("DODLIT"));
			buildCell(valLo);
			buildCell(valHi);
		}
	}
	public void buildTick(ITargetWord word) throws AbortException {
		if (isNativeDefinition()) {
			compileTick(word);
		} else {
			buildCall(require("DOLIT"));
			buildXt(word);
		}
	}
	public void buildUser(TargetUserVariable user) throws AbortException {
		if (isNativeDefinition()) {
			compileDoUser(user.getOffset());
		} else {
			buildCall(require("DOUSER"));
			buildCell(user.getOffset());
		}
	}
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
	

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#pushLeave(v9t9.forthcomp.HostContext)
	 */
	@Override
	public void pushLeave(HostContext hostContext) {
		// add fixup to a list
		pushFixup(hostContext);
		leaves.add(hostContext.popData());
	}
	abstract protected int writeJump(int opAddr, int target)
			throws AbortException;
	abstract protected void writeJumpAlloc(int target, boolean conditional)
			throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#pushFixup()
	 */
	@Override
	public void pushFixup(HostContext hostContext) {
		// a fixup needs the memory loc of the offset to update
		// as well as the original PC of the referring instruction
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		
		hostContext.markFixup(nextDp);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#pushHere(v9t9.forthcomp.HostContext)
	 */
	@Override
	public int pushHere(HostContext hostContext) {
		int nextDp = getDP();
		hostContext.pushData(nextDp);
		hostContext.markFixup(nextDp);
		return nextDp;
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#swapFixup()
	 */
	@Override
	public void swapFixup(HostContext hostContext) {
		int d0 = hostContext.popData();
		int e0 = hostContext.popData();
		hostContext.pushData(d0);
		hostContext.pushData(e0);
	}
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#resolveFixup()
	 */
	@Override
	public void resolveFixup(HostContext hostContext) throws AbortException {
		int nextDp = getDP();
		int opAddr = hostContext.popData();
		//int diff = nextDp - opAddr;
		
		writeJump(opAddr, nextDp);
		
		hostContext.resolveFixup(opAddr, nextDp);

	}


	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#resolveFixup()
	 */
	@Override
	public void compileBack(HostContext hostContext, boolean conditional) throws AbortException {
		//int nextDp = getDP();
		int opAddr = hostContext.popData();
		
		writeJumpAlloc(opAddr, conditional);
		
		//hostContext.build(new Host0Branch(hostContext.require("0branch"));
	}


	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#createMemory()
	 */
	@Override
	public MemoryDomain createMemory() {
		console = new MemoryDomain(IMemoryDomain.NAME_CPU, false);
		EnhancedRamByteArea bigRamArea = new EnhancedRamByteArea(0, 0x10000); 
		MemoryEntry bigRamEntry = new MemoryEntry("RAM", console, 0, MemoryDomain.PHYSMEMORYSIZE, 
				bigRamArea);
		console.mapEntry(bigRamEntry);
		return console;
	}

	/**
	 * @return
	 */
	public IMemoryDomain getMemory() {
		return console;
	}




	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileAddr(int)
	 */
	@Override
	public void buildCell(int loc) {
		int ptr = alloc(cellSize);
		writeCell(ptr, loc);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#compileChar(int)
	 */
	@Override
	public void buildChar(int val) {
		int ptr = alloc(1);
		writeChar(ptr, val);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.forthcomp.ITargetContext#isNativeDefinition()
	 */
	@Override
	public boolean isNativeDefinition() {
		return getLatest() instanceof INativeCodeWord;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.TargetContext#loopCompile(v9t9.forthcomp.HostContext, v9t9.forthcomp.ITargetWord)
	 */
	@Override
	public void loopCompile(HostContext hostCtx, ITargetWord loopCaller)
			throws AbortException {
		hostCtx.compileWord(this, null, loopCaller);
		
		boolean isQDo = hostCtx.popData() != 0;

		int opAddr = hostCtx.popData();
		writeLoopJump(opAddr);
		
		if (isQDo) {
			// then comes here
			resolveFixup(hostCtx);
		}
		
		for (int i = 0; i < leaves.size(); i++) {
			hostCtx.pushData(leaves.get(i));
			resolveFixup(hostCtx);
		}
		leaves.clear();
		
		ITargetWord unloop = require("unloop");
		hostCtx.compileWord(this, null, unloop);
		//unloop.getCompilationSemantics().execute(hostCtx, this);
	}

	abstract protected void writeLoopJump(int opAddr) throws AbortException;

	/**
	 * @param token
	 * @param tokenStream
	 * @throws AbortException 
	 */
	public void parse(String token) throws AbortException {
		IWord word = null;
		
		int state = hostCtx.readVar("state");
		
		if (state == 0) {
			word = hostCtx.find(token);
			if (word == null) {
				word = find(token);
			} 
			if (word instanceof ITargetWord && ((ITargetWord) word).getEntry().isTargetOnly()) {
				word = null;
			}
			if (word == null) {
				word = parseLiteral(token);
			}
			if (word == null) {
				throw abort("unknown word or literal: " + token);
			}
			
			if (word.getInterpretationSemantics() == null)
				throw abort(word.getName() + " has no interpretation semantics");
			
//			if (HostContext.DEBUG) System.out.println(
//					//stack() + "\n" + 
//					"H> exec " + word);

			word.getInterpretationSemantics().execute(hostCtx, this);
		} else {
			word = find(token);
			if (word == null) {
				word = hostCtx.find(token);
			}
			if (word == null) {
				word = parseLiteral(token);
			}
			if (word == null) {
				word = defineForward(token, hostCtx.getStream().getLocation());
			}
		
			ITargetWord targetWord = null;
			IWord hostWord = null;
			
			if (word instanceof ITargetWord) {
				targetWord = (ITargetWord) word;
				hostWord = hostCtx.find(token);
				if (hostWord == null) 
					hostWord = targetWord;
			} else {
				if (word.getCompilationSemantics() == null) {
					throw hostCtx.abort("host word " + token + " used instead of target word");
				}
				hostWord = word;
				targetWord = null;
				if (!word.isCompilerWord()) {
					targetWord = (ITargetWord) defineForward(token, 
							hostCtx.getStream().getLocation());
					//throw hostContext.abort("host word " + token + " used instead of target word");
				}
			}		
			
			hostCtx.compileWord(this, hostWord, targetWord);
		}
		
	}

	private AbortException abort(String string) {
		return hostCtx.getStream().abort(string);
	}

	/**
	 * @param doTest
	 */
	public void setTestMode(boolean doTest) {
		this.testMode = doTest;
	}
	/**
	 * @return the testMode
	 */
	public boolean isTestMode() {
		return testMode;
	}

	public void setDeferTarget(TargetDefer defer, int addr) {
		for (DictEntry entry : dictEntryMap.values()) {
			if (entry.getContentAddr() == addr) {
				defer.setHostPc(entry.getTargetWord().getHostDp());
				break;
			}
		}
		targetDefers.put(defer, addr);
	}
	/**
	 * 
	 */
	public void resolveRomDefers() throws AbortException {
		ITargetWord to = null;
		ITargetWord hang = null;
		
		for (Map.Entry<TargetDefer, Integer> defEnt : targetDefers.entrySet()) {
			Integer target = defEnt.getValue();
			if (target == null) {
				System.err.println("DEFER'ed word " + defEnt.getKey() + " not assigned with TO");
				if (hang == null) {
					hang = require("HANG");
				}
				target = hang.getEntry().getContentAddr();
			}
			if (to == null) {
				to = require("(TO)");
			}
			buildLiteral(target, true, false);
			buildTick(defEnt.getKey());
			buildCall(to);
		}
	}

	/**
	 * @param index
	 * @throws AbortException 
	 */
	public void buildFromLocal(int index) throws AbortException {
		if (isNativeDefinition())
			compileFromLocal(index);
		else {
			buildCall(require("(LOCAL@)"));
			buildCell(index);
		}
	}
	public void buildToLocal(int index) throws AbortException {
		if (isNativeDefinition())
			compileToLocal(index);
		else {
			buildCall(require("(LOCAL!)"));
			buildCell(index);
		}
	}

	/**
	 * @param hostContext
	 * @param word
	 * @throws AbortException 
	 */
	public void buildToValue(HostContext hostContext, TargetValue word) throws AbortException {
		if (isNativeDefinition())
			compileToValue(hostContext, word);
		else {
			buildXt(word);
			buildCall(require("(TO)"));
		}
	}

	/**
	 * @param hostContext
	 * @param word
	 * @throws AbortException 
	 */
	public void buildToRomDefer(HostContext hostContext, TargetDefer word) throws AbortException {
		if (isNativeDefinition())
			compileToRomDefer(hostContext, word);
		else {
			buildXt(word);
			buildCall(require("(RDEFER!)"));
		}
		
	}


	public TargetCodeWord defineCodeWord(String name) {
		TargetCodeWord word = (TargetCodeWord) define(name, new TargetCodeWord(defineEntry(name)));
		alignDP();
//		word.setDP(getDP());
		return word;
	}

	public void disassemble(TargetCodeWord word) {
		int start = word.getEntry().getContentAddr();
		int len = word.getEntry().getCodeSize();
		int end = start + len;
		for (int addr = start; addr < end; addr++) {
			console.flatWriteByte(addr, (byte) readChar(addr));
		}
		for (int addr = start; addr < end; ) {
			RawInstruction instr = rawInstructionFactory.decodeInstruction(addr, console);
			logfile.println("T>" + HexUtils.toHex4(instr.pc) + " " + instr);
			addr += instr.getSize();
		}
	}

	/**
	 * @param hostContext
	 * @throws AbortException 
	 */
	public void buildExit(HostContext hostContext) throws AbortException {
		if (isNativeDefinition()) {
			compileExit(hostContext);
		} else {
			buildCall(require(";S"));
		}
	}

	

	
}
