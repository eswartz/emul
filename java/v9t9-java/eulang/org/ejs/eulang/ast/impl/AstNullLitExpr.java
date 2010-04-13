/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNullLitExpr;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class AstNullLitExpr extends AstLitExpr implements IAstNullLitExpr {

	/**
	 * @param lit 
	 * @param lit
	 * @param type
	 */
	public AstNullLitExpr(String lit, LLType nullType) {
		super(lit, nullType);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "NULL";
	}

	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLitExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstLitExpr copy(IAstNode copyParent) {
		return new AstNullLitExpr(getLiteral(), getType());
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLitExpr#getObject()
	 */
	@Override
	public Object getObject() {
		return null;
	}

}
