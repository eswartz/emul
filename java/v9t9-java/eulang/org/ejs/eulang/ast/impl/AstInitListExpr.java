/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstFieldExpr;
import org.ejs.eulang.ast.IAstIndexExpr;
import org.ejs.eulang.ast.IAstInitListExpr;
import org.ejs.eulang.ast.IAstInitNodeExpr;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.BaseLLField;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLDataType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstInitListExpr extends AstTypedExpr implements IAstInitListExpr { 
	private IAstNodeList<IAstInitNodeExpr> initExprs;

	public AstInitListExpr(IAstNodeList<IAstInitNodeExpr> initExprs) {
		setInitExprs(initExprs);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("INITLIST");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstInitListExpr copy(IAstNode parent) {
		return fixup(this,new AstInitListExpr(doCopy(initExprs, parent)));
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((initExprs == null) ? 0 : initExprs.hashCode());
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
		AstInitListExpr other = (AstInitListExpr) obj;
		if (initExprs == null) {
			if (other.initExprs != null)
				return false;
		} else if (!initExprs.equals(other.initExprs))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#getInitExprs()
	 */
	@Override
	public IAstNodeList<IAstInitNodeExpr> getInitExprs() {
		return initExprs;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstInitExpr#setInitExprs(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setInitExprs(IAstNodeList<IAstInitNodeExpr> exprs) {
		this.initExprs = reparent(this.initExprs, exprs);
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { initExprs };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == initExprs)
			setInitExprs((IAstNodeList<IAstInitNodeExpr>) another);
		else
			throw new IllegalArgumentException();
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		// Types are driven top-down.  Force element types to the aggregate type.
		if (canInferTypeFrom(this) && getType().isComplete()) {
			for (int i = 0; i < initExprs.nodeCount(); i++) {
				IAstInitNodeExpr expr = initExprs.list().get(i);
				Pair<Integer, LLType> info = expr.getInitFieldInfo(getType());
				LLType fieldType = info.second;
				assert fieldType != null;
				
				if (!fieldType.equals(expr.getType())) {
					expr.replaceChild(expr.getExpr(), createCastOn(typeEngine, expr.getExpr(), fieldType));
					expr.setType(fieldType);
					changed = true;
				}
			}
		}
		return changed;
	}
}
