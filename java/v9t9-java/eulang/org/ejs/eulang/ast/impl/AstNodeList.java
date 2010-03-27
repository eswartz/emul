/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.List;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;


/**
 * @author ejs
 *
 */
public class AstNodeList extends AstNode implements IAstNodeList {

	private List<IAstNode> list = new ArrayList<IAstNode>();

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 22;
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstNodeList other = (AstNodeList) obj;
		if (list == null) {
			if (other.list != null)
				return false;
		} else if (!list.equals(other.list))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "";
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNodeList#list()
	 */
	@Override
	public List<IAstNode> list() {
		return list;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return (IAstNode[]) list.toArray(new IAstNode[list.size()]);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}

}
