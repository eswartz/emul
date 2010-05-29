/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.IBinaryOperation;


/**
 * Traditional binary operations 
 * @author ejs
 *
 */
public interface IAstBinExpr extends IAstTypedExpr {
	IAstBinExpr copy();
	
    public IBinaryOperation getOp();

    public void setOp(IBinaryOperation operator);

    /** Get the left-hand side of the expression */
    public IAstTypedExpr getLeft();

    /** Set the left-hand side of the expression */
    public void setLeft(IAstTypedExpr expr);

    /** Get the right-hand side of the expression */
    public IAstTypedExpr getRight();

    /** Set the right-hand side of the expression */
    public void setRight(IAstTypedExpr expr);
}
