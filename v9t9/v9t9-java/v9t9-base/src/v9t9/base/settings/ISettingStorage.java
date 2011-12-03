/**
 * 
 */
package v9t9.base.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for reading and writing settings to files
 * @author ejs
 *
 */
public interface ISettingStorage {
	void setObjectHandler(ISettingStorageObjectHandler handler);
	
	ISettingSection load(InputStream inputStream) throws IOException;
	void save(OutputStream outputStream, ISettingSection section) throws IOException;
}
