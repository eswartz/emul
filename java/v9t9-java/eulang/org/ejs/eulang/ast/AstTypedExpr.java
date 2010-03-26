/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.impl.AstExpression;

/**
 * @author ejs
 *
 */
public abstract class AstTypedExpr extends AstExpression implements IAstTypedExpr {

	protected LLType type;

	/**
	 * 
	 */
	public AstTypedExpr() {
		super();
	}

	@Override
	public LLType getType() {
		return type;
	}

	@Override
	public void setType(LLType type) {
		this.type = type;
	}

}