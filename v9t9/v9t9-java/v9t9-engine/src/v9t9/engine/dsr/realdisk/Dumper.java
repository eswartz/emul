/**
 * 
 */
package v9t9.engine.dsr.realdisk;

import java.io.PrintWriter;
import java.text.MessageFormat;

import v9t9.base.settings.Logging;
import v9t9.base.settings.SettingProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.engine.cpu.Executor;

/**
 * @author ejs
 *
 */
public class Dumper {
	private SettingProperty settingDumpFull;
	private SettingProperty settingDebug;

	/**
	 * @param settings
	 */
	public Dumper(ISettingsHandler settings) {
		settingDebug = settings.get(RealDiskDsrSettings.diskImageDebug);
		settingDumpFull = settings.get(ICpu.settingDumpFullInstructions);

	}

	public void error(String fmt, Object... args) {
		error(MessageFormat.format(fmt, args));
	}

	public void info(String string) {
		PrintWriter full = Logging.getLog(settingDumpFull);
		if (full != null)
			full.println(string);
		if (settingDebug.getBoolean())
			System.out.println(string);
		
	}

	public void info(String fmt, Object... args) {
		info(MessageFormat.format(fmt, args));
		
	}

	public void error(String string) {
		PrintWriter full = Logging.getLog(settingDumpFull);
		if (full != null)
			full.println(string);
		System.err.println(string);
		
	}

	/**
	 * @return
	 */
	public boolean isEnabled() {
		return settingDebug.getBoolean() || settingDumpFull.getBoolean();
	}

}
