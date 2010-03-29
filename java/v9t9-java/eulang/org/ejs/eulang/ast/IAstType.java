/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstType extends IAstTypedNode {
	IAstType copy(IAstNode copyParent);
}
