/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * An expression with a single operator.
 * 
 * @author eswartz
 *
 */
public interface IAstUnaryExpr extends IAstTypedExpr {
	IAstUnaryExpr copy(IAstNode copyParent);
	
    public IUnaryOperation getOp();

    public void setOp(IUnaryOperation operator);

    /** Get the target of the expression */
    public IAstTypedExpr getExpr();

    /** Set the target of the expression */
    public void setExpr(IAstTypedExpr expr);

}
