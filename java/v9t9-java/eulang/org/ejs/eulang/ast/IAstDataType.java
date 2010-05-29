/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public interface IAstDataType extends IAstTypedExpr, IAstStmtScope, IAstType {
	IAstDataType copy();
	
	ISymbol getTypeName();
	void setTypeName(ISymbol typeName);
	
	IAstNodeList<IAstTypedNode> getFields();
	IAstNodeList<IAstTypedNode> getStatics();
	
	void setFields(IAstNodeList<IAstTypedNode> fields);
	void setStatics(IAstNodeList<IAstTypedNode> statics);
	
}
