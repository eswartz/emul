/**
 * 
 */
package v9t9.base.settings;

import v9t9.base.properties.IProperty;

/**
 * @author Ed
 *
 */
public interface ISettingProperty extends IProperty {

	boolean isEnabled();

	/**
	 * @return
	 */
	boolean isDefault();

}