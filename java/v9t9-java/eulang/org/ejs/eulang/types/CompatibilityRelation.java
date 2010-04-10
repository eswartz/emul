/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.IBinaryOperation;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstBinExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;

/**
 * @author ejs
 *
 */
public class CompatibilityRelation extends BaseRelation {

	public CompatibilityRelation(ITyped head, ITyped tails) {
		super(head, tails);
	}

	public CompatibilityRelation(ITyped head, ITyped[] tails) {
		super(head, tails);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferDown()
	 */
	@Override
	public boolean inferDown(TypeEngine typeEngine) throws TypeException {
		if (head.getType() == null)
			return false;
		
		boolean changed = false;
		for (ITyped tail : tails) {
			if (tail == null)
				continue;

			if (canReplaceType(tail, head.getType())) {
				tail.setType(head.getType());
				changed = true;
			}
			else if (head.getType().isComplete()) {
				LLType common = typeEngine.getPromotionType(head.getType(), tail.getType());
				if (common == null) {
					throw new TypeException(tail, "types are not compatible");
				}				
			}
		}
		
		if (changed)
			updateComplete();

		return changed;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferUp()
	 */
	@Override
	public boolean inferUp(TypeEngine typeEngine) throws TypeException {
		boolean changed = false;
		LLType theType = null;
		for (ITyped tail : tails) {
			if (theType == null) {
				theType = tail.getType();
			} else {
				LLType common = typeEngine.getPromotionType(theType, tail.getType());
				if (common == null) {
					throw new TypeException(tail, "inconsistent types: cannot be promoted to the same type");
				}
			}
		}
		
		if (canReplaceType(head, theType)) {
			head.setType(theType);
			changed = true;
		}
		
		if (changed)
			updateComplete();
		
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseRelation#finalize(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void finalize(TypeEngine typeEngine) throws TypeException {
		for (ITyped typed : tails) {
			if (typed instanceof IAstTypedExpr) {
				IAstTypedExpr node = (IAstTypedExpr) typed;
				IAstNode origParent = node.getParent();
				IAstTypedNode cast = createCastOn(typeEngine, node, typeEngine.getBaseType(head.getType()));
				origParent.replaceChild(node, cast);
			}
		}
	}
}
