/**
 * 
 */
package v9t9.base.properties;

import v9t9.base.settings.ISettingSection;

/**
 * @author ejs
 *
 */
public interface IPersistable {
	/** Save state into the given storage.  Child sections may be created. 
	 * The type of the property should be recreatable from the data in the element
	 * (e.g., a number should be emitted in ASCII so that loadState can re-read it;
	 * or an object should be emitted with a known child element name that
	 * loadState can find.)
	 * */
	void saveState(ISettingSection section);
	/** Load state from the given storage.  The property already exists */
	void loadState(ISettingSection section);
}
