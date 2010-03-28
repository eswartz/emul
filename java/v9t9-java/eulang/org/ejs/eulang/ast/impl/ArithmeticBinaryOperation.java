/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IBinaryOperation;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.ast.IBinaryOperation.OpTypes;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;
import org.ejs.eulang.types.LLType.BasicType;

/**
 * @author ejs
 *
 */
public class ArithmeticBinaryOperation extends Operation implements IBinaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public ArithmeticBinaryOperation(String name, boolean isCommutative) {
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
				throw new TypeException("cannot find compatible type for '" + getName() + "' on " 
						+ types.left.toString() + " and " + types.right.toString());
			if (types.result == null) {
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

	private LLType getPreferredType(TypeEngine typeEngine, LLType lhsType,
			LLType a, LLType b) {
		if (lhsType != null) {
			return lhsType;
		}
		if (a != null) {
			if (b != null) {
				if (a.getBasicType() == b.getBasicType())
					return a;
				return typeEngine.getPromotionType(a, b);
			}
			return a;
		} else if (b != null) {
			return b;
		} 
		return null;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#getPreferredLeftType(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public LLType getPreferredLeftType(TypeEngine typeEngine, LLType lhsType, LLType leftType, LLType rightType) {
		return getPreferredType(typeEngine, lhsType, leftType, rightType);
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#getPreferredRightType(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public LLType getPreferredRightType(TypeEngine typeEngine, LLType lhsType, LLType leftType, LLType rightType) {
		return getPreferredType(typeEngine, lhsType, rightType, leftType);
	}


	private LLType getPromotedType(TypeEngine typeEngine, LLType lhsType, LLType type) {
		return typeEngine.getPromotionType(lhsType, type);
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#getResultType(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.types.LLType, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType getResultType(TypeEngine typeEngine, LLType leftType,
			LLType rightType) throws TypeException {
		if (leftType != null && !leftType.equals(rightType))
			throw new TypeException(getName() + " requires identical operands, got " + leftType.toString() + " and " + rightType.toString());
		return leftType != null ? leftType : rightType;
	}

}
