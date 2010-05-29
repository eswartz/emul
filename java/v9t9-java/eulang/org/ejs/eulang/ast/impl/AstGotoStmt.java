/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.List;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.types.LLLabelType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstGotoStmt extends AstTypedExpr implements IAstGotoStmt {

	private IAstSymbolExpr label;
	private IAstTypedExpr expr;

	/**
	 * @param label
	 * @param test
	 */
	public AstGotoStmt(IAstSymbolExpr label, IAstTypedExpr test) {
		setLabel(label);
		setExpr(test);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstGotoStmt copy() {
		return fixup(this, new AstGotoStmt(doCopy(label), doCopy(expr)));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("GOTO " + label.toString());
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		if (expr != null)
			return new IAstNode[] { expr };
		return NO_CHILDREN;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 999;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstGotoStmt other = (AstGotoStmt) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#getLabel()
	 */
	@Override
	public IAstSymbolExpr getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (expr != null)
			return new IAstNode[] { label, expr };
		return new IAstNode[] { label };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getLabel() == existing) {
			setLabel((IAstSymbolExpr) another);
		} else if (getExpr() == existing) {
			setExpr((IAstTypedExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#setExpr(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#setLabel(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setLabel(IAstSymbolExpr symbol) {
		this.label = reparent(this.label, symbol);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpr#equalValue(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		/*
		if (label.getSymbol().getDefinition() instanceof IAstTypedExpr)
			return inferTypesFromChildren(new ITyped[] { (ITyped) label.getSymbol().getDefinition() });
		else
			return false;
		*/
		
		if (getParent() instanceof IAstNodeList) {
			List<?> list = ((IAstNodeList) getParent()).list();
			int idx = list.indexOf(this);
			if (idx - 1 >= 0) {
				return inferTypesFromChildren(new ITyped[] { (ITyped) list.get(idx - 1) });
			}
		}
		if (label.getSymbol().getDefinition() instanceof IAstTypedExpr)
			return inferTypesFromChildren(new ITyped[] { (ITyped) label.getSymbol().getDefinition() });
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		LLType thisType = ((IAstTypedNode) this).getType();
		if (thisType == null || !thisType.isComplete())
			return;
		
		if (!(label.getType() instanceof LLLabelType)) {
			throw new TypeException(label, "label does not have label type");
		}
	}
}
