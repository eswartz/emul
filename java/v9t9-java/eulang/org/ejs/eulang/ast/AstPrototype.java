/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.impl.AstNode;

/**
 * @author ejs
 *
 */
public class AstPrototype extends AstNode implements IAstPrototype {
	private LLType retType;
	private final IAstVariableDefinition[] argumentTypes;

	/** Create with the types; may be null */
	public AstPrototype(LLType retType, IAstVariableDefinition[] argumentTypes) {
		this.retType = retType;
		this.argumentTypes = argumentTypes;
		
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
	public LLType getReturnType() {
		return retType;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstCodeExpression#setReturnType(org.ejs.eulang.llvm.types.LLType)
	 */
	@Override
	public void setReturnType(LLType type) {
		this.retType = type;
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		// TODO Auto-generated method stub
		return null;
	}

}
