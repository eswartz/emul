/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public class AstVariableDefinition extends AstTypedExpr implements IAstVariableDefinition {

	private IAstName name;
	private IAstTypedExpr defaultVal;

	/**
	 * 
	 */
	public AstVariableDefinition(IAstName name, LLType type, IAstTypedExpr defaultVal) {
		this.name = name;
		setDefaultValue(defaultVal);
		if (type != null)
			setType(type);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return name + " : " + getType() + (defaultVal != null ? " = " + defaultVal : ""); 
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
		if (defaultVal != null)
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
	public IAstTypedExpr getDefaultValue() {
		return defaultVal;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstVariableDefintion#setDefaultValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setDefaultValue(IAstTypedExpr defaultVal) {
		this.defaultVal = defaultVal;
		setDirty(true);
		
		if (defaultVal != null)
			setType(defaultVal.getType());
		else
			setType(null);
	}

}
