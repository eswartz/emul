/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstType extends IAstTypedExpr {
	IAstType copy(IAstNode copyParent);
}
