/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.types.LLType;

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
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstIntLitExpr copy(IAstNode copyParent) {
		return fixup(this, new AstIntLitExpr(getLiteral(), getType(), getValue()));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return value + ":" + getTypeString();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIntegerLiteralExpression#getValue()
	 */
	public long getValue() {
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
