/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstAllocStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.ITyped;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstAllocStmt extends AstTypedExpr implements IAstAllocStmt {

	private IAstType typeExpr;

	private IAstSymbolExpr symExpr;
	private IAstTypedExpr expr;
	/**
	 * @param expr2 
	 * @param left
	 * @param right
	 */
	public AstAllocStmt(IAstSymbolExpr id, IAstType type, IAstTypedExpr expr) {
		setSymbolExpr(id);
		setExpr(expr);
		setTypeExpr(type);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstAllocStmt copy(IAstNode copyParent) {
		return fixup(this, new AstAllocStmt(doCopy(symExpr, copyParent), doCopy(typeExpr, copyParent), doCopy(expr, copyParent)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((symExpr == null) ? 0 : symExpr.hashCode());
		result = prime * result
				+ ((typeExpr == null) ? 0 : typeExpr.hashCode());
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
		if (getExpr() != null)
			return new IAstNode[] { getSymbolExpr(), getExpr() };
		else
			return new IAstNode[] { getSymbolExpr() };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getId()
	 */
	@Override
	public IAstSymbolExpr getSymbolExpr() {
		return symExpr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefineStmt#getSymbol()
	 */
	@Override
	public ISymbol getSymbol() {
		return symExpr.getSymbol();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setId(v9t9.tools.ast.expr.IAstIdExpression)
	 */
	@Override
	public void setSymbolExpr(IAstSymbolExpr id) {
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
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChildren(IAstNode[] children) {
		if (typeExpr != null && getExpr() != null) {
			setSymbolExpr((IAstSymbolExpr) children[0]);
			if (children[1] != typeExpr)
				throw new UnsupportedOperationException();
			setExpr((IAstTypedExpr) children[2]);
		}
		else if (typeExpr != null) {
			setSymbolExpr((IAstSymbolExpr) children[0]);
			if (children[1] != typeExpr)
				throw new UnsupportedOperationException();
		}
		else if (getExpr() != null) {
			setSymbolExpr((IAstSymbolExpr) children[0]);
			setExpr((IAstTypedExpr) children[1]);
		} else {
			setSymbolExpr((IAstSymbolExpr) children[0]);
		}		
	}
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
     */
    @Override
    public void replaceChild(IAstNode existing, IAstNode another) {
		if (getTypeExpr() == existing) {
			setTypeExpr((IAstType) another);
		} else if (getSymbolExpr() == existing) {
			setSymbolExpr((IAstSymbolExpr) another);
		} else if (getExpr() == existing) {
			setExpr((IAstTypedExpr) another);
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
		if (!inferTypesFromChildren(new ITyped[] { typeExpr, getSymbolExpr(), getExpr() }))
			return false;
		
		LLType left = symExpr.getType();
		LLType right = expr != null ? expr.getType() : null;
		if (left != null && right != null) {
			setExpr(createCastOn(typeEngine, expr, left));
		}
		return true;
	}
	
}
