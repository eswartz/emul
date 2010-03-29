/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IOperation;
import org.ejs.eulang.ast.IUnaryOperation;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.TypeException;

/**
 * @author eswartz
 *
 */
public class AstUnaryExpr extends AstTypedExpr implements
        IAstUnaryExpr {

    protected IAstTypedExpr expr;
    protected IUnaryOperation op;

    /** Create a unary expression
     */
    public AstUnaryExpr(IUnaryOperation op, IAstTypedExpr operand) {
        setOp(op);
        setExpr(operand);
        dirty = false;
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstNode#copy()
     */
    @Override
    public IAstUnaryExpr copy(IAstNode copyParent) {
    	return fixup(this, new AstUnaryExpr(op, doCopy(expr, copyParent)));
    }
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#toString()
     */
    @Override
	public String toString() {
        return op.getName() + ":" + getTypeString();
    }
    
     /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.impl.AstNode#getChildren()
     */
    public IAstNode[] getChildren() {
        return new IAstNode[] { expr };
    }
    @Override
	public void replaceChildren(IAstNode[] children) {
    	setExpr((IAstTypedExpr) children[0]);
	}
	
     /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstUnaryExpr#getOp()
     */
    @Override
    public IUnaryOperation getOp() {
    	return op;
    }

    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstUnaryExpr#setOp(org.ejs.eulang.ast.IOperation)
     */
    @Override
    public void setOp(IUnaryOperation op) {
    	Check.checkArg(op);
    	this.op = op;
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
		if (op == IOperation.CAST) {
			if (expr instanceof IAstLitExpr) {
				return typeEngine.createLiteralNode(getType(), ((IAstLitExpr) expr).getObject());
			}
		}

        return this;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#equalValue(v9t9.tools.decomp.expr.IAstExpression)
     */
    public boolean equalValue(IAstTypedExpr expr) {
        return expr instanceof IAstUnaryExpr
        && ((IAstUnaryExpr) expr).getType().equals(getType())
        && ((IAstUnaryExpr) expr).getOp() == getOp()
        && ((IAstUnaryExpr) expr).getExpr().equalValue(getExpr())
        ;
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
     */
    @Override
    public boolean inferTypeFromChildren(TypeEngine typeEngine)
    		throws TypeException {
    	IUnaryOperation.OpTypes types = new IUnaryOperation.OpTypes();
    	types.expr = expr.getType();
    	types.result = getType();
    	op.inferTypes(typeEngine, types);
    	return updateType(expr, types.expr) | updateType(this, types.result);
    }
}
