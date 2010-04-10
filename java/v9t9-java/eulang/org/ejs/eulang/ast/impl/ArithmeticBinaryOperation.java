/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class ArithmeticBinaryOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public ArithmeticBinaryOperation(String name, String llvmName, boolean isCommutative) {
		super(name, llvmName, isCommutative);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		if (types.left != null && types.right != null) {
			LLType commonType = typeEngine.getPromotionType(types.left, types.right);
			if (commonType == null)
				throw new TypeException("cannot find compatible type for '" + getName() + "' on " 
						+ types.left.toString() + " and " + types.right.toString());
			if (types.result == null || types.result.isGeneric()) {
				types.result = commonType;
			}
		} 
		else if (types.left != null) {
			types.right = types.left;
			types.result = types.left;
		}
		else if (types.right != null) {
			types.left = types.right;
			types.result = types.right;
		}
		else if (types.result != null) {
			types.left = types.result;
			types.right = types.result;
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
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
}
