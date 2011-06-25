/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.*;
import org.ejs.eulang.ast.IAstTestBodyLoopExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public abstract class AstTestBodyLoopExpr extends AstBodyLoopExpr implements IAstTestBodyLoopExpr {

	/**
	 * @param doCopy
	 * @param doCopy2
	 */
	public AstTestBodyLoopExpr(IScope scope, IAstTypedExpr expr, IAstTypedExpr body) {
		super(scope, expr, body);
	}

	protected abstract LLType getExpressionType(TypeEngine typeEngine);
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstLoopStmt#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		changed |= super.inferTypeFromChildren(typeEngine);
		LLType expExprType = getExpressionType(typeEngine);
		if (expExprType == typeEngine.BOOL) {
			if (expr.getType() == null || !expr.getType().isComplete() || !expr.getType().equals(expExprType)) {
				replaceChild(expr, promoteValueToNotEqualZero(expr, typeEngine));
				changed = true;
			}
		}
		else {
			changed |= updateType(expr, expExprType);
		}
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		super.validateChildTypes(typeEngine);
		
		LLType kidType = ((IAstTypedNode) expr).getType();
		if (kidType != null && kidType.isComplete()) {
			LLType expExprType = getExpressionType(typeEngine);
			if (!expExprType.equals(kidType)) {
				throw new TypeException(body, "expression type is expected to be " + expExprType);
			}
		}

	}
}
