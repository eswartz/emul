/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstAllocTupleStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTupleNode;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.LLTupleType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstAllocTupleStmt extends AstTypedExpr implements IAstAllocTupleStmt {

	private IAstType typeExpr;

	private IAstTupleNode syms;
	private IAstTypedExpr expr;
	
	public AstAllocTupleStmt(IAstTupleNode ids, IAstType type, IAstTypedExpr expr) {
		setSymbols(ids);
		setExpr(expr);
		setTypeExpr(type);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstAllocTupleStmt copy() {
		return fixup(this, new AstAllocTupleStmt(doCopy(syms), doCopy(typeExpr), doCopy(expr)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((syms == null) ? 0 : syms.hashCode());
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
		AstAllocTupleStmt other = (AstAllocTupleStmt) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (syms == null) {
			if (other.syms != null)
				return false;
		} else if (!syms.equals(other.syms))
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
		return typedString("()ALLOC") + (typeExpr != null ? " <= " + typeExpr.toString() : "");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		if (getExpr() != null)
			return new IAstNode[] { getSymbols(), getExpr() };
		else
			return new IAstNode[] { getSymbols() };
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
	public IAstTupleNode getSymbols() {
		return syms;
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
	public void setSymbols(IAstTupleNode id) {
		Check.checkArg(id);
		this.syms = reparent(this.syms, id);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (typeExpr != null && getExpr() != null)
			return new IAstNode[] { getSymbols(), typeExpr, getExpr() };
		else if (typeExpr != null)
			return new IAstNode[] { getSymbols(), typeExpr };
		else if (getExpr() != null)
			return new IAstNode[] { getSymbols(), getExpr() };
		else
			return new IAstNode[] { getSymbols() };
	}

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
     */
    @Override
    public void replaceChild(IAstNode existing, IAstNode another) {
		if (getTypeExpr() == existing) {
			setTypeExpr((IAstType) another);
		} else if (getSymbols() == existing) {
			setSymbols((IAstTupleNode) another);
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
		boolean changed = false;
		if (!inferTypesFromChildren(new ITyped[] { typeExpr, getExpr() })) {
			if (getExpr() != null && getExpr().getType() != null) {
				if (typeExpr != null && getExpr().getType().isMoreComplete(typeExpr.getType()))
					changed = updateType(typeExpr, getExpr().getType());
				if (getExpr().getType().isMoreComplete(getType()))
					changed = updateType(this, getExpr().getType());
			}
			if (!changed)
				return false;
		}
		
		LLType right = expr != null ? expr.getType() : null;
		if (right != null) {
			if (!(right instanceof LLTupleType)) {
				throw new TypeException("unpacking from non-tuple value");
			}
			
			if (((LLTupleType) right).getTypes().length != syms.elements().nodeCount())
				return false; // detect later
				
			for (int idx = 0; idx < syms.elements().nodeCount(); idx++) {
				IAstTypedExpr sym = syms.elements().list().get(idx);
				updateType(sym, ((LLTupleType) right).getTypes()[idx]);
			}
			updateType(this, right);
			
		}
		return true;
	}
	
}
