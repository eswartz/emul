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
import org.ejs.eulang.types.TypeException;

public class AstCastNamedTypeExpr extends AstTypedExpr implements
        IAstCastNamedTypeExpr {

    protected IAstTypedExpr expr;
    protected IAstType typeExpr;

    public AstCastNamedTypeExpr(IAstType typeExpr, IAstTypedExpr expr) {
        setExpr(expr);
        setTypeExpr(typeExpr);
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#copy()
     */
    @Override
    public IAstCastNamedTypeExpr copy(IAstNode copyParent) {
    	return fixup(this, new AstCastNamedTypeExpr(doCopy(typeExpr, copyParent), doCopy(expr, copyParent)));
    }
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#toString()
     */
    @Override
	public String toString() {
        return "AS " + typeExpr.toString();
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

    public IAstTypedExpr simplify(TypeEngine typeEngine) {
		if (expr instanceof IAstLitExpr) {
			IAstLitExpr lit = typeEngine.createLiteralNode(getType(), ((IAstLitExpr) expr).getObject());
			if (lit != null)
				lit.setSourceRef(getSourceRef());
			return lit;
		}
        return this;
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
    		IAstUnaryExpr castExpr = new AstUnaryExpr(IUnaryOperation.CAST, expr);
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
