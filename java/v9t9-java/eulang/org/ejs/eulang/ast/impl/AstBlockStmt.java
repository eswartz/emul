/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstBlockStmt;
import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstBlockStmt extends AstTypedExpr implements IAstBlockStmt {

	private IAstNodeList<IAstStmt> stmtList;

	protected IScope scope;
	/**
	 * @param stmtList
	 * @param scope 
	 */
	public AstBlockStmt(IAstNodeList<IAstStmt> stmtList, IScope scope) {
		this.scope = scope;
		scope.setOwner(this);

		this.stmtList = stmtList;
		stmtList.setParent(this);
	}

	public IAstBlockStmt copy(IAstNode copyParent) {
		IAstBlockStmt copied = new AstBlockStmt(doCopy(stmtList, copyParent), getScope().newInstance(getCopyScope(copyParent)));
		remapScope(getScope(), copied.getScope(), copied);
		return fixup(this, copied);
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "BLOCK";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 444;
		result = prime * result
				+ ((stmtList == null) ? 0 : stmtList.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstBlockStmt other = (AstBlockStmt) obj;
		if (stmtList == null) {
			if (other.stmtList != null)
				return false;
		} else if (!stmtList.equals(other.stmtList))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
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
	 * @see org.ejs.eulang.ast.IAstScope#getScope()
	 */
	@Override
	public IScope getScope() {
		return scope;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBlockStmt#stmtList()
	 */
	@Override
	public IAstNodeList<IAstStmt> stmts() {
		return stmtList;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { stmtList };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (stmtList == existing) {
			stmtList = (IAstNodeList<IAstStmt>) ((IAstType) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public IAstTypedExpr getValue() {
		IAstStmt last = null;
		for (int idx = stmtList.list().size(); idx > 0; idx--) {
			last = stmtList.list().get(idx - 1);
			if (!(last instanceof IAstGotoStmt)) {
				if (last instanceof IAstTypedExpr)
					return (IAstTypedExpr) last;
				else
					return null;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		return inferTypesFromChildren(new ITyped[] { getValue() });
	}

	
}
