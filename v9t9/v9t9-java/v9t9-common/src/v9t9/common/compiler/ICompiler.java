/**
 * 
 */
package v9t9.common.compiler;

import v9t9.common.asm.IDecompileInfo;
import v9t9.common.asm.RawInstruction;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public interface ICompiler {

	static public final SettingSchema settingOptimize = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"CompilerOptimize", new Boolean(false));
	static public final SettingSchema settingOptimizeRegAccess = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"CompilerOptimizeRegAccess", new Boolean(true));
	static public final SettingSchema settingOptimizeStatus = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"CompilerOptimizeStatus", new Boolean(false));
	static public final SettingSchema settingCompileOptimizeCallsWithData = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"CompilerOptmizeCallsWithData", new Boolean(false));
	static public final SettingSchema settingDebugInstructions = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DebugInstructions", new Boolean(false));
	static public final SettingSchema settingCompileFunctions = new SettingSchema(
			ISettingsHandler.WORKSPACE,
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