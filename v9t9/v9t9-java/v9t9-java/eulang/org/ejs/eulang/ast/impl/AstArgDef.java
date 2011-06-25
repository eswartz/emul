/**
 * 
 */
package org.ejs.eulang.ast.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstArgDef;
import org.ejs.eulang.ast.IAstAttributes;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstArgDef extends AstTypedNode implements IAstArgDef {

	private IAstSymbolExpr name;
	private IAstTypedExpr defaultVal;
	private IAstType typeExpr;
	private Set<String> attrs;

	/**
	 * @param isMacro 
	 * 
	 */
	public AstArgDef(IAstSymbolExpr name, IAstType type, IAstTypedExpr defaultVal, Set<String> attrs) {
		this.name = name;
		name.setParent(this);
		setTypeExpr(type);
		setDefaultValue(defaultVal);
		this.attrs = attrs;
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstArgDef copy() {
		return fixup(this, new AstArgDef(
				doCopy(name), doCopy(typeExpr), doCopy(defaultVal), 
				new HashSet<String>(attrs)));
	}
	
	
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return name + (typeExpr != null && typeExpr.getType() != null ? " : " + typeExpr.getType().toString() : "") + (defaultVal != null ? " = " + defaultVal : "")
			+ toString(attrs); 
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((defaultVal == null) ? 0 : defaultVal.hashCode());
		result = prime * result + (attrs.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (attrs == null) {
			if (other.attrs != null)
				return false;
		} else if (!attrs.equals(other.attrs))
			return false;
		if (typeExpr == null) {
			if (other.typeExpr != null)
				return false;
		} else if (!typeExpr.equals(other.typeExpr))
			return false;
		return true;
	}

	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAttributes#getAttrs()
	 */
	@Override
	public Set<String> getAttrs() {
		return Collections.unmodifiableSet(attrs);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAttributes#attrs()
	 */
	@Override
	public Set<String> attrs() {
		if (attrs == Collections.<String>emptySet())
			attrs = new HashSet<String>();
		return attrs;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstAttributes#hasAttr(java.lang.String)
	 */
	@Override
	public boolean hasAttr(String attr) {
		return attrs.contains(attr);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArgDef#isMacro()
	 */
	@Override
	public boolean isMacro() {
		return hasAttr(MACRO);
	}
	
	@Override
	public boolean isVar() {
		return hasAttr(VAR);
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
		/*
		if (this.name != null) {
			IScope ownerScope = getOwnerScope();
			if (ownerScope == this.name.getSymbol().getScope()) {
				IAstNode def = this.name.getSymbol() != null ? this.name.getSymbol().getDefinition() : null;
				if (def == this)
					this.name.getSymbol().setDefinition(null);
				getOwnerScope().remove(this.name.getSymbol());
				getOwnerScope().add(name.getSymbol());
				name.getSymbol().setDefinition(def);
			}
		}*/
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
		boolean changed = inferTypesFromChildren(new ITyped[] { name, typeExpr, defaultVal });
		// XXX codeptr
		if (type != null && type instanceof LLCodeType && !hasAttr(IAstAttributes.MACRO)) {
			type = typeEngine.getPointerType(type); 
			typeExpr.setType(type);
			name.setType(type);
			changed = true;
		}
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#validateType(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateType(TypeEngine typeEngine) throws TypeException {
		super.validateType(typeEngine);
		
		if (!type.canAllocate())
			throw new TypeException(this, "cannot allocate argument '" + name.getSymbol() + "' of type " + type);

	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#validateTypes()
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (this instanceof IAstTypedNode) {
			LLType thisType = ((IAstTypedNode) this).getType();
			if (thisType == null || !thisType.isComplete())
				return;
			
			for (IAstNode kid : getChildren()) {
				if (kid instanceof IAstTypedNode) {
					LLType kidType = ((IAstTypedNode) kid).getType();
					if (kidType != null && kidType.isComplete()) {
						if (!thisType.equals(kidType)) {
							throw new TypeException(kid, "expression's type does not match parent");
						}
					}
				}
			}
		}
			
	}
}
