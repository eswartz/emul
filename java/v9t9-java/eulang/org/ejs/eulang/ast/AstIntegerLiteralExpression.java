/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

/**
 * @author ejs
 *
 */
public class AstIntegerLiteralExpression extends AstLiteralExpression implements
		IAstLiteralExpression, IAstIntegerLiteralExpression {

	private final long value;

	public AstIntegerLiteralExpression(String lit, LLType type, long value) {
		super(lit, type);
		this.value = value;
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIntegerLiteralExpression#getValue()
	 */
	public long getValue() {
		return value;
	}
}
