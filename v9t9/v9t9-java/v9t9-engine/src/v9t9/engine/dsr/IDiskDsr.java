/**
 * 
 */
package v9t9.engine.dsr;

import v9t9.base.properties.SettingProperty;
import v9t9.common.files.Catalog;

/**
 * @author ejs
 *
 */
public interface IDiskDsr {
	Catalog getCatalog(SettingProperty diskSetting);
}
