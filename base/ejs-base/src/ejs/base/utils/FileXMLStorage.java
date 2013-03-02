/*
  FileXMLStorage.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ejs
 *
 */
public class FileXMLStorage extends XMLStorageBase {

	private File file;

	public FileXMLStorage() {
	}
	
	public FileXMLStorage(File file) {
		this.file = file;
	}
	
	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}
	/* (non-Javadoc)
	 * 
	 */
	@Override
	protected InputStream getStorageInputStream() throws StorageException {
		if (file == null)
			throw newStorageException("No file to read", null);
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw newStorageException(null, e);
		}
	}

	/* (non-Javadoc)
	 * 
	 */
	@Override
	protected OutputStream getStorageOutputStream() throws StorageException {
		if (file == null)
			throw newStorageException("No file to read", null);
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			throw newStorageException(null, e);
		}
	}

}
