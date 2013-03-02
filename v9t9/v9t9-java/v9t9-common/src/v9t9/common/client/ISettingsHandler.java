/*
  ISettingsHandler.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
