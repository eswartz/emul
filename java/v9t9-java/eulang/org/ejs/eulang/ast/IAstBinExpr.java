/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * Traditional binary operations 
 * @author ejs
 *
 */
public interface IAstBinExpr extends IAstTypedExpr {

    public IOperation getOp();

    public void setOp(IOperation operator);

    /** Get the left-hand side of the expression */
    public IAstTypedExpr getLeft();

    /** Set the left-hand side of the expression */
    public void setLeft(IAstTypedExpr expr);

    /** Get the right-hand side of the expression */
    public IAstTypedExpr getRight();

    /** Set the right-hand side of the expression */
    public void setRight(IAstTypedExpr expr);
}
