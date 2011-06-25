/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstLitExpr extends IAstTypedExpr {
	IAstLitExpr copy();
	String getLiteral();
	
	Object getObject();
	/**
	 * @return
	 */
	boolean isZero();
}
