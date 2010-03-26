/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public class LogicalOperation extends Operation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public LogicalOperation(String name, boolean isCommutative) {
		super(name, isCommutative);
	}

}
