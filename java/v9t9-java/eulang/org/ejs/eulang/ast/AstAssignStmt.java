/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.coffee.core.utils.Check;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public class AstAssignStmt extends AstTypedExpr implements IAstAssignStmt {

	private IAstIdExpr id;
	private IAstTypedExpr expr;
	private IAstType typeExpr;

	/**
	 * @param expr2 
	 * @param left
	 * @param right
	 */
	public AstAssignStmt(IAstIdExpr id, IAstType type, IAstTypedExpr expr) {
		setExpr(expr);
		setTypeExpr(type);
		setId(id);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "=";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (type != null)
			return new IAstNode[] { id, typeExpr, expr };
		return new IAstNode[] { id, expr };
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
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#simplify()
	 */
	@Override
	public IAstExpression simplify() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getId()
	 */
	@Override
	public IAstIdExpr getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		Check.checkArg(expr);
		this.expr = expr;
		if (getType() == null)
			setType(expr.getType());
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setId(v9t9.tools.ast.expr.IAstIdExpression)
	 */
	@Override
	public void setId(IAstIdExpr id) {
		Check.checkArg(id);
		this.id = id;
		if (getType() == null)
			setType(id.getType());
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#getTypeExpr()
	 */
	@Override
	public IAstType getTypeExpr() {
		return typeExpr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setTypeExpr(org.ejs.eulang.ast.IAstType)
	 */
	@Override
	public void setTypeExpr(IAstType type) {
		this.typeExpr = type;
	}

}
