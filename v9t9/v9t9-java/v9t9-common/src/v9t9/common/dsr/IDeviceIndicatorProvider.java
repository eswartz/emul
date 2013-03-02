/*
  IDeviceIndicatorProvider.java

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
}
