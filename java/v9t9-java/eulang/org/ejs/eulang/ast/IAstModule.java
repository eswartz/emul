/**
 * 
 */
package org.ejs.eulang.ast;



/**
 * @author ejs
 *
 */
public interface IAstModule extends IAstScope {
	IAstModule copy(IAstNode copyParent);
	void setStmtList(IAstNodeList<IAstStmt> stmtList);
	IAstNodeList<IAstStmt> getStmtList();
}
