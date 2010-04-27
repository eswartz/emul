/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstAssignStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstAssignStmt extends AstTypedExpr implements IAstAssignStmt {

	private IAstNodeList<IAstTypedExpr> symExpr;
	private IAstNodeList<IAstTypedExpr> expr;
	private boolean expand;

	/**
	 * @param expr2 
	 * @param left
	 * @param right
	 */
	public AstAssignStmt(IAstNodeList<IAstTypedExpr> id, IAstNodeList<IAstTypedExpr> expr, boolean expand) {
		setExprs(expr);
		setSymbolExprs(id);
		setExpand(expand);
	}

	public IAstAssignStmt copy(IAstNode copyParent) {
		return fixup(this, new AstAssignStmt(doCopy(symExpr, copyParent), doCopy(expr, copyParent), expand));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((symExpr == null) ? 0 : symExpr.hashCode());
		result = prime * result + (expand ? 0 : 111);
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
		AstAssignStmt other = (AstAssignStmt) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (symExpr == null) {
			if (other.symExpr != null)
				return false;
		} else if (!symExpr.equals(other.symExpr))
			return false;
		if (expand != other.expand)
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("=");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { symExpr, expr };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		if (symExpr.nodeCount() == 1)
			return new IAstNode[] { symExpr.getFirst(), expr.getFirst() };
		else
			return getChildren();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getExprs() == existing) {
			setExprs((IAstNodeList<IAstTypedExpr>) another);
		} else if (getSymbolExprs() == existing) {
			setSymbolExprs((IAstNodeList<IAstTypedExpr>) another);
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

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getExpr()
	 */
	@Override
	public IAstNodeList<IAstTypedExpr> getExprs() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getId()
	 */
	@Override
	public IAstNodeList<IAstTypedExpr> getSymbolExprs() {
		return symExpr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExprs(IAstNodeList<IAstTypedExpr> expr) {
		Check.checkArg(expr);
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setId(v9t9.tools.ast.expr.IAstIdExpression)
	 */
	@Override
	public void setSymbolExprs(IAstNodeList<IAstTypedExpr> id) {
		Check.checkArg(id);
		this.symExpr = reparent(this.symExpr, id);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		boolean changed = false;
		for (int i = symExpr.nodeCount(); i-- > 0; ) {
			IAstTypedExpr theSym = symExpr.list().get(i);
			IAstTypedExpr theExpr = expr.list().get(expr.nodeCount() == 1 ? 0 : i);
			changed |= inferTypesFromChildren(new ITyped[] { theSym, theExpr });
			
			LLType left = theSym.getType();
			LLType right = theExpr.getType();
			
			if (left instanceof LLCodeType && right instanceof LLCodeType) {
				left = typeEngine.getPointerType(left);
				theSym.setType(left);
				right = typeEngine.getPointerType(right);
				theExpr.setType(right);
				changed = true;
			}
			if (left != null && right != null) {
				/*if (getType() != null && !getType().equals(left)) {
					setType(left);
					changed = true;
				}*/
				theExpr.getParent().replaceChild(theExpr, createCastOn(typeEngine, theExpr, left));
			}
		}
		return changed;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAllocStmt#setExpand(boolean)
	 */
	@Override
	public void setExpand(boolean expand) {
		this.expand = expand;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAllocStmt#getExpand()
	 */
	@Override
	public boolean getExpand() {
		return expand;
	}
}
