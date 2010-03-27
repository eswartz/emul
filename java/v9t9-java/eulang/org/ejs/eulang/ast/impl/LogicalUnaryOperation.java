/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IUnaryOperation;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;
import org.ejs.eulang.types.LLType.BasicType;

/**
 * @author ejs
 *
 */
public class LogicalUnaryOperation extends Operation implements IUnaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public LogicalUnaryOperation(String name, boolean isCommutative) {
		super(name, isCommutative);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#getPreferredType(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType getPreferredType(TypeEngine typeEngine, LLType lhsType, LLType opType) {
		if (opType != null && opType.getBasicType() == BasicType.INTEGRAL)
			return opType;
		if (lhsType != null && lhsType.getBasicType() == BasicType.INTEGRAL)
			return lhsType;
		return typeEngine.INT_ANY;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#getResultType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType getResultType(LLType type) throws TypeException {
		if (!(type.getBasicType() == BasicType.INTEGRAL))
			throw new TypeException(getName() + " requires integer operand, got " + type.toString());
		return type;
	}

}
