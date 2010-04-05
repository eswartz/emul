/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.Map;



/**
 * @author ejs
 *
 */
public interface IAstModule extends IAstScope {
	IAstModule copy(IAstNode copyParent);
	void setStmtList(IAstNodeList<IAstStmt> stmtList);
	IAstNodeList<IAstStmt> getStmtList();
}
