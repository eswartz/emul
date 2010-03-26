/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstExpression;

/**
 * @author ejs
 *
 */
public interface IAstLitExpr extends IAstExpression, IAstTypedExpr {
	String getLiteral();
}
