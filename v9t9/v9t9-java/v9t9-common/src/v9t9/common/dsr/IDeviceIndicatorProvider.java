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
