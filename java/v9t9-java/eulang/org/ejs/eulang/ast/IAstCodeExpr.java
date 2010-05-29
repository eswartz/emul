/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstCodeExpr extends IAstTypedExpr, IAstStmtScope {
	IAstCodeExpr copy();
	
	IAstPrototype getPrototype();
	
	boolean isMacro();
}
