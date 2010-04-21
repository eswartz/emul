/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstDataDecl extends IAstTypedExpr, IAstScope {
	IAstDataDecl copy(IAstNode parent);
	
	IAstNodeList<IAstTypedNode> getFields();
	IAstNodeList<IAstTypedNode> getStatics();
	
	void setFields(IAstNodeList<IAstTypedNode> fields);
	void setStatics(IAstNodeList<IAstTypedNode> statics);
}
