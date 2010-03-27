/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstLitExpr extends AstTypedExpr implements
		IAstLitExpr, IAstTypedExpr {

	private String lit;
	public AstLitExpr(String lit, LLType type) {
		this.lit = lit;
		this.type = type;
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstLiteralExpression#getLiteral()
	 */
	@Override
	public String getLiteral() {
		return lit;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstExpr expr) {
		return expr instanceof IAstLitExpr && ((IAstLitExpr) expr).getLiteral().equals(lit);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#simplify()
	 */
	@Override
	public IAstExpr simplify() {
		return this;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return NO_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return NO_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public LLType inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#setTypeOnChildren(org.ejs.eulang.ast.TypeEngine, org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setTypeOnChildren(TypeEngine typeEngine, LLType newType) {
	}
}
