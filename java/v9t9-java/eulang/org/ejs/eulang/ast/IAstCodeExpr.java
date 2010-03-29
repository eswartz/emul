/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstCodeExpr extends IAstTypedExpr, IAstScope {
	IAstCodeExpr copy(IAstNode copyParent);
	
	IAstPrototype getPrototype();
	IAstNodeList<IAstStmt> stmts();
	
	boolean isMacro();
}
