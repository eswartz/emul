/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.AstVisitor;
import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNode;
import v9t9.tools.ast.expr.IScope;
import v9t9.tools.ast.expr.ISourceRef;

/**
 * @author ejs
 *
 */
public class AstVariableDefinition extends AstTypedExpression implements IAstVariableDefinition {

	private IAstName name;
	private IAstTypedExpression defaultVal;

	/**
	 * 
	 */
	public AstVariableDefinition(IAstName name, LLType type, IAstTypedExpression defaultVal) {
		this.name = name;
		setDefaultValue(defaultVal);
		if (type != null)
			setType(type);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstVariableDefintion#getName()
	 */
	@Override
	public IAstName getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (defaultVal == null)
			return new IAstNode[] { name, defaultVal };
		return new IAstNode[] { name };
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstExpression expr) {
		return expr.equalValue(expr);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#simplify()
	 */
	@Override
	public IAstExpression simplify() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstVariableDefintion#getDefaultValue()
	 */
	@Override
	public IAstTypedExpression getDefaultValue() {
		return defaultVal;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstVariableDefintion#setDefaultValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setDefaultValue(IAstTypedExpression defaultVal) {
		this.defaultVal = defaultVal;
		setDirty(true);
		
		if (defaultVal != null)
			setType(defaultVal.getType());
		else
			setType(null);
	}

}
