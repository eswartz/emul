/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstInitIndexExpr extends IAstTypedExpr {
	IAstInitIndexExpr copy();
	
	IAstTypedExpr getIndex();
	void setIndex(IAstTypedExpr index);
}
