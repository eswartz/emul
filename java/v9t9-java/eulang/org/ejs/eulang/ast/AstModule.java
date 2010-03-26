/**
 * 
 */
package org.ejs.eulang.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;

/**
 * @author ejs
 *
 */
public class AstModule extends AstScope implements IAstModule {

	private List<IAstNode> initCode = new ArrayList<IAstNode>();

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
	 * @see org.ejs.eulang.ast.IAstModule#initCode()
	 */
	@Override
	public List<IAstNode> initCode() {
		return initCode;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.AstScope#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		List<IAstNode> kids = new ArrayList<IAstNode>(Arrays.asList(super.getChildren()));
		kids.addAll(initCode);
		return (IAstNode[]) kids.toArray(new IAstNode[kids.size()]);
	}
}
