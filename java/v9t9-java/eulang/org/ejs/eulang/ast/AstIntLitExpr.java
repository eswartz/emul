/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

/**
 * @author ejs
 *
 */
public class AstIntLitExpr extends AstLitExpr implements
		IAstLitExpr, IAstIntLitExpr {

	private final long value;

	public AstIntLitExpr(String lit, LLType type, long value) {
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
	public long getValue() {
		return value;
	}
}
