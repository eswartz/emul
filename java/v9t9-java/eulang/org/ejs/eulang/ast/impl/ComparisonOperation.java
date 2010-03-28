/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IBinaryOperation;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;
import org.ejs.eulang.types.LLType.BasicType;

/**
 * @author ejs
 *
 */
public class ComparisonOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public ComparisonOperation(String name, boolean isCommutative) {
		super(name, isCommutative);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
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
}
