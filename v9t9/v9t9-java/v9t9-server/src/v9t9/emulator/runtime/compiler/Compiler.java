/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import org.ejs.coffee.core.properties.SettingProperty;

import v9t9.engine.HighLevelCodeInfo;
import v9t9.engine.cpu.RawInstruction;

/**
 * @author Ed
 *
 */
public abstract class Compiler {

	static public final SettingProperty settingOptimize = new SettingProperty("CompilerOptimize",
	new Boolean(false));
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
	abstract public byte[] compile(String uniqueClassName, String baseName,
			HighLevelCodeInfo highLevel, RawInstruction[] insts, short[] entries);
	/**
	 * Tell if the CPU is coherent and compilation makes sense
	 * @return
	 */
	abstract public boolean validCpuState();

}
