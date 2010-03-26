/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstName;

/**
 * @author ejs
 *
 */
public interface IAstVariableDefinition extends IAstTypedExpr {
	IAstName getName();
	
	IAstTypedExpr getDefaultValue();
	void setDefaultValue(IAstTypedExpr defaultVal);
}
