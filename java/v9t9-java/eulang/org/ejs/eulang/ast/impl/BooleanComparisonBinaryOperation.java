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
public class BooleanComparisonBinaryOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param llvmName 
	 * @param isCommutative
	 */
	public BooleanComparisonBinaryOperation(String name, String llvmName, boolean isCommutative) {
		super(name, llvmName, isCommutative);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		// first, check errors
		if (types.left != null && types.left.getBasicType() != BasicType.BOOL)
			throw new TypeException(getName() + " requires an boolean left operand, got " + types.left.toString());
		if (types.right != null && types.right.getBasicType() != BasicType.BOOL)
			throw new TypeException(getName() + " requires an boolean right operand, got " + types.right.toString());

		// now, prefer booleans
		if (types.left == null) {
			types.left = typeEngine.BOOL;
		}
		if (types.right == null) {
			types.right = typeEngine.BOOL;
		}
		if (types.result == null) {
			types.result = typeEngine.getPromotionType(types.left, types.right);
			if (types.result.getBits() == 0)
				types.result = typeEngine.BOOL;
		}
		
		if (types.left != null && types.right != null) {
			LLType commonType = typeEngine.getPromotionType(types.left, types.right);
			if (commonType == null)
				throw new TypeException("cannot find compatible type for comparing "
						+ types.left.toString() + " and " + types.right.toString());
		} 
		else if (types.left != null) {
			types.right = types.left;
		}
		else if (types.right != null) {
			types.left = types.right;
		}
		if (types.result != null) {
			if (types.result.getBasicType() != BasicType.BOOL)
				throw new TypeException("cannot store comparison result in " + types.result.toString());
		} else {
			types.result = typeEngine.BOOL;
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void castTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		LLType common = typeEngine.getPromotionType(types.left, types.right);
		if (common == null)
			throw new TypeException("cannot find compatible type for comparing "
					+ types.left.toString() + " and " + types.right.toString());
		types.left = types.right = common;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (!types.left.equals(types.right) 
				|| types.result.getBasicType() != BasicType.BOOL) {
			throw new TypeException("inconsistent types in expression");
		}
	}

}
