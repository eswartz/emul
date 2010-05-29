/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * This node references the name of the type in which this reference is embedded.
 * This resolves to an LLUpType.
 * @author ejs
 *
 */
public interface IAstSelfReferentialType extends IAstType {
	IAstSelfReferentialType copy();
	IAstSymbolExpr getSymbolExpr();
	void setSymbolExpr(IAstSymbolExpr name);
}
