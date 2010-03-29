/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstLitExpr extends IAstTypedExpr {
	IAstLitExpr copy(IAstNode copyParent);
	String getLiteral();
	
	Object getObject();
}
