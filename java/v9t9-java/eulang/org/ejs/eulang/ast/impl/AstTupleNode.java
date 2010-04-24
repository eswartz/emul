/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstTupleNode;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * @author ejs
 *
 */
public class AstTupleNode extends AstNode implements IAstTupleNode {

	private IAstNodeList<IAstTypedExpr> elements;
	/**
	 * 
	 */
	public AstTupleNode(IAstNodeList<IAstTypedExpr> elements) {
		this.elements = reparent(this.elements, elements);
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "TUPLE";
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstTupleNode copy(IAstNode copyParent) {
		return fixup(this, new AstTupleNode(doCopy(elements, copyParent)));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 11; //super.hashCode();
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		//if (!super.equals(obj))
		//	return false;
		if (getClass() != obj.getClass())
			return false;
		AstTupleNode other = (AstTupleNode) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTupleNode#elements()
	 */
	@Override
	public IAstNodeList<IAstTypedExpr> elements() {
		return elements;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { elements };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == elements())
			this.elements = (IAstNodeList<IAstTypedExpr>) reparent(this.elements, another);
		else
			throw new IllegalArgumentException();
	}

}
