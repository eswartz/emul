/*
  Settings.java

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
package v9t9.common.settings;

import ejs.base.properties.IProperty;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.machine.IBaseMachine;

/**
 * @author ejs
 *
 */
public class Settings {

	public static ISettingsHandler getSettings(ICpu cpu) {
		return getSettings(cpu.getMachine());
	}
	public static ISettingsHandler getSettings(IBaseMachine machine) {
		return machine.getSettings();
	}
	public static IProperty get(ICpu cpu, SettingSchema schema) {
		return getSettings(cpu).get(schema);
	}
	public static IProperty get(IBaseMachine machine,
			SettingSchema schema) {
		return getSettings(machine).get(schema);
	}
}
