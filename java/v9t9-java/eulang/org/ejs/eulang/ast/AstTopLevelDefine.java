/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public class AstTopLevelDefine extends AstDefine implements IAstTopLevelNode {

	private LLType type;

	/**
	 * @param name
	 * @param expr
	 */
	public AstTopLevelDefine(IAstName name, IAstNode expr) {
		super(name, expr);
		if (expr instanceof IAstTypedExpression)
			setType(((IAstTypedExpression) expr).getType());
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpression#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpression#setType(org.ejs.eulang.llvm.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		this.type = type;
	}

}
