/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public class AstLiteralExpression extends AstTypedExpression implements
		IAstLiteralExpression, IAstTypedExpression {

	private String lit;
	public AstLiteralExpression(String lit, LLType type) {
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
	public boolean equalValue(IAstExpression expr) {
		return expr instanceof IAstLiteralExpression && ((IAstLiteralExpression) expr).getLiteral().equals(lit);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#simplify()
	 */
	@Override
	public IAstExpression simplify() {
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

}
