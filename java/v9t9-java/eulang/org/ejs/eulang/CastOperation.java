/**
 * 
 */
package org.ejs.eulang;

import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.impl.Operation;
import org.ejs.eulang.llvm.ops.LLConstOp;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class CastOperation extends Operation implements IUnaryOperation {

	private final boolean isUnsigned;

	/**
	 * @param name
	 * @param isCommutative
	 */
	public CastOperation(String name, boolean isUnsigned) {
		super(name, null, false);
		this.isUnsigned = isUnsigned;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#inferTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IUnaryOperation.OpTypes)
	 */
	@Override
	public void inferTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		if (types.result == null)
			throw new TypeException("cannot determine cast type");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IUnaryOperation#castTypes(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.ast.IUnaryOperation.OpTypes)
	 */
	@Override
	public boolean transformExpr(IAstUnaryExpr expr, TypeEngine typeEngine, OpTypes types)
			throws TypeException {
		/*
		// ignore when we're already a cast
		if (getCastedChild(this) != null && !child.isTypeFixed()) {
			if (canReplaceType(this))
				setType(newType);
			return child;
		}
		*/
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IBinaryOperation#validateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.IBinaryOperation.OpTypes)
	 */
	@Override
	public void validateTypes(TypeEngine typeEngine, OpTypes types)
			throws TypeException {

		// see if types are allowed
		if (types.expr.getBasicType() != types.result.getBasicType()) {
			if ((types.expr.getBasicType() == BasicType.POINTER && types.result.getBasicType() == BasicType.ARRAY)
					|| (types.result.getBasicType() == BasicType.ARRAY && types.expr.getBasicType() == BasicType.POINTER)){
				// fine
				return;
			}
			else if (types.expr.getBasicType().isCompatibleWith(types.result.getBasicType())) {
				// fine
				return;
			}
			else if (types.result.getBasicType() == BasicType.VOID || types.expr.getBasicType() == BasicType.VOID) {
				// fine, throwing away or making null
				return;
			}
			else if (types.expr.getBasicType() == BasicType.INTEGRAL && types.result.getBasicType() == BasicType.POINTER) {
				// fine
				return;
			}
			else if (types.expr.getBasicType() == BasicType.CODE && types.result.getBasicType() == BasicType.POINTER) {
				// fine
				return;
			}
			else
				throw new TypeException("cannot cast from " + types.expr +  " to " + types.result);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.IUnaryOperation#evaluate(org.ejs.eulang.ast.IAstLitExpr)
	 */
	@Override
	public LLConstOp evaluate(LLType type, IAstLitExpr simExpr) {
		Number value = null;
		if (type.getBasicType() == BasicType.INTEGRAL) {
			if (simExpr.getObject() instanceof Number) {
				long l = ((Number)simExpr.getObject()).longValue();
				if (isUnsigned) {
					if (simExpr.getType().getBits() <= 8)
						l &= 0xff;
					else if (simExpr.getType().getBits() == 16)
						l &= 0xffff;
					else
						assert false;
				}
				value = l;
			} else if (simExpr.getObject() instanceof Boolean) {
				value = ((Boolean)simExpr.getObject()).booleanValue() ? 1 : 0;
			} else {
				assert false;
			}
		}
		else if (type.getBasicType() == BasicType.BOOL) {
			if (simExpr.getObject() instanceof Number)
				value = ((Number)simExpr.getObject()).doubleValue() != 0 ? 1 : 0;
			else if (simExpr.getObject() instanceof Boolean)
				value = ((Boolean)simExpr.getObject()).booleanValue() ? 1 : 0;
			else
				assert false;
			
		}
		else if (type.getBasicType() == BasicType.FLOATING) {
			if (simExpr.getObject() instanceof Number)
				value = ((Number)simExpr.getObject()).doubleValue();
			else if (simExpr.getObject() instanceof Boolean)
				value = ((Boolean)simExpr.getObject()).booleanValue() ? 1. : 0.;
			else
				assert false;
		}
		if (value != null) {
			return new LLConstOp(type, value);
		}
		return null;
	}

}
