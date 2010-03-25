/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.impl.AstNode;

/**
 * @author ejs
 *
 */
public class AstDefine extends AstNode implements IAstDefine {

	private final IAstName name;
	private IAstNode expr;

	public AstDefine(IAstName name, IAstNode expr) {
		this.name = name;
		this.expr = expr;
		
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefine#getExpression()
	 */
	@Override
	public IAstNode getExpression() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefine#setExpression(v9t9.tools.ast.expr.IAstNode)
	 */
	@Override
	public void setExpression(IAstNode expr) {
		if (!this.expr.equals(expr)) {
			this.expr = expr;
			setDirty(true);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefine#getName()
	 */
	@Override
	public IAstName getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNameHolder#getRoleForName()
	 */
	@Override
	public int getRoleForName() {
		return NAME_DEFINED;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { name, expr };
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}

}
