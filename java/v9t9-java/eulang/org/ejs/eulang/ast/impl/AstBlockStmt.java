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
public class AstBlockStmt extends AstStmtScope implements IAstBlockStmt {

	/**
	 * @param stmtList
	 * @param scope 
	 */
	public AstBlockStmt(IAstNodeList<IAstStmt> stmtList, IScope scope) {
		super(stmtList, scope);
	}

	public IAstBlockStmt copy(IAstNode copyParent) {
		return (IAstBlockStmt) fixupStmtScope(new AstBlockStmt(
				doCopy(stmtList, copyParent), getScope().newInstance(getCopyScope(copyParent))));
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("BLOCK");
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
