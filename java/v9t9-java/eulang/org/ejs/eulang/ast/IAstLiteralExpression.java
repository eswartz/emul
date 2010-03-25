/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstExpression;

/**
 * @author ejs
 *
 */
public interface IAstLiteralExpression extends IAstExpression, IAstTypedExpression {
	String getLiteral();
}
