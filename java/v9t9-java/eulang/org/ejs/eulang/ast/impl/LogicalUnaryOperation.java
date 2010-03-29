/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IUnaryOperation;
import org.ejs.eulang.ast.TypeEngine;
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
	public LogicalUnaryOperation(String name) {
		super(name, false);
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		// first, check errors
		if (types.expr != null && types.expr.getBasicType() != BasicType.INTEGRAL)
			throw new TypeException(getName() + " requires an integer left operand, got " + types.expr.toString());

		// now, prefer integers
		if (types.expr == null) {
			types.expr = typeEngine.INT;
		}
		if (types.result == null) {
			types.result = types.expr;
			if (types.result.getBits() == 0)
				types.result = typeEngine.INT;
		}

	}

	@Override
	public void castTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		types.expr = types.result;
	}

}
