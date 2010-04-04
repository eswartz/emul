/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.symbols.IScope;


/**
 * @author ejs
 *
 */
public class AstModule extends AstScope implements IAstModule {

	private IAstNodeList<IAstStmt> stmtList;
	/**
	 * 
	 */
	public AstModule(IScope scope) {
		super(scope);
	}
	protected AstModule(IScope scope, IAstNodeList<IAstStmt> stmtList) {
		super(scope);
		setStmtList(stmtList);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstModule copy(IAstNode copyParent) {
		IAstModule copied = new AstModule(getScope().newInstance(getCopyScope(copyParent)), 
				doCopy(stmtList, copyParent));
		remapScope(getScope(), copied.getScope(), copied);
		return fixup(this, copied);
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
	public IAstNodeList<IAstStmt> getStmtList() {
		return stmtList;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstModule#setStmtList(org.ejs.eulang.ast.IAstNodeList)
	 */
	@Override
	public void setStmtList(IAstNodeList<IAstStmt> stmtList) {
		this.stmtList = reparent(this.stmtList, stmtList);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.AstScope#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { stmtList };
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == stmtList) {
			this.stmtList = (IAstNodeList<IAstStmt>) reparent(this.stmtList, another);
		}
	}
}
