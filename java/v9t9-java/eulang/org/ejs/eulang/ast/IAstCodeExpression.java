/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstExpression;

/**
 * @author ejs
 *
 */
public interface IAstCodeExpression extends IAstTypedExpr, IAstExpression, IAstScope {
	IAstPrototype getPrototype();
	IAstNodeList getStmts();
	
	boolean isMacro();
}
