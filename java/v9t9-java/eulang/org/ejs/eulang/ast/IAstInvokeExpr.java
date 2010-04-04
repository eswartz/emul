/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstInvokeExpr extends IAstTypedExpr {
	IAstInvokeExpr copy(IAstNode parent);
}
