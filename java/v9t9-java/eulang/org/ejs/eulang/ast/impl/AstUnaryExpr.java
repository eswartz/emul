/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstUnaryExpr;
import org.ejs.eulang.ast.IUnaryOperation;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author eswartz
 *
 */
public class AstUnaryExpr extends AstTypedExpr implements
        IAstUnaryExpr {

    protected IAstTypedExpr operand;
    protected IUnaryOperation op;

    /** Create a unary expression
     */
    public AstUnaryExpr(IUnaryOperation op, IAstTypedExpr operand) {
        setOp(op);
        setOperand(operand);
        dirty = false;
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
        return new IAstNode[] { operand };
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
    public IAstTypedExpr getOperand() {
        return operand;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstUnaryExpression#setOperand(v9t9.tools.decomp.expr.IAstExpression)
     */
    public void setOperand(IAstTypedExpr expr) {
        org.ejs.coffee.core.utils.Check.checkArg(expr);
        this.operand = reparent(this.operand, expr);
        dirty = true;
    }

    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#simplify()
     */
    public IAstExpr simplify() {
        return this;
    }
    
    /* (non-Javadoc)
     * @see v9t9.tools.decomp.expr.IAstExpression#equalValue(v9t9.tools.decomp.expr.IAstExpression)
     */
    public boolean equalValue(IAstExpr expr) {
        return expr instanceof IAstUnaryExpr
        && ((IAstUnaryExpr) expr).getType().equals(getType())
        && ((IAstUnaryExpr) expr).getOp() == getOp()
        && ((IAstUnaryExpr) expr).getOperand().equalValue(getOperand())
        ;
    }
    
    /* (non-Javadoc)
     * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
     */
    @Override
    public boolean inferTypeFromChildren(TypeEngine typeEngine)
    		throws TypeException {
    	LLType opType = null;
    	if (canInferTypeFrom(operand))
    		opType = operand.getType();
    	opType = op.getPreferredType(typeEngine, null, opType);
    	return updateType(operand, opType) | updateType(this, op.getResultType(opType));
    }
}
