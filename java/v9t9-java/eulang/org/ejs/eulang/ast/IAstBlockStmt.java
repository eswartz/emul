/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstBlockStmt extends IAstStatement {
	IAstNodeList<IAstStatement> stmts();
}
