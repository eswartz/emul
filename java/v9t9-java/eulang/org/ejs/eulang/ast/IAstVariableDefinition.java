/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IScope;

/**
 * @author ejs
 *
 */
public interface IAstVariableDefinition extends IAstTypedExpression {
	IAstName getName();
	
	IAstTypedExpression getDefaultValue();
	void setDefaultValue(IAstTypedExpression defaultVal);
}
