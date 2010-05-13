/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IOperation;

/**
 * @author ejs
 *
 */
public class MovOperation extends Operation implements IOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public MovOperation(String name) {
		super(name, null, false);
	}

}
