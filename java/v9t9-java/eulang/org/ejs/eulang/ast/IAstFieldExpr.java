/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.types.LLType;

/**
 * Point to a field.  
 * @author ejs
 *
 */
public interface IAstFieldExpr extends IAstTypedExpr {
	IAstFieldExpr copy();
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
	
	IAstName getField();
	void setField(IAstName name);
	
	/** get the resolved type of the expr, going through LLUpTypes */
	LLType getDataType(TypeEngine typeEngine);
}
