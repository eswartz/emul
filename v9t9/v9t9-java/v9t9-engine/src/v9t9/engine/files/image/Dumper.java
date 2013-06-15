/*
  Dumper.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.files.image;

import java.io.PrintWriter;
import java.text.MessageFormat;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;
import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;

/**
 * @author ejs
 *
 */
public class Dumper {
	private IProperty settingDumpFull;
	private IProperty settingDump;

	public Dumper(IProperty dump, IProperty dumpFull) {
		settingDump = dump;
		settingDumpFull = dumpFull;
        Logging.registerLog(settingDump, "disk_debug.txt");
        Logging.registerLog(settingDumpFull, "disk_debug.txt");

	}
	public Dumper(ISettingsHandler settings, SettingSchema dump, SettingSchema dumpFull) {
		this(settings.get(dump), settings.get(dumpFull));
	}

	public void error(String fmt, Object... args) {
		error(MessageFormat.format(fmt, args));
	}

	public void info(String string) {
		PrintWriter full = Logging.getLog(settingDumpFull);
		if (full != null) {
			full.println(string);
			full.flush();
		}
		PrintWriter dump = Logging.getLog(settingDump);
		if (dump != null) {
			dump.println(string);
			dump.flush();
		}
		
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
		if (!settingDump.getBoolean() && !settingDumpFull.getBoolean())
			return false;
		PrintWriter full = Logging.getLog(settingDumpFull);
		PrintWriter dump = Logging.getLog(settingDump);
		return (full != null || dump != null);
	}

}
