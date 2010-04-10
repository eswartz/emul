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
public class AllocRelation extends BaseRelation {

	public AllocRelation(ITyped head, ITyped tails) {
		super(head, tails);
	}

	// type, symbol, expr 
	public AllocRelation(ITyped head, ITyped[] tails) {
		super(head, tails);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.IRelation#inferDown()
	 */
	@Override
	public boolean inferDown(TypeEngine typeEngine) throws TypeException {
		boolean changed = false;
		
		// first, impose subrelations
		if (tails[0] != null && tails[0].getType() != null) {
			changed |= updateType(tails[1], tails[0].getType());
		}
		
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
		LLType theType = tails[0] != null ? tails[0].getType() : tails[1].getType();
		if (theType == null) {
			theType = tails[1] != null && tails[1].getType() != null ? tails[1].getType()
					: tails[2] != null ? tails[2].getType() : null;
		} else {
			if (tails[2] != null) {
				LLType common = typeEngine.getPromotionType(theType, tails[2].getType());
				if (common == null) {
					throw new TypeException(tails[2], "inconsistent types: cannot be promoted to the symbol type");
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
		if (tails[2] != null) {
			IAstTypedExpr node = (IAstTypedExpr) tails[2];
			IAstNode origParent = node.getParent();
			IAstTypedNode cast = createCastOn(typeEngine, node, typeEngine.getBaseType(head.getType()));
			origParent.replaceChild(node, cast);
		}
	}
}
