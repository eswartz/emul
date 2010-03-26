/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public class ArithmeticOperation extends Operation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public ArithmeticOperation(String name, boolean isCommutative) {
		super(name, isCommutative);
	}

}
