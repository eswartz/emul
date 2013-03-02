/*
  BaseSettingsHandler.java

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
package v9t9.common.settings;

import java.util.HashMap;
import java.util.Map;

import ejs.base.properties.IProperty;

import v9t9.common.client.ISettingsHandler;

/**
 * @author ejs
 *
 */
public class BaseSettingsHandler implements ISettingsHandler {

	private IStoredSettings transientSettings = new BaseStoredSettings(
			ISettingsHandler.TRANSIENT) {
	
			@Override
			public String getConfigFileName() {
				return null;
			}
		};
	protected final IStoredSettings machineSettings;
	protected final IStoredSettings userSettings;

	
	public BaseSettingsHandler(IStoredSettings machineSettings,
			IStoredSettings userSettings) {
		this.machineSettings = machineSettings;
		this.userSettings = userSettings;
		
		machineSettings.setOwner(this);
		userSettings.setOwner(this);
		transientSettings.setOwner(this);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.ISettingsHandler#getRegisteredSettings()
	 */
	@Override
	public Map<IProperty, SettingSchema> getAllSettings() {
		Map<IProperty, SettingSchema> ret = new HashMap<IProperty, SettingSchema>();
		ret.putAll(transientSettings.getAll());
		ret.putAll(userSettings.getAll());
		ret.putAll(machineSettings.getAll());
		return ret;
	}

	@Override
	public IStoredSettings getMachineSettings() {
		return machineSettings;
	}

	@Override
	public IStoredSettings getUserSettings() {
		return userSettings;
	}

	@Override
	public IProperty get(SettingSchema schema) {
		return getSettings(schema.getContext()).findOrCreate(schema);
	}

	@Override
	public <T extends IProperty> T get(String context, T defaultProperty) {
		return getSettings(context).findOrCreate(defaultProperty);
	}

	/**
	 * @param def
	 * @return
	 */
	protected IStoredSettings getSettings(String context) {
		IStoredSettings settings;
		if (MACHINE.equals(context))
			settings = getMachineSettings();
		else if (USER.equals(context))
			settings = getUserSettings();
		else if (TRANSIENT.equals(context))
			settings = transientSettings;
		else
			throw new IllegalArgumentException("unknown settings context: " + context);
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.ISettingsHandler#findSettingStorage(java.lang.String)
	 */
	@Override
	public IStoredSettings findSettingStorage(String settingsName) {
		if (machineSettings.find(settingsName) != null)
			return machineSettings;
		if (userSettings.find(settingsName) != null)
			return userSettings;
		if (transientSettings.find(settingsName) != null)
			return transientSettings;
		return null;
	}

}