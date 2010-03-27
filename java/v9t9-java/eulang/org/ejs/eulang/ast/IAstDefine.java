/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This node defines a name to a node.  This means the node may be substituted for the name.
 * @author ejs
 *
 */
public interface IAstDefine extends IAstNode, IAstNameHolder {
	IAstName getName();
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
