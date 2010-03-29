/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstBlockStmt extends IAstStmt, IAstScope {
	IAstBlockStmt copy(IAstNode copyParent);
	IAstNodeList<IAstStmt> stmts();
}
