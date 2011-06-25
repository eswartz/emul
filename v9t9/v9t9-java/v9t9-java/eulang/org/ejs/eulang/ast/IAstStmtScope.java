/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.TypeEngine;

/**
 * This is a scope that contains statements.
 * @author ejs
 *
 */
public interface IAstStmtScope extends IAstScope {
	IAstNodeList<IAstStmt> stmts();
	void setStmtList(IAstNodeList<IAstStmt> stmts);

	/**
	 * Merge another scope into this one.
	 * @param added
	 * @param typeEngine TODO
	 */
	void merge(IAstStmtScope added, TypeEngine typeEngine) throws ASTException;
}
