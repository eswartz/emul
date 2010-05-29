/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstPointerType extends IAstType {
	IAstPointerType copy();
	
	IAstType getBaseType();
	void setBaseType(IAstType typeExpr);
}
