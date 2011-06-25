/**
 * 
 */
package org.ejs.eulang;

/**
 * @author ejs
 *
 */
public interface ISourceRef {
	String getFile();
	/** number of characters */
	int getLength();
	/** 1... */
	int getLine();
	/** 1... */
	int getEndLine();
	/** 1... */
	int getColumn();
	/** 1... */
	int getEndColumn();
	/**
	 * @param sourceRef
	 * @return
	 */
	boolean contains(ISourceRef sourceRef);
}
