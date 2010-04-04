/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstFloatLitExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.types.LLType;

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
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstFloatLitExpr copy(IAstNode copyParent) {
		return fixup(this, new AstFloatLitExpr(getLiteral(), type, value));
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
	public double getValue() {
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
