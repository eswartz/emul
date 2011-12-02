/**
 * 
 */
package org.ejs.coffee.core.settings;

import java.io.File;
import java.io.IOException;

/**
 * Interface for reading and writing settings to files
 * @author ejs
 *
 */
public interface ISettingStorage {
	void setObjectHandler(ISettingStorageObjectHandler handler);
	
	ISettingSection load(File file) throws IOException;
	void save(File file, ISettingSection section) throws IOException;
}
