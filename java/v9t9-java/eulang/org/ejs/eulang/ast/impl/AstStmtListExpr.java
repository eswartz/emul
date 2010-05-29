/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.ast.IAstStmtListExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstStmtListExpr extends AstTypedExpr implements IAstStmtListExpr  {

	//private IAstSymbolExpr result;
	private IAstNodeList<IAstStmt> stmtList;

	/**
	 * @param result
	 */
	public AstStmtListExpr(/*IAstSymbolExpr result,*/ IAstNodeList<IAstStmt> stmtList)  {
		//setResult(result);
		setStmtList(stmtList);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstStmtListExpr copy() {
		return fixup(this, new AstStmtListExpr(/*doCopy(result, copyParent),*/ doCopy(stmtList)));
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("STMTLIST") /*+"; return: " + (getResult() != null ? getResult() : "nothing")*/;
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

	
	/*
	public void setResult(IAstSymbolExpr expr) {
		this.result = reparent(this.result, expr);		
	}
	public IAstSymbolExpr getResult() {
		return result;
	}*/

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		//if(result != null)
		//	return new IAstNode[] { result, stmtList };
		//else
			return new IAstNode[] { stmtList };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		/*if (getResult() == existing) {
			setResult((IAstSymbolExpr) another);
		} else*/ 
		if (getStmtList() == existing) {
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
		/*IAstStmt last = null;
		for (int idx = stmtList.list().size(); idx > 0; idx--) {
			last = stmtList.list().get(idx - 1);
			if (!(last instanceof IAstGotoStmt))
				break;
		}
		if (last instanceof ITyped)
			return inferTypesFromChildren(new ITyped[] { (ITyped) last });
		return false;*/
		return inferTypesFromChildren(new ITyped[] { getValue() });
	}

}
