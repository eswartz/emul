/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;


/**
 * @author ejs
 *
 */
public abstract class AstTypedExpr extends AstTypedNode implements IAstTypedExpr {

	/**
	 * 
	 */
	public AstTypedExpr() {
		super();
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpr#equalValue(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return expr.equals(this);
	}
}