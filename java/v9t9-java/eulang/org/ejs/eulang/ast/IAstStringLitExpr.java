/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstStringLitExpr extends IAstLitExpr {
	IAstStringLitExpr copy();
	String getValue();

}