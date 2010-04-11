/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IUnaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.types.TypeException;
import org.ejs.eulang.types.LLType.BasicType;

/**
 * @author ejs
 *
 */
public class ComparisonUnaryOperation extends Operation implements IUnaryOperation {

	/**
	 * @param name
	 * @param isCommutative
	 */
	public ComparisonUnaryOperation(String name) {
		super(name, null, false);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IBinaryOperation#inferTypes(org.ejs.eulang.ast.IBinaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types) throws TypeException {
		if (types.expr != null && types.expr.getBasicType() != BasicType.BOOL)
			throw new TypeException("cannot invert " + types.expr.toString());

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
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (!types.expr.equals(types.result) 
				|| types.result.getBasicType() != BasicType.BOOL) {
			throw new TypeException("inconsistent types in expression");
		}
	}

}
