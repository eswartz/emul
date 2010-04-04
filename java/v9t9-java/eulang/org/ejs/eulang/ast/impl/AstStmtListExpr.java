/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.ITyped;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstStmtListExpr extends AstTypedExpr implements IAstStmtListExpr  {

	private IAstSymbolExpr result;
	private IAstNodeList<IAstStmt> stmtList;

	/**
	 * @param result
	 */
	public AstStmtListExpr(IAstSymbolExpr result, IAstNodeList<IAstStmt> stmtList)  {
		setResult(result);
		setStmtList(stmtList);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstStmtListExpr copy(IAstNode copyParent) {
		return fixup(this, new AstStmtListExpr(doCopy(result, copyParent), doCopy(stmtList, copyParent)));
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("STMTLIST")+"; return: " + (getResult() != null ? getResult() : "nothing");
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstStmtListExpr#setStmtList(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setStmtList(IAstNodeList<IAstStmt> list) {
		this.stmtList = reparent(this.stmtList, list);
		
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstStmtListExpr#getStmtList()
	 */
	@Override
	public IAstNodeList<IAstStmt> getStmtList() {
		return stmtList;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.IAstExprStatement#setExpr(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	public void setResult(IAstSymbolExpr expr) {
		this.result = reparent(this.result, expr);		
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.IAstExprStatement#getExpr()
	 */
	public IAstSymbolExpr getResult() {
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if(result != null)
			return new IAstNode[] { result, stmtList };
		else
			return new IAstNode[] { stmtList };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChildren(IAstNode[] children) {
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getResult() == existing) {
			setResult((IAstSymbolExpr) another);
		} else if (getStmtList() == existing) {
			setStmtList((IAstNodeList) another);
		} else {
			throw new IllegalArgumentException();
		}
	}
	 
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpr#equalValue(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		IAstStmt last = stmtList.getLast();
		if (last instanceof ITyped)
			return inferTypesFromChildren(new ITyped[] { (ITyped) last });
		return false;
	}

}
