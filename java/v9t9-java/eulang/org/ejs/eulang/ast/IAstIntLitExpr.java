/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstIntLitExpr extends IAstLitExpr {
	IAstIntLitExpr copy(IAstNode copyParent);
	long getValue();

}