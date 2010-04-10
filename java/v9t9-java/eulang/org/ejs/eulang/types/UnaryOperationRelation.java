/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.IUnaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;

/**
 * @author ejs
 *
 */
public class UnaryOperationRelation extends BaseRelation implements IRelation {

	private final IUnaryOperation operation;

	public UnaryOperationRelation(IUnaryOperation operation, ITyped head, ITyped tail) {
		super(head, tail);
		this.operation = operation;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferDown()
	 */
	@Override
	public boolean inferDown(TypeEngine typeEngine) throws TypeException {
		IUnaryOperation.OpTypes types = new IUnaryOperation.OpTypes();
		ITyped expr = tails[0];
		                    
		types.expr = typeEngine.getBaseType(expr.getType());
		types.result = typeEngine.getBaseType(head.getType());
		operation.inferTypes(typeEngine, types);
		
		boolean changed = updateType(expr, types.expr);
		if (changed)
			updateComplete();
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferUp()
	 */
	@Override
	public boolean inferUp(TypeEngine typeEngine) throws TypeException {
		IUnaryOperation.OpTypes types = new IUnaryOperation.OpTypes();
		ITyped expr = tails[0];
		                    
		types.expr = typeEngine.getBaseType(expr.getType());
		types.result = typeEngine.getBaseType(head.getType());
		
		operation.inferTypes(typeEngine, types);
		
		if (canReplaceType(head, types.result)) {
			head.setType(types.result);
			updateComplete();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#finalize(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void finalize(TypeEngine typeEngine) throws TypeException {
		IUnaryOperation.OpTypes types = new IUnaryOperation.OpTypes();
		ITyped expr = tails[0];
		types.expr = expr.getType();
		types.result = head.getType();
		operation.castTypes(typeEngine, types);
		((IAstUnaryExpr) head).setExpr(createCastOn(typeEngine, (IAstTypedExpr) expr, types.expr));
	}
}
