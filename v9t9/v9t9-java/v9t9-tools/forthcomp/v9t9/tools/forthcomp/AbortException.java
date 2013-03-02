/*
  AbortException.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

/**
 * @author ejs
 *
 */
public class AbortException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5463815671870295570L;
	private final int line;
	private final String file;

	/**
	 * @param line
	 * @param string
	 */
	public AbortException(String file, int line, String string) {
		super(string);
		this.file = file;
		this.line = line;
	}
	
	/**
	 * @param string
	 */
	public AbortException(String string) {
		super(string);
		file = null;
		line = 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#toString()
	 */
	@Override
	public String toString() {
		return getClass().getName()+": " + getFile()+":" + getLine()+": " + getMessage();
	}
	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}
}
