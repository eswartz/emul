/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNameHolder;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public interface IAstDefine extends IAstNode, IAstNameHolder {
	IAstName getName();
	IAstNode getExpression();
	void setExpression(IAstNode expr);
}
