/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstBlockStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStatement;
import org.ejs.eulang.symbols.IScope;

/**
 * @author ejs
 *
 */
public class AstBlockStmt extends AstScope implements IAstBlockStmt {

	private IAstNodeList<IAstStatement> stmtList;

	/**
	 * @param stmtList
	 * @param scope 
	 */
	public AstBlockStmt(IAstNodeList<IAstStatement> stmtList, IScope scope) {
		super(scope);
		this.stmtList = stmtList;
		stmtList.setParent(this);
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
		return true;
	}



	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBlockStmt#stmtList()
	 */
	@Override
	public IAstNodeList<IAstStatement> stmts() {
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
	@Override
	public void replaceChildren(IAstNode[] children) {
		throw new UnsupportedOperationException();
	}

}
