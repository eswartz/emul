/**
 * 
 */
package org.ejs.coffee.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;

/**
 * @author ejs
 *
 */
public class FileXMLStorage extends XMLStorageBase {

	private final File file;

	/**
	 * 
	 */
	public FileXMLStorage(File file) {
		this.file = file;
	}
	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.XMLStorageBase#getStorageInputStream()
	 */
	@Override
	protected InputStream getStorageInputStream() throws CoreException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw newCoreException(null, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.XMLStorageBase#getStorageOutputStream()
	 */
	@Override
	protected OutputStream getStorageOutputStream() throws CoreException {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw newCoreException(null, e);
		}
	}

}
