/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * An expression defining an initializer.  This implicitly contains
 * an "=".
 * @author eswartz
 *
 */
public interface IAstInitializerExpr extends IAstInitializer {
    /** Get the initializer expression (never null) */
    public IAstExpr getExpr();
    
    /** Set the initializer expression (must not be null) */
    public void setExpr(IAstExpr expr);
}
