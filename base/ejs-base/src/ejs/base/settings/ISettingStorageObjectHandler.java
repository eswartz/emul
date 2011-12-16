/**
 * 
 */
package ejs.base.settings;

/**
 * @author ejs
 *
 */
public interface ISettingStorageObjectHandler {
	String encodeObject(Object object);
	Object decodeObject(String text);
}
