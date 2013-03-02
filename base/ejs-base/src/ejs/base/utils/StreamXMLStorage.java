/*
  StreamXMLStorage.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

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
	 * 
	 */
	@Override
	protected InputStream getStorageInputStream() throws StorageException {
		if (is == null)
			throw newStorageException("No file to read", null);
		return is;
	}

	/* (non-Javadoc)
	 * 
	 */
	@Override
	protected OutputStream getStorageOutputStream() throws StorageException {
		if (os == null)
			throw newStorageException("No file to read", null);
		return os;
	}

}
