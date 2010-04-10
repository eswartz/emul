/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.IOperation;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * @author ejs
 *
 */
public class BinaryOperationRelation extends BaseRelation implements IRelation {

	private final IBinaryOperation operation;

	public BinaryOperationRelation(IBinaryOperation operation, ITyped head, ITyped[] tails) {
		super(head, tails);
		this.operation = operation;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferDown()
	 */
	@Override
	public boolean inferDown(TypeEngine typeEngine) throws TypeException {
		return false;
		/*
		IBinaryOperation.OpTypes types = new IBinaryOperation.OpTypes();
		ITyped left = tails[0];
		ITyped right = tails[1];

		types.left = typeEngine.getBaseType(left.getType());
		types.right = typeEngine.getBaseType(right.getType());
		types.result = typeEngine.getBaseType(head.getType());
		operation.inferTypes(typeEngine, types);
		
		boolean changed = updateType(left, types.left) | updateType(right, types.right);
		if (changed) {
			updateComplete();
		}
		return changed;
		*/
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferUp()
	 */
	@Override
	public boolean inferUp(TypeEngine typeEngine) throws TypeException {
		IBinaryOperation.OpTypes types = new IBinaryOperation.OpTypes();
		ITyped left = tails[0];
		ITyped right = tails[1];
		                    
		types.left = typeEngine.getBaseType(left.getType());
		types.right = typeEngine.getBaseType(right.getType());
		types.result = typeEngine.getBaseType(head.getType());
		
		if (isCompleteType(head.getType()) && isCompleteType(left.getType()) && isCompleteType(right.getType()))
			operation.castTypes(typeEngine, types);
		else
			operation.inferTypes(typeEngine, types);
		
		boolean changed = false;
		if (canReplaceType(head, types.result)) {
			head.setType(types.result);
			changed = true;
			updateComplete();
			//finalize(typeEngine);
		}
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#finalize(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void finalize(TypeEngine typeEngine) throws TypeException {
		IAstBinExpr binExpr = (IAstBinExpr) head;
		IAstTypedExpr left = binExpr.getLeft();
		IAstTypedExpr right = binExpr.getRight();

		IBinaryOperation.OpTypes types = new IBinaryOperation.OpTypes();
		types.left = left.getType();
		types.right = right.getType();
		types.result = head.getType();
		operation.castTypes(typeEngine, types);
		
		binExpr.setLeft(createCastOn(typeEngine, left, types.left));
		binExpr.setRight(createCastOn(typeEngine, right, types.right));
	}
}
