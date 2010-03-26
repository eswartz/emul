/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public class Operation implements IOperation {

	private final String name;
	private final boolean isCommutative;

	/**
	 * @param string
	 * @param isCommutative
	 * @param isArithmetic
	 * @param isLogical
	 */
	public Operation(String name, boolean isCommutative) {
		this.name = name;
		this.isCommutative = isCommutative;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IOperation#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isCommutative() {
		return isCommutative;
	}


}
