/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstArgDef;
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
	private boolean isMacro;

	/**
	 * @param isMacro 
	 * 
	 */
	public AstArgDef(IAstSymbolExpr name, IAstType type, IAstTypedExpr defaultVal, boolean isMacro) {
		this.name = name;
		name.setParent(this);
		setTypeExpr(type);
		setDefaultValue(defaultVal);
		setMacro(isMacro);
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstArgDef copy(IAstNode copyParent) {
		return fixup(this, new AstArgDef(
				doCopy(name, copyParent), doCopy(typeExpr, copyParent), doCopy(defaultVal, copyParent), isMacro()));
	}
	
	
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return (isMacro ? "macro " : "") + name + (typeExpr != null && typeExpr.getType() != null ? " : " + typeExpr.getType().toString() : "") + (defaultVal != null ? " = " + defaultVal : ""); 
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((defaultVal == null) ? 0 : defaultVal.hashCode());
		result = prime * result + (isMacro ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((typeExpr == null) ? 0 : typeExpr.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstArgDef other = (AstArgDef) obj;
		if (defaultVal == null) {
			if (other.defaultVal != null)
				return false;
		} else if (!defaultVal.equals(other.defaultVal))
			return false;
		if (isMacro != other.isMacro)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (typeExpr == null) {
			if (other.typeExpr != null)
				return false;
		} else if (!typeExpr.equals(other.typeExpr))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArgDef#isMacro()
	 */
	@Override
	public boolean isMacro() {
		return isMacro;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArgDef#setMacro(boolean)
	 */
	@Override
	public void setMacro(boolean isMacro) {
		this.isMacro = isMacro;
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
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getTypeExpr() == existing) {
			setTypeExpr((IAstType) another);
		} else if (getDefaultValue() == existing) {
			setDefaultValue((IAstTypedExpr) another);
		} else if (name == existing) {
			setName((IAstSymbolExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
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
		name.setType(type);
		/*
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
		return super.getType();
	}
}
