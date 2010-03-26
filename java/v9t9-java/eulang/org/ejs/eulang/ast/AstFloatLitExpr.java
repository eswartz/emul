/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

/**
 * @author ejs
 *
 */
public class AstFloatLitExpr extends AstLitExpr implements
		IAstLitExpr, IAstFloatLitExpr {

	private final double value;

	public AstFloatLitExpr(String lit, LLType type, double value) {
		super(lit, type);
		this.value = value;
		
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return value + ":" + getType();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIntegerLiteralExpression#getValue()
	 */
	public double getValue() {
		return value;
	}
}
