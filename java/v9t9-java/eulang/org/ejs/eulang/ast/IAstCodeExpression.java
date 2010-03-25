/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IScope;

/**
 * @author ejs
 *
 */
public interface IAstCodeExpression extends IAstTypedExpression, IAstExpression, IAstScope {
	IAstPrototype getPrototype();
	IAstNodeList getStmts();
}
