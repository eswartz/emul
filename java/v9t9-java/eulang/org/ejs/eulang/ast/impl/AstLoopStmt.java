/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstBreakStmt;
import org.ejs.eulang.ast.IAstLoopStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public abstract class AstLoopStmt extends AstTypedExpr implements IAstLoopStmt {

	protected IAstTypedExpr body;
	
	
	protected IScope scope;

	
	/**
	 * @param body2
	 */
	public AstLoopStmt(IScope scope, IAstTypedExpr body) {
		this.scope = scope;
		scope.setOwner(this);
		setBody(body);
	}
	

	protected IAstLoopStmt fixupLoop(IAstLoopStmt copied) {
		remapScope(getScope(), copied.getScope(), copied);
		return fixup(this, copied);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
		AstLoopStmt other = (AstLoopStmt) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}





	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstScope#getScope()
	 */
	@Override
	public IScope getScope() {
		return scope;
	}
	
	@Override
	public void setParent(IAstNode node) {
		super.setParent(node);

		if (node != null) {
			while (node != null) {
				if (node instanceof IAstScope) {
					scope.setParent(((IAstScope) node).getScope());
					break;
				}
				node = node.getParent();
			}
		} else {
			scope.setParent(null);
		}
		
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLoopExpr#getBody()
	 */
	@Override
	public IAstTypedExpr getBody() {
		return body;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLoopExpr#setBody(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setBody(IAstTypedExpr expr) {
		this.body = reparent(this.body, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == body) {
			setBody((IAstTypedExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/** The value of a loop is either the last expression in its body or the value of the
	 * break statement. 
	 * @return
	 */
	public IAstTypedExpr[] getLoopValues() {
		List<IAstTypedExpr> values = new ArrayList<IAstTypedExpr>();
		if (body instanceof IAstStmtListExpr) {
			values.add(((IAstStmtListExpr) body).getValue());
			for (IAstStmt stmt : ((IAstStmtListExpr) body).getStmtList().list()) {
				if (stmt instanceof IAstBreakStmt) {
					values.add(((IAstBreakStmt) stmt).getExpr());
				}
			}
			return (IAstTypedExpr[]) values.toArray(new IAstTypedExpr[values.size()]);
		}
		return new IAstTypedExpr[] { body };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {

		IAstTypedExpr[] loopValues = getLoopValues();
		
		boolean changed = inferTypesFromChildList(typeEngine, loopValues);
		
		if (canInferTypeFrom(this)) {
			// get common promotion type
			LLType common = getType();
			for (int i = 0; i < loopValues.length; i++)
				if (canInferTypeFrom(loopValues[i]))
					common = typeEngine.getPromotionType(common, loopValues[i].getType());
			
			// if we make it out, assign
			if (common != null) {
				for (int i = 0; i < loopValues.length; i++) {
					if (canReplaceType(loopValues[i])) {
						changed |= updateType(loopValues[i], common);
					}
				}
				for (int i = 0; i < loopValues.length; i++) {
					if (loopValues[i] != null) {
						loopValues[i].getParent().replaceChild(loopValues[i], createCastOn(typeEngine, loopValues[i], common));
					}
				}
			}
		}
		
		return changed;
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		LLType thisType = ((IAstTypedNode) this).getType();
		if (thisType == null || !thisType.isComplete())
			return;
		
		LLType kidType = ((IAstTypedNode) body).getType();
		if (kidType != null && kidType.isComplete()) {
			if (!typeEngine.getBaseType(thisType).equals(typeEngine.getBaseType(kidType))) {
				throw new TypeException(body, "expression's type does not match parent");
			}
		}

	}

}
