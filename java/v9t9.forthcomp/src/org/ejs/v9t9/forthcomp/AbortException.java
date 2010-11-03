/**
 * 
 */
package org.ejs.v9t9.forthcomp;

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
