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
public abstract class AstTypedExpression extends AstExpression implements IAstTypedExpression {

	protected LLType type;

	/**
	 * 
	 */
	public AstTypedExpression() {
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