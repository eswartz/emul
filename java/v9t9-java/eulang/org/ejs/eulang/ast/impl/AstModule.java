/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.HashMap;
import java.util.Map;

import org.ejs.eulang.ast.IAstModule;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstNodeList;
import org.ejs.eulang.ast.IAstStmt;
import org.ejs.eulang.symbols.IScope;


/**
 * @author ejs
 *
 */
public class AstModule extends AstStmtScope implements IAstModule {

	private Map<String, String> nonFileText = new HashMap<String, String>();
	public AstModule(IScope scope, IAstNodeList<IAstStmt> stmtList) {
		super(stmtList, scope);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstModule copy(IAstNode copyParent) {
		return (IAstModule) fixupStmtScope(new AstModule(getScope().newInstance(getCopyScope(copyParent)), 
				doCopy(stmtList, copyParent)));
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "module";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstModule#getNonFileText()
	 */
	@Override
	public Map<String, String> getNonFileText() {
		return nonFileText;
	}
}
