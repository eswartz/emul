/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.llvm.types.LLType;

import v9t9.tools.ast.expr.IAstName;
import v9t9.tools.ast.expr.impl.AstIdExpression;

/**
 * @author ejs
 *
 */
public class AstIdExpr extends AstIdExpression implements IAstIdExpr {

	private LLType type;

	/**
	 * @param name
	 */
	public AstIdExpr(IAstName name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstIdExpression#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " : " +(type != null ? type.toString() : "<unknown>"); 
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
