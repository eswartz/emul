/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstTopLevelNode extends IAstNode, IAstTypedExpr {
	IAstTopLevelNode copy(IAstNode copyParent);
}
