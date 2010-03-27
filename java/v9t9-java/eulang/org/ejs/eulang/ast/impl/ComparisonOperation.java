/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IBinaryOperation;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

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
	 * @see org.ejs.eulang.ast.IBinaryOperation#getPreferredLeftType(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public LLType getPreferredLeftType(TypeEngine typeEngine, LLType lhsType, LLType leftType, LLType rightType) {
		if (leftType != null) {
			if (rightType != null) {
				if (leftType.getBasicType() == rightType.getBasicType())
					return leftType;
				return typeEngine.getPromotionType(leftType, rightType);
			}
			return leftType;
		} else if (rightType != null) {
			return rightType;
		} else if (lhsType != null) {
			return lhsType;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#getPreferredRightType(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public LLType getPreferredRightType(TypeEngine typeEngine, LLType lhsType, LLType leftType, LLType rightType) {
		if (rightType != null) {
			if (leftType != null) {
				if (rightType.getBasicType() == leftType.getBasicType())
					return rightType;
				return typeEngine.getPromotionType(leftType, rightType);
			}
			return rightType;
		} else if (leftType != null) {
			return leftType;
		} else if (lhsType != null) {
			return lhsType;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#getResultType(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.types.LLType, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType getResultType(TypeEngine typeEngine, LLType leftType,
			LLType rightType) throws TypeException {
		if (!leftType.equals(rightType))
			throw new TypeException(getName() + " requires identical operands, got " + leftType.toString() + " and " + rightType.toString());
		return typeEngine.BOOL;
	}

	
}
