/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstBoolLitExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class AstBoolLitExpr extends AstLitExpr implements
		IAstLitExpr, IAstBoolLitExpr {

	private final boolean value;

	public AstBoolLitExpr(String lit, LLType type, boolean value) {
		super(lit, type);
		this.value = value;
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstBoolLitExpr copy() {
		return fixup(this, new AstBoolLitExpr(getLiteral(), type, value));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString(value+"");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIntegerLiteralExpression#getValue()
	 */
	public boolean getValue() {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLitExpr#getObject()
	 */
	@Override
	public Object getObject() {
		return value;
	}
}
