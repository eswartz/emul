/**
 * Mar 1, 2011
 */
package v9t9.common.dsr;

import java.util.Collection;
import java.util.Map;

import v9t9.base.properties.SettingProperty;

/**
 * @author ejs
 *
 */
public interface IDsrSettings {

	/**
	 * Get editable settings
	 * @return map of group label to settings
	 */
	Map<String, Collection<SettingProperty>> getEditableSettingGroups();
}
