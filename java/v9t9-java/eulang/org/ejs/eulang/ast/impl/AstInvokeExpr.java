/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstCodeExpr;
import org.ejs.eulang.ast.IAstInvokeExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstInvokeExpr extends AstTypedExpr implements IAstInvokeExpr {

	public AstInvokeExpr() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("INVOKE");
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstInvokeExpr copy(IAstNode copyParent) {
		return fixup(this, new AstInvokeExpr());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpr#equalValue(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		// the "child" is actually the enclosing code
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#getType()
	 */
	@Override
	public LLType getType() {
		IAstCodeExpr code = getCodeExpr();
		if (code == null)
			return null;
		return code.getType();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		IAstCodeExpr code = getCodeExpr();
		if (code != null)
			code.setType(type);
	}

	/**
	 * @return
	 */
	private IAstCodeExpr getCodeExpr() {
		IAstNode node = getParent();
		while (node != null) {
			if (node instanceof IAstCodeExpr) {
				return (IAstCodeExpr) node;
			}
			node = node.getParent();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return NO_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChildren(IAstNode[] children) {
		throw new UnsupportedOperationException();
	}

}
