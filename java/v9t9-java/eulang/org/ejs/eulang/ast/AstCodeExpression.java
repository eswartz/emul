/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;

/**
 * @author ejs
 *
 */
public class AstCodeExpression extends AstTypedExpr implements IAstCodeExpression {

	private final IAstPrototype proto;
	private final IAstNodeList stmts;
	private final IScope scope;
	private final boolean macro;
	/**
	 * @param stmts 
	 * 
	 */
	public AstCodeExpression(IAstPrototype proto, IScope scope, IAstNodeList stmts, boolean macro) {
		this.proto = proto;
		this.scope = scope;
		this.macro = macro;
		scope.setOwner(this);
		this.stmts = stmts;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return macro ? "macro" : "code";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#isMacro()
	 */
	@Override
	public boolean isMacro() {
		return macro;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstScope#getScope()
	 */
	@Override
	public IScope getScope() {
		return scope;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#getPrototype()
	 */
	@Override
	public IAstPrototype getPrototype() {
		return proto;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#getStmts()
	 */
	@Override
	public IAstNodeList getStmts() {
		return stmts;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { proto, stmts };
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstExpression expr) {
		return expr.equals(this);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#simplify()
	 */
	@Override
	public IAstExpression simplify() {
		return this;
	}

}
