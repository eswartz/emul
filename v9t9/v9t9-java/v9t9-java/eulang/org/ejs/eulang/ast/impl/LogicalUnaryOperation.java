/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.IUnaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

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
		super(name, null, false);
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
	public boolean transformExpr(IAstUnaryExpr expr, TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		types.expr = types.result;
		return expr.setExpr(AstTypedNode.createCastOn(typeEngine, expr.getExpr(), types.expr));
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (!types.expr.equals(types.result) 
				|| types.result.getBasicType() != BasicType.INTEGRAL) {
			throw new TypeException("inconsistent types in expression");
		}
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#evaluate(org.ejs.eulang.types.LLType, org.ejs.eulang.ast.IAstLitExpr, org.ejs.eulang.ast.IAstLitExpr)
	 */
	@Override
	public LLConstOp evaluate(LLType type, IAstLitExpr expr) {
		Number value = null;
		
		if (type.getBasicType() == BasicType.INTEGRAL
				&& expr instanceof IAstIntLitExpr) {
			long l = ((IAstIntLitExpr) expr).getValue();
			
			int bits = type.getBits();
			long limit = bits == 1 ? 1 : bits == 8 ? 0xff : bits == 16 
					? 0xffff : bits == 32 ? 0xffffffff : Long.MAX_VALUE;
			
			if (this == IOperation.INV) {
				value = (~l) & limit;
			}
		}
		if (value != null)
			return new LLConstOp(type, value);
		return null;
	}
}
