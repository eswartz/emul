/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This is the definition of an argument in a prototype.
 * Its default value will be substituted in a formal argument list
 * if the call does not explicitly provide the argument.  
 * @author ejs
 *
 */
public interface IAstArgDef extends IAstTypedNode {
	IAstArgDef copy();
	
	boolean isMacro();
	void setMacro(boolean isMacro);
	
	boolean isVar();
	void setVar(boolean isVar);
	
	String getName();
	IAstSymbolExpr getSymbolExpr();
	
	IAstType getTypeExpr();
	void setTypeExpr(IAstType typeExpr);
	IAstTypedExpr getDefaultValue();
	void setDefaultValue(IAstTypedExpr defaultVal);
}
