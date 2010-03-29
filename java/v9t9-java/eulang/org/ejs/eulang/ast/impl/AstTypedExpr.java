/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.TypeEngine;


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
	 * @see org.ejs.eulang.ast.IAstTypedExpr#simplify(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public IAstExpr simplify(TypeEngine engine) {
		return this;
	}
}