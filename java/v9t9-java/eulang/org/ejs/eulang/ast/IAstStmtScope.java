/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * This is a scope that contains statements.
 * @author ejs
 *
 */
public interface IAstStmtScope extends IAstScope {
	IAstNodeList<IAstStmt> stmts();

	/**
	 * Merge another scope into this one.
	 * @param added
	 */
	void merge(IAstStmtScope added) throws ASTException;
}
