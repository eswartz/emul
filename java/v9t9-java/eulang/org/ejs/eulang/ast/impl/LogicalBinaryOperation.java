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
public class LogicalBinaryOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public LogicalBinaryOperation(String name, boolean isCommutative) {
		super(name, isCommutative);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#getPreferredLeftType(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public LLType getPreferredLeftType(TypeEngine typeEngine, LLType lhsType, LLType leftType, LLType rightType) {
		if (leftType != null && leftType.getBasicType() == BasicType.INTEGRAL)
			return leftType;
		if (lhsType != null && lhsType.getBasicType() == BasicType.INTEGRAL)
			return lhsType;
		return typeEngine.INT_ANY;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#getPreferredRightType(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public LLType getPreferredRightType(TypeEngine typeEngine, LLType lhsType, LLType leftType, LLType rightType) {
		if (rightType != null && rightType.getBasicType() == BasicType.INTEGRAL)
			return rightType;
		if (lhsType != null && lhsType.getBasicType() == BasicType.INTEGRAL)
			return lhsType;
		return typeEngine.INT_ANY;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#getResultType(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.types.LLType, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType getResultType(TypeEngine typeEngine, LLType leftType,
			LLType rightType) throws TypeException {
		if (leftType.getBasicType() != BasicType.INTEGRAL)
			throw new TypeException(getName() + " requires integer operands, got " + leftType.toString());
		if (rightType.getBasicType() != BasicType.INTEGRAL)
			throw new TypeException(getName() + " requires integer operands, got " + rightType.toString());
		LLType type = typeEngine.getPromotionType(leftType, rightType);
		return type;
	}

	
}
