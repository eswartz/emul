/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.Map;

import v9t9.tools.ast.expr.IAstName;
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
	
	void add(IAstName name, IAstNode node);
	IAstNode find(IAstName name);
}
