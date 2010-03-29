/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstStmt extends IAstNode {
	IAstStmt copy(IAstNode copyParent);
}
