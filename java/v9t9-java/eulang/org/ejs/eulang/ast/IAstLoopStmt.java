/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * A loop is an expression that yields a value. 
 * @author ejs
 *
 */
public interface IAstLoopStmt extends IAstTypedExpr, IAstStmt, IAstScope {
	//IAstSymbolExpr getResult();
	//void setResult(IAstSymbolExpr result);
	
	IAstTypedExpr getBody();
	void setBody(IAstTypedExpr expr);
}
