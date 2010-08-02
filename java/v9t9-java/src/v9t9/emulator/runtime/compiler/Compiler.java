/**
 * 
 */
package v9t9.emulator.runtime.compiler;

import org.ejs.coffee.core.properties.SettingProperty;

/**
 * @author Ed
 *
 */
public class Compiler {

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

}
