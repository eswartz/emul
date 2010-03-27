/**
 * 
 */
package org.ejs.eulang.ast;



/**
 * @author ejs
 *
 */
public interface IAstModule extends IAstScope {
	void setStmtList(IAstNodeList stmtList);
	IAstNodeList getStmtList();
}
