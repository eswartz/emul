/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstBoolLitExpr extends IAstLitExpr {
	IAstBoolLitExpr copy();
	boolean getValue();

}