/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstCodeExpr extends IAstTypedExpr, IAstExpr, IAstScope {
	IAstPrototype getPrototype();
	IAstNodeList<IAstStatement> stmts();
	
	boolean isMacro();
}
