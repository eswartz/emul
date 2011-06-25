/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstBlockStmt extends IAstStmt, IAstStmtScope {
	IAstBlockStmt copy();
}
