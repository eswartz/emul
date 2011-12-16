package v9t9.common.dsr;

import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public interface IDeviceIndicatorProvider {

	int getBaseIconIndex();
	int getActiveIconIndex();
	String getToolTip();
	IProperty getActiveProperty();
}
