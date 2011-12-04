/**
 * 
 */
package v9t9.common.compiler;

import v9t9.base.properties.SettingProperty;
import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.RawInstruction;
import v9t9.common.memory.IMemoryEntry;

/**
 * @author ejs
 *
 */
public interface ICompiler {

	static public final SettingProperty settingOptimize = new SettingProperty(
			"CompilerOptimize", new Boolean(false));
	static public final SettingProperty settingOptimizeRegAccess = new SettingProperty(
			"CompilerOptimizeRegAccess", new Boolean(false));
	static public final SettingProperty settingOptimizeStatus = new SettingProperty(
			"CompilerOptimizeStatus", new Boolean(false));
	static public final SettingProperty settingCompileOptimizeCallsWithData = new SettingProperty(
			"CompilerOptmizeCallsWithData", new Boolean(false));
	static public final SettingProperty settingDebugInstructions = new SettingProperty(
			"DebugInstructions", new Boolean(false));
	static public final SettingProperty settingCompileFunctions = new SettingProperty(
			"CompilerCompileFunctions", new Boolean(false));

	/**
	 * Compile the instructions into bytecode.
	 * @param uniqueClassName
	 * @param baseName
	 * @param highLevel
	 * @param insts
	 * @param entries
	 * @return
	 */
	byte[] compile(String uniqueClassName, String baseName,
			IDecompileInfo highLevel, RawInstruction[] insts, short[] entries);

	/**
	 * Tell if the CPU is coherent and compilation makes sense
	 * @return
	 */
	boolean validCpuState();

	/** Currently, only gather high-level info for one memory entry at a time */
	IDecompileInfo getHighLevelCode(IMemoryEntry entry);

}