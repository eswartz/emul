/**
 * Mar 1, 2011
 */
package v9t9.common.dsr;

import java.util.Collection;
import java.util.Map;

import ejs.base.properties.IProperty;


/**
 * @author ejs
 *
 */
public interface IDeviceSettings {

	/**
	 * Get editable settings
	 * @return map of group label to settings
	 */
	Map<String, Collection<IProperty>> getEditableSettingGroups();
}
