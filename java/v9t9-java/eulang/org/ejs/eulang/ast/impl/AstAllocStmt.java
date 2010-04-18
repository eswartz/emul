/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstAllocStmt extends AstTypedExpr implements IAstAllocStmt {

	private IAstType typeExpr;

	private IAstNodeList<IAstSymbolExpr> symExpr;
	private IAstNodeList<IAstTypedExpr> expr;

	private boolean expand;
	/**
	 * @param expr2 
	 * @param left
	 * @param right
	 */
	public AstAllocStmt(IAstNodeList<IAstSymbolExpr> id, IAstType type, IAstNodeList<IAstTypedExpr> expr, boolean expand) {
		setSymbolExpr(id);
		setExpr(expr);
		setTypeExpr(type);
		setExpand(expand);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstAllocStmt copy(IAstNode copyParent) {
		return fixup(this, new AstAllocStmt(doCopy(symExpr, copyParent), 
				doCopy(typeExpr, copyParent), doCopy(expr, copyParent), expand));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((symExpr == null) ? 0 : symExpr.hashCode());
		result = prime * result + ((typeExpr == null) ? 0 : typeExpr.hashCode());
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
		AstAllocStmt other = (AstAllocStmt) obj;
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
		if (typeExpr == null) {
			if (other.typeExpr != null)
				return false;
		} else if (!typeExpr.equals(other.typeExpr))
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
		return typedString("ALLOC") + (typeExpr != null ? " <= " + typeExpr.toString() : "");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		if (getExpr() != null) {
			if (getSymbolExpr().nodeCount() == 1)
				return new IAstNode[] { getSymbolExpr().getFirst(), getExpr().getFirst() };
			else
				return new IAstNode[] { getSymbolExpr(), getExpr() };
		}
		else {
			if (getSymbolExpr().nodeCount() == 1)
				return new IAstNode[] { getSymbolExpr().getFirst() };
			else
				return new IAstNode[] { getSymbolExpr() };
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getExpr()
	 */
	@Override
	public IAstNodeList<IAstTypedExpr> getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getId()
	 */
	@Override
	public IAstNodeList<IAstSymbolExpr> getSymbolExpr() {
		return symExpr;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExpr(IAstNodeList<IAstTypedExpr> expr) {
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setId(v9t9.tools.ast.expr.IAstIdExpression)
	 */
	@Override
	public void setSymbolExpr(IAstNodeList<IAstSymbolExpr> id) {
		Check.checkArg(id);
		this.symExpr = reparent(this.symExpr, id);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (typeExpr != null && getExpr() != null)
			return new IAstNode[] { getSymbolExpr(), typeExpr, getExpr() };
		else if (typeExpr != null)
			return new IAstNode[] { getSymbolExpr(), typeExpr };
		else if (getExpr() != null)
			return new IAstNode[] { getSymbolExpr(), getExpr() };
		else
			return new IAstNode[] { getSymbolExpr() };
	}

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
     */
    @SuppressWarnings("unchecked")
	@Override
    public void replaceChild(IAstNode existing, IAstNode another) {
		if (getTypeExpr() == existing) {
			setTypeExpr((IAstType) another);
		} else if (getSymbolExpr() == existing) {
			setSymbolExpr((IAstNodeList<IAstSymbolExpr>) another);
		} else if (getExpr() == existing) {
			setExpr((IAstNodeList<IAstTypedExpr>) another);
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
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getTypeExpr()
	 */
	@Override
	public IAstType getTypeExpr() {
		return typeExpr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setTypeExpr(org.ejs.eulang.ast.IAstType)
	 */
	@Override
	public void setTypeExpr(IAstType type) {
		this.typeExpr = reparent(this.typeExpr, type);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		boolean changed = false;
		
		// infer each type individually
		for (int i = 0; i < symExpr.nodeCount(); i++) {
			IAstSymbolExpr theSymbol = getSymbolExpr().list().get(i);
			IAstTypedExpr theExpr = getExpr() != null ? (getExpr().list().get(getExpr().nodeCount() == 1 ? 0 : i)) : null;
			if (!inferTypesFromChildren(new ITyped[] { typeExpr, getSymbolExpr().list().get(i), theExpr })) {
				if (getExpr() != null && theExpr.getType() != null) {
					if (getSymbolExpr() != null && theExpr.getType().isMoreComplete(theSymbol.getType()))
						changed |= updateType(theSymbol, theExpr.getType());
					if (typeExpr != null && theExpr.getType().isMoreComplete(typeExpr.getType()))
						changed |= updateType(typeExpr, theExpr.getType());
					if (i == 0 && theExpr.getType().isMoreComplete(getType()))
						changed |= updateType(this, theExpr.getType());
				}
				else if (getSymbolExpr() != null && theSymbol.getType() != null) {
					if (typeExpr != null)
						changed |= updateType(typeExpr, theSymbol.getType());
					if (i == 0)
						changed |= updateType(this, theSymbol.getType());
				}
				
			} else {
				changed = true;
			}
			
			LLType left = theSymbol.getType();
			LLType right = theExpr != null ? theExpr.getType() : null;
			if (left != null && right != null) {
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
