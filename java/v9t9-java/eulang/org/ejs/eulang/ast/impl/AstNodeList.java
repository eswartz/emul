/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;


/**
 * @author ejs
 *
 */
public class AstNodeList<T extends IAstNode> extends AstNode implements IAstNodeList<T> {

	private List<T> list = new ArrayList<T>();

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 22;
		result = prime * result + ((list == null) ? 0 : list.hashCode());
		return result;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstNodeList<T> other = (AstNodeList) obj;
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
		return "LIST";
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNodeList#list()
	 */
	@Override
	public List<T> list() {
		return list;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return (IAstNode[]) list.toArray(new IAstNode[list.size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void replaceChildren(IAstNode[] children) {
		list.clear();
		List<T> asList = (List<T>) Arrays.asList(children);
		list.addAll(asList);
	}
	
}
