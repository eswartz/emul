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

	SettingSchema settingOptimize = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerOptimize", Boolean.FALSE);
	SettingSchema settingOptimizeRegAccess = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerOptimizeRegAccess", Boolean.TRUE);
	SettingSchema settingOptimizeStatus = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerOptimizeStatus", Boolean.FALSE);
	SettingSchema settingCompileOptimizeCallsWithData = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerOptmizeCallsWithData", Boolean.FALSE);
	SettingSchema settingDebugInstructions = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"DebugInstructions", Boolean.TRUE);
	SettingSchema settingCompileFunctions = new SettingSchema(
			ISettingsHandler.MACHINE,
			"CompilerCompileFunctions", Boolean.FALSE);

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