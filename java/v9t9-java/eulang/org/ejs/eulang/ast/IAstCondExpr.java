/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstCondExpr extends IAstTypedExpr {
	IAstCondExpr copy();
	
	IAstTypedExpr getTest();
	void setTest(IAstTypedExpr test);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
