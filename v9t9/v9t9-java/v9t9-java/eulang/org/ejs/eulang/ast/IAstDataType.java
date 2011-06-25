/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.List;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public interface IAstDataType extends IAstTypedExpr, IAstStmtScope, IAstType {
	IAstDataType copy();
	
	ISymbol getTypeName();
	void setTypeName(ISymbol typeName);
	
	List<IAstRedefinition> redefs();
	
	IAstNodeList<IAstTypedNode> getFields();
	IAstNodeList<IAstTypedNode> getStatics();
	
	void setFields(IAstNodeList<IAstTypedNode> fields);
	void setStatics(IAstNodeList<IAstTypedNode> statics);

	/**
	 * Create the per-instance init code 
	 * @param typeEngine
	 * @return
	 */
	IAstCodeExpr getInitCode(TypeEngine typeEngine);

	/**
	 * Get the constructor (initializer) symbol
	 * @param typeEngine
	 * @return
	 */
	ISymbol getInitName(TypeEngine typeEngine);

	/**
	 * Tell if the initializer does anything beyond default init
	 * @return
	 */
	boolean needsExplicitInit();
}
