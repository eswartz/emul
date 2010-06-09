/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.IUnaryOperation;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstCastNamedTypeExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.TypeException;

public class AstCastNamedTypeExpr extends AstTypedExpr implements
        IAstCastNamedTypeExpr {

    protected IAstTypedExpr expr;
    protected IAstType typeExpr;
	private boolean isUnsigned;

    public AstCastNamedTypeExpr(IAstType typeExpr, IAstTypedExpr expr, boolean isUnsigned) {
        this.isUnsigned = isUnsigned;
		setExpr(expr);
        setTypeExpr(typeExpr);
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#copy()
     */
    @Override
    public IAstCastNamedTypeExpr copy() {
    	return fixup(this, new AstCastNamedTypeExpr(doCopy(typeExpr), doCopy(expr), isUnsigned));
    }
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#toString()
     */
    @Override
	public String toString() {
        return "AS " + typeExpr.toString();
    }
    
    
    
     @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + (isUnsigned ? 1231 : 1237);
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
		AstCastNamedTypeExpr other = (AstCastNamedTypeExpr) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (isUnsigned != other.isUnsigned)
			return false;
		if (typeExpr == null) {
			if (other.typeExpr != null)
				return false;
		} else if (!typeExpr.equals(other.typeExpr))
			return false;
		return true;
	}

	/* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return new IAstNode[] { expr,  typeExpr };
    }
    /* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getExpr() == existing) {
			setExpr((IAstTypedExpr) another);
		} else if (getTypeExpr() == existing) {
				setTypeExpr((IAstType) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @return the isUnsigned
	 */
	public boolean isUnsigned() {
		return isUnsigned;
	}
	/**
	 * @param isUnsigned the isUnsigned to set
	 */
	public void setUnsigned(boolean isUnsigned) {
		this.isUnsigned = isUnsigned;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCastExpr#getTypeExpr()
	 */
	@Override
	public IAstType getTypeExpr() {
		return typeExpr;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCastExpr#setTypeExpr(org.ejs.eulang.ast.IAstType)
	 */
	@Override
	public void setTypeExpr(IAstType type) {
		this.typeExpr = reparent(this.typeExpr, type);
	}
	
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstUnaryExpression#getOperand()
     */
    public IAstTypedExpr getExpr() {
        return expr;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstUnaryExpression#setOperand(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void setExpr(IAstTypedExpr expr) {
        org.ejs.coffee.core.utils.Check.checkArg(expr);
        this.expr = reparent(this.expr, expr);
        dirty = true;
    }

    public boolean simplify(TypeEngine typeEngine) {
		if (expr instanceof IAstLitExpr) {
			IAstLitExpr lit = typeEngine.createLiteralNode(getType(), ((IAstLitExpr) expr).getObject());
			if (lit != null) {
				lit.setSourceRef(getSourceRef());
				getParent().replaceChild(this, lit);
				return true;
			}
		}
        return super.simplify(typeEngine);
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
     */
    @Override
    public boolean inferTypeFromChildren(TypeEngine typeEngine)
    		throws TypeException {
    	boolean changed = inferTypesFromChildren(new ITyped[] { typeExpr });
    	if (typeExpr.getType() != null && typeExpr.getType().isComplete()) {
    		expr.setParent(null);
    		
    		if (isUnsigned() && typeExpr.getType().getBasicType() != BasicType.INTEGRAL) {
    			throw new TypeException(typeExpr, "'unsigned' modifier makes no sense on non-integral type");
    		}
    		
    		IAstUnaryExpr castExpr = new AstUnaryExpr(
    				isUnsigned ? IUnaryOperation.UCAST : IUnaryOperation.CAST, 
    						expr);
    		castExpr.setType(typeExpr.getType());
    		castExpr.setTypeFixed(true);
    		castExpr.setSourceRef(getSourceRef());
    		getParent().replaceChild(this, castExpr);
    		return true;
    	}
    	return changed;
    }
    
	
	 /* (non-Javadoc)
    * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes()
    */
   @Override
   public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
	   throw new TypeException(typeExpr, "cannot resolve type");
   }
}
