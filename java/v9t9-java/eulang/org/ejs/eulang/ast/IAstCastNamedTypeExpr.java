/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * A temporary node for an "as" cast expression.  The node is converted into a IAstUnaryExpr
 * with operation CAST once the type expression's type is resolved.
 * 
 * @author eswartz
 *
 */
public interface IAstCastNamedTypeExpr extends IAstTypedExpr {
	IAstCastNamedTypeExpr copy();

    public IAstType getTypeExpr();

    public void setTypeExpr(IAstType type);

    /** Get the target of the expression */
    public IAstTypedExpr getExpr();

    /** Set the target of the expression */
    public void setExpr(IAstTypedExpr expr);

}
