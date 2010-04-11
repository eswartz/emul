/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstCondList;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstCondList extends AstTypedExpr implements IAstCondList {

	private IAstNodeList<IAstCondExpr> condList;

	/**
	 * @param expr2 
	 * @param left
	 * @param right
	 */
	public AstCondList(IAstNodeList<IAstCondExpr> condList) {
		setCondExprs(condList);
	}

	public IAstCondList copy(IAstNode copyParent) {
		return fixup(this, new AstCondList(doCopy(condList, copyParent)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((condList == null) ? 0 : condList.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstCondList other = (AstCondList) obj;
		if (condList == null) {
			if (other.condList != null)
				return false;
		} else if (!condList.equals(other.condList))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("CONDLIST");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { condList };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getCondExprs() == existing) {
			setCondExprs((IAstNodeList<IAstCondExpr>) another);
		} else {
			throw new IllegalArgumentException();
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
	}

	/**
	 * @return the condList
	 */
	public IAstNodeList<IAstCondExpr> getCondExprs() {
		return condList;
	}
	
	/**
	 * @param condList the condList to set
	 */
	public void setCondExprs(IAstNodeList<IAstCondExpr> condList) {
		Check.checkArg(condList);
		this.condList = reparent(this.condList, condList);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		IAstTypedExpr[] exprs = new IAstTypedExpr[condList.list().size() ];
		for (int i = 0; i < exprs.length; i++)
			exprs[i] = condList.list().get(i).getExpr();
		boolean changed = inferTypesFromChildList(typeEngine, exprs);
		
		if (canInferTypeFrom(this)) {
			// get common promotion type
			LLType common = getType();
			for (int i = 0; i < exprs.length; i++)
				if (canInferTypeFrom(exprs[i]))
					common = typeEngine.getPromotionType(common, exprs[i].getType());
			
			// if we make it out, assign
			if (common != null) {
				for (int i = 0; i < exprs.length; i++) {
					if (canReplaceType(exprs[i])) {
						updateType(exprs[i], common);
						changed = true;
					}
				}
				for (int i = 0; i < exprs.length; i++) {
					condList.list().get(i).setExpr(createCastOn(typeEngine, exprs[i], common));
					condList.list().get(i).setType(common);
				}
			}
		}
		
		return changed;
	}
	
}
