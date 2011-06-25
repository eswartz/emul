/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * An initializer expression.  These appear inside [ ... ] in an assignment.
 * 
 * Each expr has a context, which is an optional expression preceding each
 * entry, e.g.:
 * 
 * x:Int[] = [ 10, [8]=8 ];		 
 * two entries: one with null context and value 10 and another with
 * context IAstIndexExpr(8) and value 8.
 * 
 * x:Struct = [ .x = 10, .y = 5 ];
 * two entries, each with IAstFieldExpr entries as context.
 * @author ejs
 *
 */
public interface IAstInitNodeExpr extends IAstTypedExpr {
	IAstInitNodeExpr copy();
	
	IAstTypedExpr getContext();
	void setContext(IAstTypedExpr context);
	
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
	
	Pair<Integer, LLType> getInitFieldInfo(LLType exprType) throws TypeException;
}
