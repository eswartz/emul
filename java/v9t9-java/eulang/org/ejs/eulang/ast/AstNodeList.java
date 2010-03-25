/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.ArrayList;
import java.util.List;

import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.impl.AstNode;

/**
 * @author ejs
 *
 */
public class AstNodeList extends AstNode implements IAstNodeList {

	private List<IAstNode> list = new ArrayList<IAstNode>();

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
