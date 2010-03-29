/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstLitExpr extends IAstExpr, IAstTypedExpr {
	String getLiteral();
	
	Object getObject();
}
