/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.symbols.IScope;


/**
 * @author ejs
 *
 */
public class AstModule extends AstScope implements IAstModule {

	private IAstNodeList stmtList;
	/**
	 * 
	 */
	public AstModule(IScope scope) {
		super(scope);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "module";
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstModule#getStmtList()
	 */
	@Override
	public IAstNodeList getStmtList() {
		return stmtList;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstModule#setStmtList(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setStmtList(IAstNodeList stmtList) {
		this.stmtList = reparent(this.stmtList, stmtList);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.AstScope#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { stmtList };
	}
}
