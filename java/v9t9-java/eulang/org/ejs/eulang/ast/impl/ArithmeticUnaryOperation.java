/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IUnaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class ArithmeticUnaryOperation extends Operation implements IUnaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public ArithmeticUnaryOperation(String name) {
		super(name, null, false);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#inferTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IUnaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (types.result != null && types.expr == null)
			types.expr = types.result;
		if (types.expr != null && types.result == null)
			types.result = types.expr;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IUnaryOperation.OpTypes)
	 */
	@Override
	public void castTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		types.expr = types.result;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (!types.result.equals(types.expr)) {
			throw new TypeException("inconsistent types in expression");
		}
	}

}
