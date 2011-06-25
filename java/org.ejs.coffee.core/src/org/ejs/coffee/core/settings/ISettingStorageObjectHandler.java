/**
 * 
 */
package org.ejs.coffee.core.settings;

/**
 * @author ejs
 *
 */
public interface ISettingStorageObjectHandler {
	String encodeObject(Object object);
	Object decodeObject(String text);
}
