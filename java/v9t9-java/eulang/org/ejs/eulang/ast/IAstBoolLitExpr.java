/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstBoolLitExpr extends IAstLitExpr {
	IAstBoolLitExpr copy(IAstNode copyParent);
	boolean getValue();

}