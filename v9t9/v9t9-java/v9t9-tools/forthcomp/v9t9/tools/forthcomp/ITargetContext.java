/**
 * 
 */
package v9t9.tools.forthcomp;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.memory.MemoryDomain;
import v9t9.tools.forthcomp.RelocEntry.RelocType;
import v9t9.tools.forthcomp.words.TargetColonWord;
import v9t9.tools.forthcomp.words.TargetConstant;
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

	void defineBuiltins() throws AbortException;

	/** read the value in memory */
	int readCell(int addr);

	/** read the addr in memory, which may be a relocation
	 * @return negative value for relocation, else literal value */
	int readAddr(int addr);

	int addRelocation(int addr, RelocType type, int target);

	int getDP();

	/**
	 * @param dp the dp to set
	 */
	void setDP(int dp);

	int alloc(int size);

	/**
	 * @return
	 */
	int allocCell();

	/**
	 * @param relocIndex
	 * @return
	 */
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
	IWord defineForward(String token, String location);

	/**
	 * 
	 */
	void alignDP();

	int getAlignedDP();

	int align(int bytes);

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.words.TargetContext#convertCell(int)
	 */
	int writeCell(byte[] memory, int offs, int cell);

	void writeCell(int addr, int cell);

	void writeChar(int addr, int ch);

	int getCellSize();

	int readChar(int addr);

	TargetVariable create(String name, int bytes);

	TargetColonWord defineColonWord(String name);

	TargetConstant defineConstant(String name, int value, int cells)
			throws AbortException;

	TargetValue defineValue(String name, int value, int cells)
			throws AbortException;

	/**
	 * At runtime, push the # of cells to the stack from the current DP.
	 * Return the location of the value.  The exact space must be allocated
	 * so the value can change.
	 * @param cells
	 * @param value
	 * @return DP of value
	 */
	int compilePushValue(int cells, int value) throws AbortException;

	/** At runtime, push the value in the given # of cells to the stack. 
	 * Can be optimized.
	 * @param value
	 * @param cells
	 * @throws AbortException
	 */
	void compileDoConstant(int value, int cells) throws AbortException;

	/** At runtime, push the user variable for the given index. 
	 * Can be optimized.
	 */
	void compileDoUser(int index) throws AbortException;

	TargetUserVariable defineUser(String name, int bytes) throws AbortException;

	void initCode();

	void alignBranch();

	/**
	 * Compile a word onto the current dictionary entry
	 */
	void compile(ITargetWord word);

	void compileLiteral(int value, boolean isUnsigned, boolean optimize);

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
	 * @throws AbortException TODO
	 */
	void resolveFixup(HostContext hostContext) throws AbortException;

	void compileBack(HostContext hostContext, boolean conditional)
			throws AbortException;

	void clearDict();

	/** compile cell value */
	void compileCell(int val);

	void compileChar(int val);

	/** compile address */
	void compileTick(ITargetWord word);

	void compileWordParamAddr(TargetValue word);

	void pushLeave(HostContext hostContext);

	void loopCompile(HostContext hostCtx, ITargetWord loopCaller)
			throws AbortException;

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

	/**
	 * @return
	 */
	Collection<ForwardRef> getForwardRefs();

	/**
	 * @return
	 */
	Map<String, DictEntry> getTargetDictionary();

	boolean isLocalSupportAvailable(HostContext hostContext)
			throws AbortException;

	void ensureLocalSupport(HostContext hostContext) throws AbortException;

	void compileSetupLocals(HostContext hostContext) throws AbortException;

	void compileAllocLocals(int count) throws AbortException;

	void compileLocalAddr(int index);

	void compileFromLocal(int index) throws AbortException;

	void compileToLocal(int index) throws AbortException;

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

	void compileCall(ITargetWord word);

	void compilePostpone(ITargetWord word) throws AbortException;

	void compileDoes(HostContext hostContext, DictEntry dictEntry, int targetDP)
			throws AbortException;

	void compileString(HostContext hostContext, String string)
			throws AbortException;

	/**
	 * Prepare for DOES>
	 * @param hostContext
	 * @return target addr for DOES
	 */
	int compileDoDoes(HostContext hostContext) throws AbortException;

	/**
	 * @param opcode
	 */
	void compileOpcode(int opcode);

	void compileUser(TargetUserVariable var);

	/**
	 * @return
	 */
	int getUP();

	void dumpStubs(PrintStream logfile);

	IWord parseLiteral(String token);

}