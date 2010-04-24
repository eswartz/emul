/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * Point to a field.  
 * @author ejs
 *
 */
public interface IAstFieldExpr extends IAstTypedExpr {
	IAstFieldExpr copy(IAstNode parent);
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
	
	IAstName getField();
	void setField(IAstName name);
}
