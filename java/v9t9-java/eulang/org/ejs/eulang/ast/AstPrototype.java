/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.impl.AstNode;

/**
 * @author ejs
 *
 */
public class AstPrototype extends AstNode implements IAstPrototype {
	private IAstType retType;
	private final IAstVariableDefinition[] argumentTypes;

	/** Create with the types; may be null */
	public AstPrototype(IAstType retType, IAstVariableDefinition[] argumentTypes) {
		this.retType = retType;
		retType.setParent(this);
		this.argumentTypes = argumentTypes;
		for (IAstVariableDefinition arg : argumentTypes)
			arg.setParent(this);
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return (retType != null ? retType.toString() : "<Object>") + " (" + catenate(argumentTypes) + ")"; 
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#argumentTypes()
	 */
	@Override
	public IAstVariableDefinition[] argumentTypes() {
		return argumentTypes;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#getReturnType()
	 */
	@Override
	public IAstType returnType() {
		return retType;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		IAstNode[] children = new IAstNode[argumentTypes.length + 1];
		children[0] = retType;
		System.arraycopy(argumentTypes, 0, children, 1, argumentTypes.length);
		return children;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}

}
