/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.List;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstLabelStmt;
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
public class AstLabelStmt extends AstTypedExpr implements IAstLabelStmt {

	private IAstSymbolExpr label;

	/**
	 * @param label
	 * @param test
	 */
	public AstLabelStmt(IAstSymbolExpr label) {
		setLabel(label);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstLabelStmt copy(IAstNode copyParent) {
		return fixup(this, new AstLabelStmt(doCopy(label, copyParent)));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("LABEL " + label.toString());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		return NO_CHILDREN;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 999;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstLabelStmt other = (AstLabelStmt) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}



	@Override
	public IAstSymbolExpr getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { label };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getLabel() == existing) {
			setLabel((IAstSymbolExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#setLabel(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setLabel(IAstSymbolExpr symbol) {
		Check.checkArg(symbol);
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
		if (getParent() instanceof IAstNodeList) {
			List<?> list = ((IAstNodeList) getParent()).list();
			int idx = list.indexOf(this);
			if (idx >= 0 && idx + 1 < list.size()) {
				return inferTypesFromChildren(new ITyped[] { (ITyped) list.get(idx + 1) });
			}
		}
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
