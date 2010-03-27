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
public interface IAstUnaryExpr extends IAstExpr, IAstTypedExpr {

    public IUnaryOperation getOp();

    public void setOp(IUnaryOperation operator);

    /** Get the target of the expression */
    public IAstTypedExpr getOperand();

    /** Set the target of the expression */
    public void setOperand(IAstTypedExpr expr);

}
