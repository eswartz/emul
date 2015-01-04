/*
  IDeviceIndicatorProvider.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.dsr;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public interface IDeviceIndicatorProvider {
	
	/** Modify the setting for this property when devices go in and out of existence */ 
	SettingSchema settingDevicesChanged = new SettingSchema(ISettingsHandler.TRANSIENT, "DevicesChanged", Boolean.FALSE);

	int getBaseIconIndex();
	int getActiveIconIndex();
	String getToolTip();
	IProperty getActiveProperty();
	
	/** Get settings groups to edit for the provider, e.g. IDsrHandler#GROUP... */
	String[] getGroups();
	String getTitle();
}
