/**
 * 
 */
package v9t9.base.settings;

/**
 * @author ejs
 *
 */
public interface ISettingStorageObjectHandler {
	String encodeObject(Object object);
	Object decodeObject(String text);
}
