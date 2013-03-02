/*
  ISettingsHandler.java

  (c) 2011-2013 Edward Swartz

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
package v9t9.common.client;

import java.util.Map;

import ejs.base.properties.IProperty;

import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs 
 *
 */
public interface ISettingsHandler {
	String GLOBAL = "Global";
	String MACHINE = "Machine";
	String USER = "User";
	String TRANSIENT = "Transient";
	
	IProperty get(SettingSchema schema);
	<T extends IProperty> T get(String context, T defaultProperty);
	
	Map<IProperty, SettingSchema> getAllSettings();
	
	IStoredSettings getMachineSettings();
	IStoredSettings getUserSettings();
	
	IStoredSettings findSettingStorage(String settingsName);
}
