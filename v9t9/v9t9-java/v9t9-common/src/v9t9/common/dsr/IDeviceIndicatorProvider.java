package v9t9.common.dsr;

import v9t9.base.properties.SettingProperty;

/**
 * @author ejs
 *
 */
public interface IDeviceIndicatorProvider {

	int getBaseIconIndex();
	int getActiveIconIndex();
	String getToolTip();
	SettingProperty getActiveProperty();
}
