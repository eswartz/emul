/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.ITyped;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstArgDef extends AstTypedExpr implements IAstArgDef {

	private IAstSymbolExpr name;
	private IAstTypedExpr defaultVal;
	private IAstType typeExpr;

	/**
	 * 
	 */
	public AstArgDef(IAstSymbolExpr name, IAstType type, IAstTypedExpr defaultVal) {
		this.name = name;
		name.setParent(this);
		setDefaultValue(defaultVal);
		setTypeExpr(type);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return name + " : " + getTypeString() + (defaultVal != null ? " = " + defaultVal : ""); 
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstVariableDefintion#getName()
	 */
	@Override
	public IAstSymbolExpr getSymbolExpr() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(IAstSymbolExpr name) {
		this.name = reparent(this.name, name);
	}
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (typeExpr != null && defaultVal != null)
			return new IAstNode[] { name, typeExpr, defaultVal };
		else if (defaultVal != null)
			return new IAstNode[] { name, defaultVal };
		else if (typeExpr != null)
			return new IAstNode[] { name, typeExpr };
		else
			return new IAstNode[] { name };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChildren(IAstNode[] children) {
		if (typeExpr != null && defaultVal != null) {
			setName((IAstSymbolExpr) children[0]);
			setTypeExpr((IAstType) children[1]);
			setDefaultValue((IAstTypedExpr) children[2]);
		} else if (defaultVal != null) {
			setName((IAstSymbolExpr) children[0]);
			setDefaultValue((IAstTypedExpr) children[1]);
		} else if (typeExpr != null) {
			setName((IAstSymbolExpr) children[0]);
			setTypeExpr((IAstType) children[1]);
		} else {
			setName((IAstSymbolExpr) children[0]);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstExpr expr) {
		return expr.equalValue(expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArgDef#getName()
	 */
	@Override
	public String getName() {
		return name.getSymbol().getName();
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
		this.defaultVal = reparent(this.defaultVal, defaultVal);
		
		if (defaultVal != null)
			setType(defaultVal.getType());
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArgDef#setTypeExpr(org.ejs.eulang.ast.IAstType)
	 */
	@Override
	public void setTypeExpr(IAstType typeExpr) {
		this.typeExpr = reparent(this.typeExpr, typeExpr);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArgDef#getTypeExpr()
	 */
	@Override
	public IAstType getTypeExpr() {
		return typeExpr;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren()
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine) throws TypeException {
		return inferTypesFromChildren(new ITyped[] { name.getSymbol(), typeExpr, defaultVal });
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		super.setType(type);
		/*
		name.setType(type);
		name.getSymbol().setType(type);
		if (typeExpr != null)
			typeExpr.setType(type);
		*/
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#getType()
	 */
	@Override
	public LLType getType() {
		return name.getType();
	}
}
