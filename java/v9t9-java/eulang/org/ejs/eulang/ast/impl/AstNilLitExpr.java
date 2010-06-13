/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNilLitExpr;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class AstNilLitExpr extends AstLitExpr implements IAstNilLitExpr {

	/**
	 * @param lit 
	 * @param lit
	 * @param type
	 */
	public AstNilLitExpr(String lit, LLType nullType) {
		super(lit, nullType);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("NIL");
	}

	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLitExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstLitExpr copy() {
		return new AstNilLitExpr(getLiteral(), getType());
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLitExpr#getObject()
	 */
	@Override
	public Object getObject() {
		return null;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLitExpr#isZero()
	 */
	@Override
	public boolean isZero() {
		return true;
	}
}
