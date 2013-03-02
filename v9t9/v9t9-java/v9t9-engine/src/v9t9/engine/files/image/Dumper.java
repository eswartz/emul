/*
  Dumper.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.engine.files.image;

import java.io.PrintWriter;
import java.text.MessageFormat;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;

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
	}
	public Dumper(ISettingsHandler settings, SettingSchema dump, SettingSchema dumpFull) {
		settingDump = settings.get(dump);
		settingDumpFull = settings.get(dumpFull);
	}

	public void error(String fmt, Object... args) {
		error(MessageFormat.format(fmt, args));
	}

	public void info(String string) {
		PrintWriter full = Logging.getLog(settingDumpFull);
		if (full != null)
			full.println(string);
		PrintWriter dump = Logging.getLog(settingDump);
		if (dump != null)
			dump.println(string);
		
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
		return settingDump.getBoolean() || settingDumpFull.getBoolean();
	}

}
