/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IOperation;

/**
 * @author ejs
 *
 */
public class Operation implements IOperation {

	private final String name;
	private final boolean isCommutative;
	private final String llvmName;

	/**
	 * @param llvmName TODO
	 * @param isCommutative
	 * @param string
	 * @param isArithmetic
	 * @param isLogical
	 */
	public Operation(String name, String llvmName, boolean isCommutative) {
		this.name = name;
		this.llvmName = llvmName;
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

	/* (non-Javadoc)
	 * @see org.ejs.eulang.IOperation#getLLVMName()
	 */
	@Override
	public String getLLVMName() {
		return llvmName;
	}
	@Override
	public boolean isCommutative() {
		return isCommutative;
	}


}
