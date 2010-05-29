/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.IOperation;
import org.ejs.eulang.IUnaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstBoolLitExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
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
		if ((types.expr.getBasicType().getClassMask() & LLType.TYPECLASS_PRIMITIVE) == 0) 
			throw new TypeException("invalid type for '" + getName() + "': " + types.expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.IUnaryOperation#evaluate(org.ejs.eulang.types.LLType, org.ejs.eulang.ast.IAstLitExpr)
	 */
	@Override
	public LLConstOp evaluate(LLType type, IAstLitExpr expr) {
		Number value = null;
		
		long limit = type.getBits() == 1 ? 1 : type.getBits() == 8 ? 0xff : type.getBits() == 16 ? 0xffff : type.getBits() == 32 ? 0xffffffff : Long.MAX_VALUE;
		if (type.getBasicType() == BasicType.INTEGRAL
				&& expr instanceof IAstIntLitExpr) {
			long v = ((IAstIntLitExpr) expr).getValue();
			
			
			if (this == IOperation.NEG) {
				value = -v;
			} else if (this == IOperation.PREINC || this == IOperation.PREDEC) {
				value = v;
			} else if (this == IOperation.POSTINC) {
				value = (v+1) & limit;
			} else if (this == IOperation.POSTDEC) {
				value = (v-1) & limit;
			}
		}
		else if (type.getBasicType() == BasicType.BOOL && expr instanceof IAstBoolLitExpr) {
			if (this == IOperation.NOT) {
				value = ((IAstBoolLitExpr) expr).getValue() ? 0 : 1;
			}
		}
		if (value != null)
			return new LLConstOp(type, value);
		return null;
	}
}
