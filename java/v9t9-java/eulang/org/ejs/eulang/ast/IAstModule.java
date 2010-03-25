/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.List;

import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public interface IAstModule extends IAstScope {
	List<IAstNode> initCode();
}
