/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstCondExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstCondExpr extends AstTypedExpr implements IAstCondExpr {

	private IAstTypedExpr test;
	private IAstTypedExpr expr;

	/**
	 * @param expr2 
	 * @param left
	 * @param right
	 */
	public AstCondExpr(IAstTypedExpr test, IAstTypedExpr expr) {
		setExpr(expr);
		setTest(test);
	}

	public IAstCondExpr copy() {
		return fixup(this, new AstCondExpr(doCopy(test), doCopy(expr)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((test == null) ? 0 : test.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstCondExpr other = (AstCondExpr) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (test == null) {
			if (other.test != null)
				return false;
		} else if (!test.equals(other.test))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("CONDEXPR");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { test, expr };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getExpr() == existing) {
			setExpr((IAstTypedExpr) another);
		} else if (getTest() == existing) {
			setTest((IAstTypedExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
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
	public IAstTypedExpr getTest() {
		return test;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		Check.checkArg(expr);
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAssignStmt#setId(v9t9.tools.ast.expr.IAstIdExpression)
	 */
	@Override
	public void setTest(IAstTypedExpr expr) {
		Check.checkArg(expr);
		this.test = reparent(this.test, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		boolean changed = inferTypesFromChildren(new ITyped[] { expr });
		
		changed |= updateType(test, typeEngine.BOOL);
		
		return changed;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes()
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		LLType thisType = ((IAstTypedNode) this).getType();
		if (thisType == null || !thisType.isComplete())
			return;
		
		LLType kidType = ((IAstTypedNode) expr).getType();
		if (kidType != null && kidType.isComplete()) {
			if (!typeEngine.getBaseType(thisType).equals(typeEngine.getBaseType(kidType))) {
				throw new TypeException(expr, "expression's type does not match parent");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedExpr#simplify(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public IAstTypedExpr simplify(TypeEngine engine) {
		IAstTypedExpr simExpr = expr.simplify(engine);
		IAstTypedExpr simTest = test.simplify(engine);
		if (simTest != test || simExpr != expr) {
			if (simTest == test)
				simTest = (IAstTypedExpr) test.copy();
			if (simExpr == expr)
				simExpr= (IAstTypedExpr) expr.copy();
			IAstCondExpr sim = new AstCondExpr(simTest, simExpr);
			sim.setType(getType());
			sim.setSourceRef(getSourceRef());
			return sim;
		}
		return super.simplify(engine);
	}
}
