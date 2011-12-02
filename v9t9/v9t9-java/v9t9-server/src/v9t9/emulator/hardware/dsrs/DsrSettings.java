/**
 * Mar 1, 2011
 */
package v9t9.emulator.hardware.dsrs;

import java.util.Collection;
import java.util.Map;

import org.ejs.coffee.core.properties.SettingProperty;

/**
 * @author ejs
 *
 */
public interface DsrSettings {

	/**
	 * Get editable settings
	 * @return map of group label to settings
	 */
	Map<String, Collection<SettingProperty>> getEditableSettingGroups();
}
