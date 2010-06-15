/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstRedefinition extends IAstNode {

	String getSymbol();
	void setSymbol(String id);
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
