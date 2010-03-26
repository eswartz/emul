/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;

/**
 * @author ejs
 *
 */
public interface IAstScope extends IAstNode {
	/**
	 * @return
	 */
	IScope getScope(); 
}
