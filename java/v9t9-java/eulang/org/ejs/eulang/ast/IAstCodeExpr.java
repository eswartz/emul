/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstCodeExpr extends IAstTypedExpr, IAstStmtScope {
	IAstCodeExpr copy(IAstNode copyParent);
	
	IAstPrototype getPrototype();
	
	boolean isMacro();
}
