/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstFloatLitExpr extends IAstLitExpr {
	IAstFloatLitExpr copy(IAstNode copyParent);
	double getValue();

}