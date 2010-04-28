/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDataType;
import org.ejs.eulang.ast.IAstNamedType;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstScope;
import org.ejs.eulang.ast.IAstSelfReferentialType;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstNamedType extends AstType implements IAstNamedType {

	/**
	 * 
	 */
	//private static final LLVoidType UNKNOWN_NAME = new LLVoidType("self-reference");
	private IAstSymbolExpr symbolExpr;

	/**
	 * @param type
	 */
	public AstNamedType(LLType type, IAstSymbolExpr symbolExpr) {
		super(type);
		setSymbol(symbolExpr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstNamedType copy(IAstNode copyParent) {
		return fixup(this, new AstNamedType(getType(), doCopy(symbolExpr, copyParent)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((symbolExpr == null) ? 0 : symbolExpr.hashCode());
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
		AstNamedType other = (AstNamedType) obj;
		if (symbolExpr == null) {
			if (other.symbolExpr != null)
				return false;
		} else if (!symbolExpr.equals(other.symbolExpr))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return symbolExpr.getSymbol() + ": " + super.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstType#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { symbolExpr };
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == symbolExpr)
			setSymbol((IAstSymbolExpr) another);
		else
			throw new IllegalArgumentException();
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		if (type == null || !type.isComplete()) {
			// check for self-refs
			IAstNode ref = getParent();
			int level = 1;
			while (ref != null) {
				if (ref instanceof IAstDataType && ((IAstDataType) ref).getTypeName().equals(symbolExpr.getOriginalSymbol())) {
					IAstSelfReferentialType selfRef = new AstSelfReferentialType(symbolExpr.copy(this), level);
					selfRef.setSourceRef(getSourceRef());
					getParent().replaceChild(this, selfRef);
					return true;
				}
				if(ref instanceof IAstScope)
					level++;
				ref = ref.getParent();
			}
			
			changed |= inferTypesFromChildren(new ITyped[] { symbolExpr });
		} else {
			changed |= updateType(symbolExpr, type);
		}
		changed |= super.inferTypeFromChildren(typeEngine);
 		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedNode#getType()
	 */
	@Override
	public LLType getType() {
		/*
		if (type == null || !type.isComplete()) {
			// if we're inside the type itself, use its type
			IAstNode node = this;
			while (node != null) {
				if (node == symbolExpr) {
					return UNKNOWN_NAME;
				}
				node = node.getParent();
			}
		}
		*/
		return super.getType();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#setSymbol(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setSymbol(IAstSymbolExpr symbolExpr) {
		this.symbolExpr = reparent(this.symbolExpr, symbolExpr);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#getSymbol()
	 */
	@Override
	public IAstSymbolExpr getSymbol() {
		return symbolExpr;
	}
}
