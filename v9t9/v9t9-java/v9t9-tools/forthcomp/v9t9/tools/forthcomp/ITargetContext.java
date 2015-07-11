/*
  ITargetContext.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.forthcomp.RelocEntry.RelocType;
import v9t9.tools.forthcomp.words.TargetCodeWord;
import v9t9.tools.forthcomp.words.TargetColonWord;
import v9t9.tools.forthcomp.words.TargetConstant;
import v9t9.tools.forthcomp.words.TargetDefer;
import v9t9.tools.forthcomp.words.TargetUserVariable;
import v9t9.tools.forthcomp.words.TargetValue;
import v9t9.tools.forthcomp.words.TargetVariable;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public interface ITargetContext {

	void setHostContext(HostContext hostCtx);

	// define the minimal prims to be able to compile colon defs
	void defineColonPrims() throws AbortException;
	// define the remaining prims, for bootstrapping
	void definePrims() throws AbortException;

	int getCellSize();
	
	/** read the value in memory */
	int readCell(int addr);
	void writeCell(int addr, int cell);

	/** read the addr in memory, which may be a relocation
	 * @return negative value for relocation, else literal value */
	int readAddr(int addr);

	int writeCell(byte[] memory, int offs, int cell);

	int readChar(int addr);
	void writeChar(int addr, int ch);




	int addRelocation(int addr, RelocType type, int target);

	int getDP();

	void setDP(int dp);

	/** add bytes, return addr of allocation */
	int alloc(int size);

	/** add #cell bytes, return addr of allocation */
	int allocCell();

	int resolveAddr(int relocIndex);

	int findReloc(int addr);

	void removeReloc(int addr);

	RelocEntry getRelocEntry(int id);

	/**
	 * @param name
	 * @return
	 */
	DictEntry defineEntry(String name);

	/**
	 * @return the lastEntry
	 */
	DictEntry getLastEntry();

	/**
	 * @param token
	 * @return
	 */
	ITargetWord defineForward(String token, String location);
	void resolveForward(DictEntry entry);

	void alignDP();

	int getAlignedDP();

	int align(int bytes);

	void defineCompilerWords(HostContext hostContext);

	void setExport(boolean export);

	boolean isExport();

	void setExportNext(boolean export);

	void dumpDict(PrintStream out, int from, int to);

	/**
	 * @return
	 */
	int getBaseDP();

	/**
	 * @param baseDP the baseDP to set
	 */
	void setBaseDP(int baseDP);

	/**
	 * @param logfile
	 */
	void setLog(PrintStream logfile);

	Collection<ForwardRef> getForwardRefs();

	Map<String, DictEntry> getTargetDictionary();


	TargetVariable create(String name, int bytes);

	TargetColonWord defineColonWord(String name);

	TargetConstant defineConstant(String name, int value, int cells)
			throws AbortException;

	TargetValue defineValue(String name, int value, int cells)
			throws AbortException;
	
	TargetUserVariable defineUser(String name, int bytes) throws AbortException;

	/**
	 * Define a deferred word, whose initial execution semantics cause an error.
	 * @param name
	 */
	TargetDefer defineRomDefer(String name) throws AbortException;


	/**
	 * Compile code for (DOVALUE)
	 * 
	 * At runtime, push the # of cells to the stack from the current DP.
	 * Return the location of the value.  The exact space must be allocated
	 * so the value can change.
	 * @param cells
	 * @param value
	 * @return DP of value
	 */
	int compilePushValue(int cells, int value) throws AbortException;

	/** 
	 * Compile code for (DOCONSTANT)
	 * 
	 * At runtime, push the value in the given # of cells to the stack. 
	 * Can be optimized.
	 * @param value
	 * @param cells
	 * @throws AbortException
	 */
	void compileDoConstant(int value, int cells) throws AbortException;

	/** 
	 * Compile code for (DOUSER)
	 * 
	 * At runtime, push the user variable for the given offset into the current user table. 
	 * Can be optimized.
	 */
	void compileDoUser(int offset) throws AbortException;

	/**
	 * Initialize the current entry to accept code.  E.g. align the DP.
	 */
	void initWordEntry();

	/**
	 * Compile the execution semantics of a word onto the current code entry.
	 * @throws AbortException 
	 */
	void compile(ITargetWord word) throws AbortException;

	/**
	 * Compile code that pushes the given literal onto the stack.
	 * @param value
	 * @param isUnsigned
	 * @param optimize if true, use whatever means to push the literal.
	 * Otherwise, emit a well-known format where (for instance) the
	 * literal can be changed later.
	 */
	void compileLiteral(int value, boolean isUnsigned, boolean optimize);

	/**
	 * Compile code that pushes the given literal as a double onto the stack.
	 * @param valueLo next-to-top entry
	 * @param valueHi top entry
	 * @param isUnsigned
	 * @param optimize if true, use whatever means to push the literal.
	 * Otherwise, emit a well-known format where (for instance) the
	 * literal can be changed later.
	 */
	void compileDoubleLiteral(int valueLo, int valiueHi, boolean isUnsigned,
			boolean optimize);

	/**
	 * Flatten memory and resolve addresses
	 * @param console
	 * @throws AbortException 
	 */
	void exportMemory(IMemoryDomain console) throws AbortException;

	/**
	 * Flatten memory and resolve addresses
	 * @param console
	 */
	void importMemory(IMemoryDomain console);

	/**
	 * here 0 ,
	 */
	void pushFixup(HostContext hostContext);

	/**
	 * here 
	 */
	int pushHere(HostContext hostContext);

	/**
	 * swap
	 */
	void swapFixup(HostContext hostContext);

	/**
	 * here over - swap !
	 */
	void resolveFixup(HostContext hostContext) throws AbortException;

	void compileBack(HostContext hostContext, boolean conditional) throws AbortException;

	void clearDict();

	/** append cell value to dictionary */
	void buildCell(int val);

	/** append char value to dictionary */
	void buildChar(int val);

	/** compile code that pushes the address of the word */
	void compileTick(ITargetWord word);

	/** for TO value */
	void compileWordParamAddr(ITargetWord word);

	void pushLeave(HostContext hostContext);

	void loopCompile(HostContext hostCtx, ITargetWord loopCaller)
			throws AbortException;

	boolean isLocalSupportAvailable(HostContext hostContext)
			throws AbortException;

	/** Compile code for (LOCALS:) */
	void compileSetupLocals(HostContext hostContext) throws AbortException;

	/** Compile code for (LOCALS.) */
	void compileAllocLocals(int count) throws AbortException;

	/** Compile code for (LOCAL) */
	void compileLocalAddr(int index);

	/** Compile code for (LOCAL@) */
	void compileFromLocal(int index) throws AbortException;

	/** Compile code for (LOCAL!) */
	void compileToLocal(int index) throws AbortException;

	/** Compile code for (;LOCALS) */
	void compileCleanupLocals(HostContext hostContext) throws AbortException;

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.Context#find(java.lang.String)
	 */
	IWord find(String token);

	/**
	 * @param hostContext TODO
	 * @throws AbortException 
	 * 
	 */
	void compileExit(HostContext hostContext) throws AbortException;

	/**
	 * @param hostContext TODO
	 * @param word
	 * @throws AbortException 
	 */
	void compileToValue(HostContext hostContext, TargetValue word)
			throws AbortException;

	MemoryDomain createMemory();

	void exportState(HostContext hostCtx, IBaseMachine machine, int baseSp,
			int baseRp, int baseUp) throws AbortException;

	void importState(HostContext hostCtx, IBaseMachine machine, int baseSp,
			int baseRp);

	/**
	 * @param string
	 * @return pair of the address of start, plus the total length
	 */
	Pair<Integer, Integer> writeLengthPrefixedString(String string)
			throws AbortException;

	/**
	 * 
	 */
	void markHostExecutionUnsupported();

	boolean isNativeDefinition();
	
	/** Ensure the XT of the given word is appended to the dictionary entry */
	void buildXt(ITargetWord word);
	
	void compileCall(ITargetWord word);

	/** In CREATE'd word dictEntry, change the execution behavior to invoke targetDP 
	 *
	 * @param hostContext
	 * @param dictEntry CREATE'd word
	 * @param targetDP the XT of the DOES> behavior
	 */
	void compileDoes(HostContext hostContext, DictEntry dictEntry, int targetDP)
			throws AbortException;

	Pair<Integer, Integer> buildPushString(HostContext hostContext, String string)
			throws AbortException;

	void compileUser(TargetUserVariable var);

	int getUP();

	void dumpStubs(PrintStream logfile);

	IWord parseLiteral(String token);

	/**
	 * Prepare for DOES>
	 * @param hostContext
	 * @return target addr for DOES
	 */
	int buildDoes(HostContext hostContext) throws AbortException;

	/**
	 * @param offset
	 */
	void compileDoRomDefer(int offset);

	/**
	 * @param hostContext
	 * @param word
	 * @throws AbortException
	 */
	void compileToRomDefer(HostContext hostContext, TargetDefer word)
			throws AbortException;

	/**
	 * @return
	 */
	boolean isTestMode();

	/**
	 * @param name
	 * @return
	 */
	TargetCodeWord defineCodeWord(String name);
	void compileEndCode();

	/**
	 * @return
	 */
	PrintStream getLog();

}