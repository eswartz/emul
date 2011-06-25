/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDoWhileExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class AstDoWhileExpr extends AstTestBodyLoopExpr implements IAstDoWhileExpr {

	public AstDoWhileExpr(IScope scope, IAstTypedExpr body, IAstTypedExpr expr) {
		super(scope, expr, body);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("DO");
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstDoWhileExpr copy() {
		return (IAstDoWhileExpr) fixupLoop(new AstDoWhileExpr(getScope().newInstance(getCopyScope()), 
				 doCopy(getBody()), doCopy(getExpr())));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTestBodyLoopExpr#getExpressionType(org.ejs.eulang.TypeEngine)
	 */
	@Override
	protected LLType getExpressionType(TypeEngine typeEngine) {
		return typeEngine.BOOL;
	}
}
