/*
  Settings.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
