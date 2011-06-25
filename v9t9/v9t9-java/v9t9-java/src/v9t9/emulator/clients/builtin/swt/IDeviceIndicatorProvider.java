/**
 * Mar 11, 2011
 */
package v9t9.emulator.clients.builtin.swt;

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
