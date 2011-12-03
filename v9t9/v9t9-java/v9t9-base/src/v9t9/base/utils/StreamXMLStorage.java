/**
 * 
 */
package v9t9.base.utils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ejs
 *
 */
public class StreamXMLStorage extends XMLStorageBase {

	private InputStream is;
	private OutputStream os;

	public StreamXMLStorage() {
	}

	/**
	 * @param is the is to set
	 */
	public void setInputStream(InputStream is) {
		this.is = is;
	}
	/**
	 * @param os the os to set
	 */
	public void setOutputStream(OutputStream os) {
		this.os = os;
	}
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.XMLStorageBase#getStorageInputStream()
	 */
	@Override
	protected InputStream getStorageInputStream() throws StorageException {
		if (is == null)
			throw newStorageException("No file to read", null);
		return is;
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.XMLStorageBase#getStorageOutputStream()
	 */
	@Override
	protected OutputStream getStorageOutputStream() throws StorageException {
		if (os == null)
			throw newStorageException("No file to read", null);
		return os;
	}

}
