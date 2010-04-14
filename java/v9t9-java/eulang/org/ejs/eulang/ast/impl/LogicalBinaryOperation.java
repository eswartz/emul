/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class LogicalBinaryOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param llvmName 
	 * @param isCommutative
	 */
	public LogicalBinaryOperation(String name, String llvmName, boolean isCommutative) {
		super(name, llvmName, isCommutative);
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		// first, check errors
		if (types.left != null && types.left.getBasicType() != BasicType.INTEGRAL)
			throw new TypeException(getName() + " requires an integer left operand, got " + types.left.toString());
		if (types.right != null && types.right.getBasicType() != BasicType.INTEGRAL)
			throw new TypeException(getName() + " requires an integer right operand, got " + types.right.toString());

		// now, prefer integers
		if (types.left == null) {
			types.left = typeEngine.INT;
		}
		if (types.right == null) {
			types.right = typeEngine.INT;
		}
		if (types.result == null) {
			types.result = typeEngine.getPromotionType(types.left, types.right);
			if (types.result.getBits() == 0)
				types.result = typeEngine.INT;
		}

	}

	@Override
	public void castTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		LLType newLeft = typeEngine.getPromotionType(types.left, types.result);
		LLType newRight = typeEngine.getPromotionType(types.right, types.result);
		if (newLeft == null || newRight == null)
			throw new TypeException("cannot convert result of '" + getName() + "' on " 
					+ types.left.toString() + " and " + types.right.toString() + " to " + types.result.toString());
		types.left = newLeft;
		types.right = newRight;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (!types.left.equals(types.right) 
				|| !types.result.equals(types.left)
				|| types.result.getBasicType() != BasicType.INTEGRAL) {
			throw new TypeException("inconsistent types in expression");
		}
	}

}
