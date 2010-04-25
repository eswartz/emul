/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstDataType extends IAstTypedExpr, IAstStmtScope, IAstType {
	IAstDataType copy(IAstNode parent);
	
	IAstNodeList<IAstTypedNode> getFields();
	IAstNodeList<IAstTypedNode> getStatics();
	
	void setFields(IAstNodeList<IAstTypedNode> fields);
	void setStatics(IAstNodeList<IAstTypedNode> statics);
	
}
