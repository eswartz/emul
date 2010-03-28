/**
 * 
 */
package org.ejs.eulang.ast;



/**
 * @author ejs
 *
 */
public interface IAstModule extends IAstScope {
	void setStmtList(IAstNodeList<IAstStatement> stmtList);
	IAstNodeList<IAstStatement> getStmtList();
}
