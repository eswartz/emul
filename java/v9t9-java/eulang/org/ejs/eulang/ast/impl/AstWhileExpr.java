/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstWhileExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class AstWhileExpr extends AstTestBodyLoopExpr implements IAstWhileExpr {

	/**
	 * @param doCopy
	 * @param doCopy2
	 */
	public AstWhileExpr(IScope scope, IAstTypedExpr expr, IAstTypedExpr body) {
		super(scope, expr, body);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("WHILE");
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstWhileExpr copy() {
		return (IAstWhileExpr) fixupLoop(new AstWhileExpr(getScope().newInstance(getCopyScope()), 
				doCopy(getExpr()), doCopy(getBody())));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTestBodyLoopExpr#getExpressionType(org.ejs.eulang.TypeEngine)
	 */
	@Override
	protected LLType getExpressionType(TypeEngine typeEngine) {
		return typeEngine.BOOL;
	}
}
