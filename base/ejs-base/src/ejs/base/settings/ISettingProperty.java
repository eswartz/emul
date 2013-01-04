/**
 * 
 */
package ejs.base.settings;

import ejs.base.properties.IProperty;

/**
 * @author Ed
 *
 */
public interface ISettingProperty extends IProperty {

	boolean isEnabled();

	boolean isDefault();

	void resetToDefault();
}