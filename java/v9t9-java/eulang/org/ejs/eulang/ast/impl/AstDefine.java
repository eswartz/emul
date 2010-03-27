/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstDefine;
import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;


/**
 * @author ejs
 *
 */
public class AstDefine extends AstNode implements IAstDefine {

	private final IAstName name;
	private IAstTypedExpr expr;

	public AstDefine(IAstName name, IAstTypedExpr expr) {
		this.name = name;
		name.setParent(this);
		setExpr(expr);
		
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 111;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstDefine other = (AstDefine) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "define";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefine#getExpression()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstDefine#setExpression(v9t9.tools.ast.expr.IAstNode)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
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
