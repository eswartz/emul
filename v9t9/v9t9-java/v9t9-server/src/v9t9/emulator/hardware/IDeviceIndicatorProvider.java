package v9t9.emulator.hardware;

import org.ejs.coffee.core.properties.SettingProperty;

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
